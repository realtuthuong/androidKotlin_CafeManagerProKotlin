package com.pro.shopfee.activity.admin

import android.os.Bundle
import android.widget.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.model.Voucher
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddVoucherActivity : BaseActivity() {

    private var tvToolbarTitle: TextView? = null
    private var edtVoucherCode: EditText? = null
    private var edtDiscount: EditText? = null
    private var edtMinimum: EditText? = null
    private var spnMinRank: Spinner? = null
    private var btnAddOrEdit: Button? = null
    private var isUpdate = false
    private var mVoucher: Voucher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_voucher)
        loadDataIntent()
        initUi()
        initView()
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mVoucher = bundleReceived[Constant.KEY_INTENT_VOUCHER_OBJECT] as Voucher?
        }
    }

    private fun initUi() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title)
        edtVoucherCode = findViewById(R.id.edt_voucher_code)
        edtDiscount = findViewById(R.id.edt_discount)
        edtMinimum = findViewById(R.id.edt_minimum)
        spnMinRank = findViewById(R.id.spn_min_rank)
        btnAddOrEdit = findViewById(R.id.btn_add_or_edit)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        btnAddOrEdit?.setOnClickListener { addOrEditVoucher() }
        // Setup spinner items 0..3
        val ranks = arrayOf("Thường", "Bạc", "Vàng", "Kim cương")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ranks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMinRank?.adapter = adapter
    }

    private fun initView() {
        if (isUpdate) {
            tvToolbarTitle!!.text = getString(R.string.label_update_voucher)
            btnAddOrEdit!!.text = getString(R.string.action_edit)
            edtVoucherCode!!.setText(mVoucher!!.code)
            edtDiscount!!.setText(mVoucher!!.discount.toString())
            edtMinimum!!.setText(mVoucher!!.minimum.toString())
            spnMinRank?.setSelection(mVoucher!!.minRankLevel)
        } else {
            tvToolbarTitle!!.text = getString(R.string.label_add_voucher)
            btnAddOrEdit!!.text = getString(R.string.action_add)
        }
    }

    private fun addOrEditVoucher() {
        val strVoucherCode = edtVoucherCode!!.text.toString().trim().uppercase()
        val strDiscount = edtDiscount!!.text.toString().trim { it <= ' ' }
        var strMinimum = edtMinimum!!.text.toString().trim { it <= ' ' }

        if (isEmpty(strVoucherCode)) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDiscount) || strDiscount.toInt() <= 0) {
            Toast.makeText(this, getString(R.string.msg_discount_require), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (isEmpty(strMinimum)) {
            strMinimum = "0"
        }
        // Update voucher
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["code"] = strVoucherCode
            map["discount"] = strDiscount.toInt()
            map["minimum"] = strMinimum.toInt()
            map["minRankLevel"] = spnMinRank?.selectedItemPosition ?: 0
            MyApplication[this].getVoucherDatabaseReference()
                ?.child(mVoucher!!.id.toString())
                ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    Toast.makeText(
                        this,
                        getString(R.string.msg_edit_voucher_success),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            return
        }

        // Add voucher
        showProgressDialog(true)
        val voucherId = System.currentTimeMillis()
        val voucher = Voucher(voucherId, strDiscount.toInt(), strMinimum.toInt(), strVoucherCode)
        voucher.minRankLevel = spnMinRank?.selectedItemPosition ?: 0
        MyApplication[this].getVoucherDatabaseReference()
            ?.child(voucherId.toString())
            ?.setValue(voucher) { _: DatabaseError?, _: DatabaseReference? ->
                showProgressDialog(false)
                edtVoucherCode!!.setText("")
                edtDiscount!!.setText("")
                edtMinimum!!.setText("")
                spnMinRank?.setSelection(0)

                Toast.makeText(
                    this,
                    getString(R.string.msg_add_voucher_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}