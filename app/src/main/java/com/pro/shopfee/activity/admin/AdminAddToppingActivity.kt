package com.pro.shopfee.activity.admin

import android.os.Bundle
import android.widget.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.model.Topping
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddToppingActivity : BaseActivity() {

    private var tvToolbarTitle: TextView? = null
    private var edtName: EditText? = null
    private var edtPrice: EditText? = null
    private var btnAddOrEdit: Button? = null
    private var isUpdate = false
    private var mTopping: Topping? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_topping)
        loadDataIntent()
        initUi()
        initView()
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mTopping = bundleReceived[Constant.KEY_INTENT_TOPPING_OBJECT] as Topping?
        }
    }

    private fun initUi() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title)
        edtName = findViewById(R.id.edt_name)
        edtPrice = findViewById(R.id.edt_price)
        btnAddOrEdit = findViewById(R.id.btn_add_or_edit)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        btnAddOrEdit?.setOnClickListener { addOrEditTopping() }
    }

    private fun initView() {
        if (isUpdate) {
            tvToolbarTitle!!.text = getString(R.string.label_update_topping)
            btnAddOrEdit!!.text = getString(R.string.action_edit)
            edtName!!.setText(mTopping!!.name)
            edtPrice!!.setText(mTopping!!.price.toString())
        } else {
            tvToolbarTitle!!.text = getString(R.string.label_add_topping)
            btnAddOrEdit!!.text = getString(R.string.action_add)
        }
    }

    private fun addOrEditTopping() {
        val strName = edtName!!.text.toString().trim { it <= ' ' }
        val strPrice = edtPrice!!.text.toString().trim { it <= ' ' }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update topping
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["price"] = strPrice.toInt()
            MyApplication[this].getToppingDatabaseReference()
                ?.child(mTopping!!.id.toString())
                ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    Toast.makeText(
                        this,
                        getString(R.string.msg_edit_topping_success), Toast.LENGTH_SHORT
                    ).show()
                    GlobalFunction.hideSoftKeyboard(this)
                }
            return
        }

        // Add topping
        showProgressDialog(true)
        val toppingId = System.currentTimeMillis()
        val topping = Topping(toppingId, strName, strPrice.toInt())
        MyApplication[this].getToppingDatabaseReference()
            ?.child(toppingId.toString())
            ?.setValue(topping) { _: DatabaseError?, _: DatabaseReference? ->
                showProgressDialog(false)
                edtName!!.setText("")
                edtPrice!!.setText("")
                GlobalFunction.hideSoftKeyboard(this)
                Toast.makeText(
                    this,
                    getString(R.string.msg_add_topping_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}