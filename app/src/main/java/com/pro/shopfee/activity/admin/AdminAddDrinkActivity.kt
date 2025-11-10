package com.pro.shopfee.activity.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.adapter.admin.AdminSelectAdapter
import com.pro.shopfee.model.Category
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddDrinkActivity : BaseActivity() {

    private var tvToolbarTitle: TextView? = null
    private var edtName: EditText? = null
    private var edtDescription: EditText? = null
    private var edtPrice: EditText? = null
    private var edtPromotion: EditText? = null
    private var edtImage: EditText? = null
    private var edtImageBanner: EditText? = null
    private var chbFeatured: CheckBox? = null
    private var spnCategory: Spinner? = null
    private var btnAddOrEdit: Button? = null
    private var isUpdate = false
    private var mDrink: Drink? = null
    private var mCategorySelected: Category? = null
    private var mValueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_drink)
        loadDataIntent()
        initUi()
        initData()
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mDrink = bundleReceived[Constant.KEY_INTENT_DRINK_OBJECT] as Drink?
        }
    }

    private fun initUi() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title)
        edtName = findViewById(R.id.edt_name)
        edtDescription = findViewById(R.id.edt_description)
        edtPrice = findViewById(R.id.edt_price)
        edtPromotion = findViewById(R.id.edt_promotion)
        edtImage = findViewById(R.id.edt_image)
        edtImageBanner = findViewById(R.id.edt_image_banner)
        chbFeatured = findViewById(R.id.chb_featured)
        btnAddOrEdit = findViewById(R.id.btn_add_or_edit)
        spnCategory = findViewById(R.id.spn_category)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        btnAddOrEdit?.setOnClickListener { addOrEditDrink() }
    }

    private fun initData() {
        if (isUpdate) {
            tvToolbarTitle!!.text = getString(R.string.label_update_drink)
            btnAddOrEdit!!.text = getString(R.string.action_edit)
            edtName!!.setText(mDrink!!.name)
            edtDescription!!.setText(mDrink!!.description)
            edtPrice!!.setText(mDrink!!.price.toString())
            edtPromotion!!.setText(mDrink!!.sale.toString())
            edtImage!!.setText(mDrink!!.image)
            edtImageBanner!!.setText(mDrink!!.banner)
            chbFeatured!!.isChecked = mDrink!!.isFeatured
        } else {
            tvToolbarTitle!!.text = getString(R.string.label_add_drink)
            btnAddOrEdit!!.text = getString(R.string.action_add)
        }
        loadListCategory()
    }

    private fun loadListCategory() {
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list: MutableList<Category?> = mutableListOf()
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(
                        Category::class.java
                    ) ?: return
                    list.add(0, category)
                }
                val adapter = AdminSelectAdapter(
                    this@AdminAddDrinkActivity,
                    R.layout.item_choose_option, list
                )
                spnCategory!!.adapter = adapter
                spnCategory!!.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        mCategorySelected = adapter.getItem(position)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                if (mDrink != null && mDrink!!.category_id > 0) {
                    spnCategory!!.setSelection(getPositionSelected(list, mDrink!!.category_id))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[this].getCategoryDatabaseReference()
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun getPositionSelected(list: List<Category?>, id: Long): Int {
        var position = 0
        for (i in list.indices) {
            if (id == list[i]!!.id) {
                position = i
                break
            }
        }
        return position
    }

    private fun addOrEditDrink() {
        val strName = edtName!!.text.toString().trim { it <= ' ' }
        val strDescription = edtDescription!!.text.toString().trim { it <= ' ' }
        val strPrice = edtPrice!!.text.toString().trim { it <= ' ' }
        var strPromotion = edtPromotion!!.text.toString().trim { it <= ' ' }
        val strImage = edtImage!!.text.toString().trim { it <= ' ' }
        val strImageBanner = edtImageBanner!!.text.toString().trim { it <= ' ' }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_require), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_require), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (isEmpty(strPromotion)) {
            strPromotion = "0"
        }

        // Update drink
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any?> = HashMap()
            map["name"] = strName
            map["description"] = strDescription
            map["price"] = strPrice.toInt()
            map["sale"] = strPromotion.toInt()
            map["image"] = strImage
            map["banner"] = strImageBanner
            map["featured"] = chbFeatured!!.isChecked
            map["category_id"] = mCategorySelected!!.id
            map["category_name"] = mCategorySelected!!.name
            MyApplication[this].getDrinkDatabaseReference()
                ?.child(mDrink!!.id.toString())
                ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    Toast.makeText(
                        this,
                        getString(R.string.msg_edit_drink_success), Toast.LENGTH_SHORT
                    ).show()
                    GlobalFunction.hideSoftKeyboard(this)
                }
            return
        }

        // Add drink
        showProgressDialog(true)
        val drinkId = System.currentTimeMillis()
        val drink = Drink()
        drink.id = drinkId
        drink.name = strName
        drink.description = strDescription
        drink.price = strPrice.toInt()
        drink.sale = strPromotion.toInt()
        drink.image = strImage
        drink.banner = strImageBanner
        drink.isFeatured = chbFeatured!!.isChecked
        drink.category_id = mCategorySelected!!.id
        drink.category_name = mCategorySelected!!.name
        MyApplication[this].getDrinkDatabaseReference()
            ?.child(drinkId.toString())
            ?.setValue(drink) { _: DatabaseError?, _: DatabaseReference? ->
                showProgressDialog(false)
                edtName!!.setText("")
                edtDescription!!.setText("")
                edtPrice!!.setText("")
                edtPromotion!!.setText("0")
                edtImage!!.setText("")
                edtImageBanner!!.setText("")
                chbFeatured!!.isChecked = false
                spnCategory!!.setSelection(0)
                GlobalFunction.hideSoftKeyboard(this)
                Toast.makeText(this, getString(R.string.msg_add_drink_success), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mValueEventListener?.let {
            MyApplication[this].getCategoryDatabaseReference()?.removeEventListener(it)
        }
    }
}