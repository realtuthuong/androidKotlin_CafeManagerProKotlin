package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ToppingAdapter
import com.pro.shopfee.adapter.ToppingAdapter.IClickToppingListener
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.RatingReview
import com.pro.shopfee.model.Topping
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlideUtils.loadUrlBanner
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus

class DrinkDetailActivity : BaseActivity() {

    private var imgDrink: ImageView? = null
    private var tvName: TextView? = null
    private var tvPriceSale: TextView? = null
    private var tvDescription: TextView? = null
    private var tvSub: TextView? = null
    private var tvAdd: TextView? = null
    private var tvCount: TextView? = null
    private var layoutRatingAndReview: RelativeLayout? = null
    private var tvRate: TextView? = null
    private var tvCountReview: TextView? = null
    private var tvVariantIce: TextView? = null
    private var tvVariantHot: TextView? = null
    private var tvSizeRegular: TextView? = null
    private var tvSizeMedium: TextView? = null
    private var tvSizeLarge: TextView? = null
    private var tvSugarNormal: TextView? = null
    private var tvSugarLess: TextView? = null
    private var tvIceNormal: TextView? = null
    private var tvIceLess: TextView? = null
    private var rcvTopping: RecyclerView? = null
    private var edtNotes: EditText? = null
    private var tvTotal: TextView? = null
    private var tvAddOrder: TextView? = null
    private var mDrinkId: Long = 0
    private var mDrinkOld: Drink? = null
    private var mDrink: Drink? = null
    private var currentVariant: String? = Topping.VARIANT_ICE
    private var currentSize: String? = Topping.SIZE_REGULAR
    private var currentSugar: String? = Topping.SUGAR_NORMAL
    private var currentIce: String? = Topping.ICE_NORMAL
    private var listTopping: MutableList<Topping>? = null
    private var toppingAdapter: ToppingAdapter? = null
    private var variantText = ""
    private var sizeText = ""
    private var sugarText = ""
    private var iceText = ""
    private var toppingIdsText = ""
    private var mDrinkValueEventListener: ValueEventListener? = null
    private var mToppingValueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_detail)
        loadDataIntent()
        initUi()
        loadDrinkDetailFromFirebase()
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        mDrinkId = bundle.getLong(Constant.DRINK_ID)
        if (bundle[Constant.DRINK_OBJECT] != null) {
            mDrinkOld = bundle[Constant.DRINK_OBJECT] as Drink?
        }
    }

    private fun initUi() {
        imgDrink = findViewById(R.id.img_drink)
        tvName = findViewById(R.id.tv_name)
        tvPriceSale = findViewById(R.id.tv_price_sale)
        tvDescription = findViewById(R.id.tv_description)
        tvSub = findViewById(R.id.tv_sub)
        tvAdd = findViewById(R.id.tv_add)
        tvCount = findViewById(R.id.tv_count)
        layoutRatingAndReview = findViewById(R.id.layout_rating_and_review)
        tvCountReview = findViewById(R.id.tv_count_review)
        tvRate = findViewById(R.id.tv_rate)
        tvVariantIce = findViewById(R.id.tv_variant_ice)
        tvVariantHot = findViewById(R.id.tv_variant_hot)
        tvSizeRegular = findViewById(R.id.tv_size_regular)
        tvSizeMedium = findViewById(R.id.tv_size_medium)
        tvSizeLarge = findViewById(R.id.tv_size_large)
        tvSugarNormal = findViewById(R.id.tv_sugar_normal)
        tvSugarLess = findViewById(R.id.tv_sugar_less)
        tvIceNormal = findViewById(R.id.tv_ice_normal)
        tvIceLess = findViewById(R.id.tv_ice_less)
        rcvTopping = findViewById(R.id.rcv_topping)
        edtNotes = findViewById(R.id.edt_notes)
        tvTotal = findViewById(R.id.tv_total)
        tvAddOrder = findViewById(R.id.tv_add_order)
    }

    private fun loadDrinkDetailFromFirebase() {
        showProgressDialog(true)
        mDrinkValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressDialog(false)
                mDrink = snapshot.getValue(Drink::class.java)
                if (mDrink == null) return
                initToolbar()
                initData()
                initListener()
                loadListToppingFromFirebase()
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressDialog(false)
                showToastMessage(getString(R.string.msg_get_date_error))
            }
        }
        MyApplication[this].getDrinkDetailDatabaseReference(mDrinkId)
            ?.addValueEventListener(mDrinkValueEventListener!!)
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = mDrink!!.name
    }

    private fun initData() {
        if (mDrink == null) return
        loadUrlBanner(mDrink!!.banner, imgDrink!!)
        tvName!!.text = mDrink!!.name
        val strPrice = mDrink!!.realPrice.toString() + Constant.CURRENCY
        tvPriceSale!!.text = strPrice
        tvDescription!!.text = mDrink!!.description
        if (mDrinkOld != null) {
            mDrink!!.count = mDrinkOld!!.count
        } else {
            mDrink!!.count = 1
        }
        tvCount!!.text = mDrink!!.count.toString()
        tvRate!!.text = mDrink!!.rate.toString()
        val strCountReview = "(" + mDrink!!.countReviews + ")"
        tvCountReview!!.text = strCountReview
        if (mDrinkOld != null) {
            if (isEmpty(mDrinkOld!!.toppingIds)) calculatorTotalPrice()
        } else {
            calculatorTotalPrice()
        }
        if (mDrinkOld != null) {
            setValueToppingVariant(mDrinkOld!!.variant)
            setValueToppingSize(mDrinkOld!!.size)
            setValueToppingSugar(mDrinkOld!!.sugar)
            setValueToppingIce(mDrinkOld!!.ice)
            edtNotes!!.setText(mDrinkOld!!.note)
        } else {
            setValueToppingVariant(Topping.VARIANT_ICE)
            setValueToppingSize(Topping.SIZE_REGULAR)
            setValueToppingSugar(Topping.SUGAR_NORMAL)
            setValueToppingIce(Topping.ICE_NORMAL)
        }
    }

    private fun initListener() {
        tvSub!!.setOnClickListener {
            val count = tvCount!!.text.toString().toInt()
            if (count <= 1) {
                return@setOnClickListener
            }
            val newCount = tvCount!!.text.toString().toInt() - 1
            tvCount!!.text = newCount.toString()
            calculatorTotalPrice()
        }
        tvAdd!!.setOnClickListener {
            val newCount = tvCount!!.text.toString().toInt() + 1
            tvCount!!.text = newCount.toString()
            calculatorTotalPrice()
        }
        tvVariantIce!!.setOnClickListener {
            if (Topping.VARIANT_ICE != currentVariant) {
                setValueToppingVariant(Topping.VARIANT_ICE)
            }
        }
        tvVariantHot!!.setOnClickListener {
            if (Topping.VARIANT_HOT != currentVariant) {
                setValueToppingVariant(Topping.VARIANT_HOT)
            }
        }
        tvSizeRegular!!.setOnClickListener {
            if (Topping.SIZE_REGULAR != currentSize) {
                setValueToppingSize(Topping.SIZE_REGULAR)
            }
        }
        tvSizeMedium!!.setOnClickListener {
            if (Topping.SIZE_MEDIUM != currentSize) {
                setValueToppingSize(Topping.SIZE_MEDIUM)
            }
        }
        tvSizeLarge!!.setOnClickListener {
            if (Topping.SIZE_LARGE != currentSize) {
                setValueToppingSize(Topping.SIZE_LARGE)
            }
        }
        tvSugarNormal!!.setOnClickListener {
            if (Topping.SUGAR_NORMAL != currentSugar) {
                setValueToppingSugar(Topping.SUGAR_NORMAL)
            }
        }
        tvSugarLess!!.setOnClickListener {
            if (Topping.SUGAR_LESS != currentSugar) {
                setValueToppingSugar(Topping.SUGAR_LESS)
            }
        }
        tvIceNormal!!.setOnClickListener {
            if (Topping.ICE_NORMAL != currentIce) {
                setValueToppingIce(Topping.ICE_NORMAL)
            }
        }
        tvIceLess!!.setOnClickListener {
            if (Topping.ICE_LESS != currentIce) {
                setValueToppingIce(Topping.ICE_LESS)
            }
        }
        layoutRatingAndReview!!.setOnClickListener {
            val bundle = Bundle()
            val ratingReview =
                RatingReview(RatingReview.TYPE_RATING_REVIEW_DRINK, mDrink!!.id.toString())
            bundle.putSerializable(Constant.RATING_REVIEW_OBJECT, ratingReview)
            startActivity(
                this@DrinkDetailActivity,
                RatingReviewActivity::class.java, bundle
            )
        }
        tvAddOrder!!.setOnClickListener {
            mDrink!!.option = getAllOption()
            mDrink!!.variant = currentVariant
            mDrink!!.size = currentSize
            mDrink!!.sugar = currentSugar
            mDrink!!.ice = currentIce
            mDrink!!.toppingIds = toppingIdsText
            val notes = edtNotes!!.text.toString().trim { it <= ' ' }
            if (!isEmpty(notes)) {
                mDrink!!.note = notes
            }
            if (!isDrinkInCart()) {
                getInstance(this@DrinkDetailActivity)!!.drinkDAO()!!.insertDrink(mDrink)
            } else {
                getInstance(this@DrinkDetailActivity)!!.drinkDAO()!!.updateDrink(mDrink)
            }
            startActivity(this@DrinkDetailActivity, CartActivity::class.java)
            EventBus.getDefault().post(DisplayCartEvent())
            finish()
        }
    }

    private fun setValueToppingVariant(type: String?) {
        currentVariant = type
        when (type) {
            Topping.VARIANT_ICE -> {
                tvVariantIce!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvVariantIce!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvVariantHot!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvVariantHot!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                variantText =
                    getString(R.string.label_variant) + " " + tvVariantIce!!.text.toString()
            }
            Topping.VARIANT_HOT -> {
                tvVariantIce!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvVariantIce!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvVariantHot!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvVariantHot!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                variantText =
                    getString(R.string.label_variant) + " " + tvVariantHot!!.text.toString()
            }
        }
    }

    private fun setValueToppingSize(type: String?) {
        currentSize = type
        when (type) {
            Topping.SIZE_REGULAR -> {
                tvSizeRegular!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvSizeRegular!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvSizeMedium!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeMedium!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvSizeLarge!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeLarge!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                sizeText = getString(R.string.label_size) + " " + tvSizeRegular!!.text.toString()
            }
            Topping.SIZE_MEDIUM -> {
                tvSizeRegular!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeRegular!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvSizeMedium!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvSizeMedium!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvSizeLarge!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeLarge!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                sizeText = getString(R.string.label_size) + " " + tvSizeMedium!!.text.toString()
            }
            Topping.SIZE_LARGE -> {
                tvSizeRegular!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeRegular!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvSizeMedium!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSizeMedium!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvSizeLarge!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvSizeLarge!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                sizeText = (tvSizeLarge!!.text.toString() + " "
                        + getString(R.string.label_size))
            }
        }
    }

    private fun setValueToppingSugar(type: String?) {
        currentSugar = type
        when (type) {
            Topping.SUGAR_NORMAL -> {
                tvSugarNormal!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvSugarNormal!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvSugarLess!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSugarLess!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                sugarText = (tvSugarNormal!!.text.toString() + " "
                        + getString(R.string.label_sugar))
            }
            Topping.SUGAR_LESS -> {
                tvSugarNormal!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvSugarNormal!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvSugarLess!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvSugarLess!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                sugarText = (tvSugarLess!!.text.toString() + " "
                        + getString(R.string.label_sugar))
            }
        }
    }

    private fun setValueToppingIce(type: String?) {
        currentIce = type
        when (type) {
            Topping.ICE_NORMAL -> {
                tvIceNormal!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvIceNormal!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvIceLess!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvIceLess!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                iceText = tvIceNormal!!.text.toString() + " " + getString(R.string.label_ice)
            }
            Topping.ICE_LESS -> {
                tvIceNormal!!.setBackgroundResource(R.drawable.bg_white_corner_6_border_main)
                tvIceNormal!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                tvIceLess!!.setBackgroundResource(R.drawable.bg_main_corner_6)
                tvIceLess!!.setTextColor(ContextCompat.getColor(this, R.color.white))
                iceText = tvIceLess!!.text.toString() + " " + getString(R.string.label_ice)
            }
        }
    }

    private fun loadListToppingFromFirebase() {
        mToppingValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listTopping != null) {
                    listTopping!!.clear()
                } else {
                    listTopping = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val topping = dataSnapshot.getValue(Topping::class.java)
                    if (topping != null) {
                        listTopping!!.add(topping)
                    }
                }
                displayListTopping()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[this].getToppingDatabaseReference()
            ?.addValueEventListener(mToppingValueEventListener!!)
    }

    private fun displayListTopping() {
        val linearLayoutManager = LinearLayoutManager(this)
        rcvTopping!!.layoutManager = linearLayoutManager
        toppingAdapter = ToppingAdapter(
            listTopping,
            object : IClickToppingListener {
                override fun onClickToppingItem(topping: Topping) {
                    handleClickItemTopping(topping)
                }
            })
        rcvTopping!!.adapter = toppingAdapter
        handleSetToppingDrinkOld()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSetToppingDrinkOld() {
        if (mDrinkOld == null || isEmpty(mDrinkOld!!.toppingIds)) return
        if (listTopping == null || listTopping!!.isEmpty()) return
        val tempId = mDrinkOld!!.toppingIds!!.split(",").toTypedArray()
        for (s in tempId) {
            for (topping in listTopping!!) {
                if (topping.id == s.toLong()) {
                    topping.isSelected = true
                    break
                }
            }
        }
        if (toppingAdapter != null) toppingAdapter!!.notifyDataSetChanged()
        calculatorTotalPrice()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleClickItemTopping(topping: Topping) {
        for (toppingEntity in listTopping!!) {
            if (toppingEntity.id == topping.id) {
                toppingEntity.isSelected = !toppingEntity.isSelected
            }
        }
        if (toppingAdapter != null) toppingAdapter!!.notifyDataSetChanged()
        calculatorTotalPrice()
    }

    private fun calculatorTotalPrice() {
        val count = tvCount!!.text.toString().trim { it <= ' ' }.toInt()
        val priceOneDrink = mDrink!!.realPrice + getTotalPriceTopping()
        val totalPrice = priceOneDrink * count
        val strTotalPrice = totalPrice.toString() + Constant.CURRENCY
        tvTotal!!.text = strTotalPrice
        mDrink!!.count = count
        mDrink!!.priceOneDrink = priceOneDrink
        mDrink!!.totalPrice = totalPrice
    }

    private fun getTotalPriceTopping(): Int {
        if (listTopping == null || listTopping!!.isEmpty()) return 0
        var total = 0
        for (topping in listTopping!!) {
            if (topping.isSelected) {
                total += topping.price
            }
        }
        return total
    }

    private fun getAllToppingSelected(): String {
        if (listTopping == null || listTopping!!.isEmpty()) return ""
        var strTopping = ""
        for (topping in listTopping!!) {
            if (topping.isSelected) {
                if (isEmpty(strTopping)) {
                    strTopping += topping.name
                    toppingIdsText += topping.id.toString()
                } else {
                    strTopping += ", " + topping.name
                }
                toppingIdsText += if (isEmpty(toppingIdsText)) {
                    topping.id.toString()
                } else {
                    "," + topping.id
                }
            }
        }
        return strTopping
    }

    private fun isDrinkInCart(): Boolean {
        val list = getInstance(this)?.drinkDAO()!!.checkDrinkInCart(mDrink!!.id)
        return list != null && list.isNotEmpty()
    }

    private fun getAllOption(): String {
        var option = "$variantText, $sizeText, $sugarText, $iceText"
        val allToppingSelected = getAllToppingSelected()
        if (!isEmpty(allToppingSelected)) {
            option += ", $allToppingSelected"
        }
        val notes = edtNotes!!.text.toString().trim { it <= ' ' }
        if (!isEmpty(notes)) {
            option += ", $notes"
        }
        return option
    }

    override fun onDestroy() {
        super.onDestroy()
        mDrinkValueEventListener?.let {
            MyApplication[this].getDrinkDetailDatabaseReference(mDrinkId)?.removeEventListener(it)
        }
        mToppingValueEventListener?.let {
            MyApplication[this].getToppingDatabaseReference()?.removeEventListener(it)
        }
    }
}