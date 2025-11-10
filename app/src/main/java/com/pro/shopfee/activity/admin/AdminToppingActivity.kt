package com.pro.shopfee.activity.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.adapter.admin.AdminToppingAdapter
import com.pro.shopfee.listener.IOnAdminManagerToppingListener
import com.pro.shopfee.model.Topping
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.utils.Utils.getTextSearch
import java.util.*

class AdminToppingActivity : BaseActivity() {

    private var mListTopping: MutableList<Topping>? = null
    private var mAdminToppingAdapter: AdminToppingAdapter? = null
    private var mChildEventListener: ChildEventListener? = null
    private var edtSearchName: EditText? = null
    private var imgSearch: ImageView? = null
    private var btnAdd: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_topping)
        initToolbar()
        initUi()
        initView()
        initListener()
        loadListTopping("")
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.manage_topping)
    }

    private fun initUi() {
        edtSearchName = findViewById(R.id.edt_search_name)
        imgSearch = findViewById(R.id.img_search)
        btnAdd = findViewById(R.id.btn_add)
    }

    private fun initView() {
        val rcvData = findViewById<RecyclerView>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvData.layoutManager = linearLayoutManager
        mListTopping = ArrayList()
        mAdminToppingAdapter =
            AdminToppingAdapter(mListTopping, object : IOnAdminManagerToppingListener {
                override fun onClickUpdateTopping(topping: Topping) {
                    onClickEditTopping(topping)
                }

                override fun onClickDeleteTopping(topping: Topping) {
                    deleteToppingItem(topping)
                }
            })
        rcvData.adapter = mAdminToppingAdapter
        rcvData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    btnAdd!!.hide()
                } else {
                    btnAdd!!.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initListener() {
        btnAdd!!.setOnClickListener { onClickAddTopping() }
        imgSearch!!.setOnClickListener { searchTopping() }
        edtSearchName!!.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchTopping()
                return@setOnEditorActionListener true
            }
            false
        }
        edtSearchName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchTopping()
                }
            }
        })
    }

    private fun onClickAddTopping() {
        startActivity(this, AdminAddToppingActivity::class.java)
    }

    private fun onClickEditTopping(topping: Topping) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_TOPPING_OBJECT, topping)
        startActivity(this, AdminAddToppingActivity::class.java, bundle)
    }

    private fun deleteToppingItem(topping: Topping) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.msg_delete_title))
            .setMessage(getString(R.string.msg_confirm_delete))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                MyApplication[this].getToppingDatabaseReference()
                    ?.child(topping.id.toString())
                    ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                        Toast.makeText(
                            this,
                            getString(R.string.msg_delete_topping_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun searchTopping() {
        val strKey = edtSearchName!!.text.toString().trim { it <= ' ' }
        resetListTopping()
        if (mChildEventListener != null) {
            MyApplication[this].getToppingDatabaseReference()!!
                .removeEventListener(mChildEventListener!!)
        }
        loadListTopping(strKey)
        GlobalFunction.hideSoftKeyboard(this)
    }

    private fun resetListTopping() {
        if (mListTopping != null) {
            mListTopping!!.clear()
        } else {
            mListTopping = ArrayList()
        }
    }

    private fun loadListTopping(keyword: String?) {
        mChildEventListener = object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val topping = dataSnapshot.getValue(Topping::class.java)
                if (topping == null || mListTopping == null) return
                if (isEmpty(keyword)) {
                    mListTopping!!.add(0, topping)
                } else {
                    if (getTextSearch(topping.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                            .contains(getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                        mListTopping!!.add(0, topping)
                    }
                }
                if (mAdminToppingAdapter != null) mAdminToppingAdapter!!.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val topping = dataSnapshot.getValue(Topping::class.java)
                if (topping == null || mListTopping == null || mListTopping!!.isEmpty()) return
                for (i in mListTopping!!.indices) {
                    if (topping.id == mListTopping!![i].id) {
                        mListTopping!![i] = topping
                        break
                    }
                }
                if (mAdminToppingAdapter != null) mAdminToppingAdapter!!.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val topping = dataSnapshot.getValue(Topping::class.java)
                if (topping == null || mListTopping == null || mListTopping!!.isEmpty()) return
                for (toppingObject in mListTopping!!) {
                    if (topping.id == toppingObject.id) {
                        mListTopping!!.remove(toppingObject)
                        break
                    }
                }
                if (mAdminToppingAdapter != null) mAdminToppingAdapter!!.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        MyApplication[this].getToppingDatabaseReference()
            ?.addChildEventListener(mChildEventListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        mChildEventListener?.let {
            MyApplication[this].getToppingDatabaseReference()?.removeEventListener(it)
        }
    }
}