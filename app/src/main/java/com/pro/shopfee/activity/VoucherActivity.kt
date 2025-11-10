package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.VoucherAdapter
import com.pro.shopfee.adapter.VoucherAdapter.IClickVoucherListener
import com.pro.shopfee.event.VoucherSelectedEvent
import com.pro.shopfee.model.Voucher
import com.pro.shopfee.utils.Constant
import org.greenrobot.eventbus.EventBus
import com.pro.shopfee.prefs.DataStoreManager

class VoucherActivity : BaseActivity() {

    private var edtSearchName: EditText? = null
    private var imgSearch: ImageView? = null
    private var listVoucher: MutableList<Voucher>? = null
    private var voucherAdapter: VoucherAdapter? = null
    private var amount = 0
    private var voucherSelectedId: Long = 0
    private var mValueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deBUG -- TUTHUONG 10/9/2025
        try {
            setContentView(R.layout.activity_voucher)
            loadDataIntent()
            initToolbar()
            initUi()
            initListener()
            loadListVoucherFromFirebase()
        } catch (e: Exception) {
            e.printStackTrace()
            showToastMessage("Lỗi khởi tạo: ${e.message}")
            finish()
        }
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        voucherSelectedId = bundle.getLong(Constant.VOUCHER_ID, 0)
        amount = bundle.getInt(Constant.AMOUNT_VALUE, 0)
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.title_voucher)
    }

    private fun initUi() {
        edtSearchName = findViewById(R.id.edt_search_name)
        imgSearch = findViewById(R.id.img_search)
        val rcvVoucher = findViewById<RecyclerView>(R.id.rcv_voucher)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvVoucher.layoutManager = linearLayoutManager
        listVoucher = ArrayList()
        voucherAdapter = VoucherAdapter(
            this,
            listVoucher,
            amount,
            object : IClickVoucherListener {
                override fun onClickVoucherItem(voucher: Voucher) {
                    handleClickVoucher(voucher)
                }
            })
        rcvVoucher.adapter = voucherAdapter
    }

    private fun initListener() {
        imgSearch?.setOnClickListener { searchVoucherByCode() }
        edtSearchName?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                searchVoucherByCode()
                true
            } else {
                false
            }
        }
    }

    private fun searchVoucherByCode() {
        val searchCode = edtSearchName?.text.toString().trim().uppercase()
        if (searchCode.isEmpty()) {
            showToastMessage("Vui lòng nhập mã giảm giá")
            return
        }
        
        showProgressDialog(true)
        MyApplication[this].getVoucherDatabaseReference()
            ?.orderByChild("code")
            ?.equalTo(searchCode)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    showProgressDialog(false)
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val voucher = dataSnapshot.getValue(Voucher::class.java)
                            if (voucher != null) {
                                val rank = DataStoreManager.user?.rankLevel ?: 0
                                if (rank >= voucher.minRankLevel && voucher.isVoucherEnable(amount) && voucher.isAvailable()) {
                                    handleClickVoucher(voucher)
                                } else {
                                    if (rank < voucher.minRankLevel) {
                                        showToastMessage("Mã chỉ áp dụng cho hạng ${rankNameOf(voucher.minRankLevel)} trở lên")
                                    } else if (!voucher.isAvailable()) {
                                        showToastMessage("Mã giảm giá đã hết lượt sử dụng")
                                    } else {
                                        showToastMessage(voucher.getCondition(amount))
                                    }
                                }
                                return
                            }
                        }
                    } else {
                        showToastMessage("Mã giảm giá không tồn tại hoặc đã hết hạn")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showProgressDialog(false)
                    showToastMessage("Lỗi khi tìm kiếm mã giảm giá")
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListVoucherFromFirebase() {
        showProgressDialog(true)
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressDialog(false)
                resetListVoucher()

                val rank = DataStoreManager.user?.rankLevel ?: 0
                for (dataSnapshot in snapshot.children) {
                    val voucher = dataSnapshot.getValue(Voucher::class.java)
                    if (voucher != null) {
                        if (rank >= voucher.minRankLevel && voucher.isAvailable()) {
                            listVoucher!!.add(0, voucher)
                        }
                    }
                }

                if (voucherSelectedId > 0 && !listVoucher.isNullOrEmpty()) {
                    for (voucher in listVoucher!!) {
                        if (voucher.id == voucherSelectedId) {
                            voucher.isSelected = true
                            break
                        }
                    }
                }

                voucherAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressDialog(false)
                showToastMessage(getString(R.string.msg_get_date_error))
            }
        }

        MyApplication[this]
            .getVoucherDatabaseReference()
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun resetListVoucher() {
        if (listVoucher != null) {
            listVoucher!!.clear()
        } else {
            listVoucher = ArrayList()
        }
    }

    private fun handleClickVoucher(voucher: Voucher) {
        EventBus.getDefault().post(VoucherSelectedEvent(voucher))
        finish()
    }

    private fun rankNameOf(level: Int): String = when (level) {
        3 -> "Kim cương"
        2 -> "Vàng"
        1 -> "Bạc"
        else -> "Thường"
    }

    override fun onDestroy() {
        super.onDestroy()
        voucherAdapter?.release()
        mValueEventListener?.let {
            MyApplication[this].getVoucherDatabaseReference()?.removeEventListener(it)
        }
    }
}
