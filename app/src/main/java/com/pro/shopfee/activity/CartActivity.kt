package com.pro.shopfee.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.Geocoder
import android.location.Address
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pro.shopfee.R
import com.pro.shopfee.adapter.CartAdapter
import com.pro.shopfee.adapter.CartAdapter.IClickCartListener
import com.pro.shopfee.database.DrinkDatabase.Companion.getInstance
import com.pro.shopfee.event.*
import com.pro.shopfee.model.*
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.VnPayHelper
import com.pro.shopfee.utils.PendingOrders
import com.pro.shopfee.utils.GlobalFunction.startActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.io.IOException
import java.util.Locale

class CartActivity : BaseActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private var rcvCart: RecyclerView? = null
    private var layoutAddOrder: LinearLayout? = null
    private var layoutPaymentMethod: RelativeLayout? = null
    private var tvPaymentMethod: TextView? = null
    private var layoutAddress: RelativeLayout? = null
    private var tvAddress: TextView? = null
    private var layoutVoucher: RelativeLayout? = null
    private var tvVoucher: TextView? = null
    private var tvNameVoucher: TextView? = null
    private var tvPriceDrink: TextView? = null
    private var tvCountItem: TextView? = null
    private var tvAmount: TextView? = null
    private var tvPriceVoucher: TextView? = null
    private var tvCheckout: TextView? = null
    private var listDrinkCart: MutableList<Drink>? = null
    private var cartAdapter: CartAdapter? = null
    private var priceDrink = 0
    private var mAmount = 0
    private var mDiscount = 0
    private var mDiscountLabel: String? = null
    private var paymentMethodSelected: PaymentMethod? = null
    private var addressSelected: com.pro.shopfee.model.Address? = null
    private var voucherSelected: Voucher? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initToolbar()
        initUi()
        initListener()
        initData()
        requestLocationPermission()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.label_cart)
    }

    private fun initUi() {
        rcvCart = findViewById(R.id.rcv_cart)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvCart?.layoutManager = linearLayoutManager
        layoutAddOrder = findViewById(R.id.layout_add_order)
        layoutPaymentMethod = findViewById(R.id.layout_payment_method)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)
        layoutAddress = findViewById(R.id.layout_address)
        tvAddress = findViewById(R.id.tv_address)
        layoutVoucher = findViewById(R.id.layout_voucher)
        tvVoucher = findViewById(R.id.tv_voucher)
        tvNameVoucher = findViewById(R.id.tv_name_voucher)
        tvCountItem = findViewById(R.id.tv_count_item)
        tvPriceDrink = findViewById(R.id.tv_price_drink)
        tvAmount = findViewById(R.id.tv_amount)
        tvPriceVoucher = findViewById(R.id.tv_price_voucher)
        tvCheckout = findViewById(R.id.tv_checkout)
    }

    private fun initListener() {
        layoutAddOrder!!.setOnClickListener { finish() }
        layoutPaymentMethod!!.setOnClickListener {
            val bundle = Bundle()
            if (paymentMethodSelected != null) {
                bundle.putInt(Constant.PAYMENT_METHOD_ID, paymentMethodSelected!!.id)
            }
            startActivity(this@CartActivity, PaymentMethodActivity::class.java, bundle)
        }
        layoutAddress!!.setOnClickListener {
            val bundle = Bundle()
            if (addressSelected != null) {
                bundle.putLong(Constant.ADDRESS_ID, addressSelected!!.id)
            }
            startActivity(this@CartActivity, AddressActivity::class.java, bundle)
        }
        layoutVoucher!!.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(Constant.AMOUNT_VALUE, priceDrink)
            if (voucherSelected != null) {
                bundle.putLong(Constant.VOUCHER_ID, voucherSelected!!.id)
            }
            startActivity(this@CartActivity, VoucherActivity::class.java, bundle)
        }
        tvCheckout!!.setOnClickListener {
            if (listDrinkCart == null || listDrinkCart!!.isEmpty()) return@setOnClickListener
            if (paymentMethodSelected == null) {
                showToastMessage(getString(R.string.label_choose_payment_method))
                return@setOnClickListener
            }
            if (addressSelected == null) {
                showToastMessage(getString(R.string.label_choose_address))
                return@setOnClickListener
            }
            
            // Get current location and create order (with shipping fee)
            getCurrentLocationAndCreateOrder()
        }
    }

    private fun initData() {
        listDrinkCart = ArrayList()
        listDrinkCart = getInstance(this)!!.drinkDAO()!!.listDrinkCart
        if (listDrinkCart == null || listDrinkCart!!.isEmpty()) {
            return
        }
        cartAdapter = CartAdapter(listDrinkCart, object : IClickCartListener {
            override fun onClickDeleteItem(drink: Drink?, position: Int) {
                getInstance(this@CartActivity)!!.drinkDAO()!!.deleteDrink(drink)
                listDrinkCart?.removeAt(position)
                cartAdapter!!.notifyItemRemoved(position)
                displayCountItemCart()
                calculateTotalPrice()
                EventBus.getDefault().post(DisplayCartEvent())
            }

            override fun onClickUpdateItem(drink: Drink?, position: Int) {
                getInstance(this@CartActivity)!!.drinkDAO()!!.updateDrink(drink)
                cartAdapter!!.notifyItemChanged(position)
                calculateTotalPrice()
                EventBus.getDefault().post(DisplayCartEvent())
            }

            override fun onClickEditItem(drink: Drink?) {
                val bundle = Bundle()
                bundle.putLong(Constant.DRINK_ID, drink!!.id)
                bundle.putSerializable(Constant.DRINK_OBJECT, drink)
                startActivity(this@CartActivity, DrinkDetailActivity::class.java, bundle)
            }
        })
        rcvCart!!.adapter = cartAdapter
        calculateTotalPrice()
        displayCountItemCart()
    }

    private fun displayCountItemCart() {
        val strCountItem = "(" + listDrinkCart!!.size + " " + getString(R.string.label_item) + ")"
        tvCountItem!!.text = strCountItem
    }

    private fun calculateTotalPrice() {
        if (listDrinkCart == null || listDrinkCart!!.isEmpty()) {
            val strZero = 0.toString() + Constant.CURRENCY
            priceDrink = 0
            tvPriceDrink!!.text = strZero
            mAmount = 0
            mDiscount = 0
            mDiscountLabel = null
            tvVoucher?.text = ""
            tvNameVoucher?.text = ""
            tvPriceVoucher?.text = ("-" + 0 + Constant.CURRENCY)
            tvAmount!!.text = strZero
            return
        }
        var totalPrice = 0
        for (drink in listDrinkCart!!) {
            totalPrice += drink.totalPrice
        }
        priceDrink = totalPrice
        val strPriceDrink = priceDrink.toString() + Constant.CURRENCY
        tvPriceDrink!!.text = strPriceDrink
        // Compute discount: voucher takes precedence; otherwise auto 3% for Normal rank with subtotal >= 400k
        mDiscount = 0
        mDiscountLabel = null
        if (voucherSelected != null) {
            mDiscount = voucherSelected!!.getPriceDiscount(priceDrink)
            mDiscountLabel = voucherSelected!!.title
        } else {
            val rank = user?.rankLevel ?: 0
            if (rank == 0 && priceDrink >= 400) {
                mDiscount = (priceDrink * 3) / 100
                mDiscountLabel = "Giảm 3% hạng Thường"
            }
        }
        mAmount = totalPrice - mDiscount

        // Update discount UI
        if (mDiscount > 0) {
            tvVoucher?.text = mDiscountLabel
            tvNameVoucher?.text = mDiscountLabel
            tvPriceVoucher?.text = ("-" + mDiscount + Constant.CURRENCY)
        } else {
            tvVoucher?.text = ""
            tvNameVoucher?.text = ""
            tvPriceVoucher?.text = ("-" + 0 + Constant.CURRENCY)
        }
        val strAmount = mAmount.toString() + Constant.CURRENCY
        tvAmount!!.text = strAmount
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPaymentMethodSelectedEvent(event: PaymentMethodSelectedEvent) {
        paymentMethodSelected = event.paymentMethod
        tvPaymentMethod!!.text = paymentMethodSelected!!.name
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddressSelectedEvent(event: AddressSelectedEvent) {
        addressSelected = event.address
        tvAddress!!.text = addressSelected!!.address
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVoucherSelectedEvent(event: VoucherSelectedEvent) {
        voucherSelected = event.voucher
        calculateTotalPrice()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOrderSuccessEvent(event: OrderSuccessEvent?) {
        finish()
    }
    
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                showToastMessage("Cần cấp quyền vị trí để sử dụng tính năng này")
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                }
            }
            .addOnFailureListener {
                showToastMessage("Không thể lấy vị trí hiện tại")
            }
    }
    
    private fun getCurrentLocationAndCreateOrder() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showToastMessage("Vui lòng cấp quyền vị trí để đặt hàng")
            requestLocationPermission()
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
                // Compute shipping fee by distance matrix then create order
                computeShippingAndCreateOrder()
            }
            .addOnFailureListener {
                showToastMessage("Không thể lấy vị trí. Đơn hàng sẽ được tạo không có thông tin vị trí.")
                createOrder()
            }
    }
    
    private fun computeShippingAndCreateOrder() {
        // Prefer selected address coordinates if present; else use current device location
        val destLat = addressSelected?.latitude?.takeIf { it != 0.0 } ?: currentLatitude
        val destLng = addressSelected?.longitude?.takeIf { it != 0.0 } ?: currentLongitude
        if (destLat == 0.0 && destLng == 0.0) {
            geocodeAndDistanceFallback()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val (distanceKm, fee) = withContext(Dispatchers.IO) {
                fetchDistanceAndFee(
                    Constant.STORE_LATITUDE,
                    Constant.STORE_LONGITUDE,
                    destLat,
                    destLng
                )
            }
            if (fee > 0 && distanceKm > 0) {
                createOrderWithShipping(fee, distanceKm, destLat, destLng)
            } else {
                geocodeAndDistanceFallback()
            }
        }
    }
    
    private fun fetchDistanceAndFee(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Pair<Double, Int> {
        return try {
            val origin = "$originLat,$originLng"
            val dest = "$destLat,$destLng"
            val url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=$origin&destinations=$dest&key=${Constant.GOOGLE_MAPS_API_KEY}"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return Pair(0.0, 0)
                val body = resp.body()?.string() ?: return Pair(0.0, 0)
                val root = JsonParser.parseString(body).asJsonObject
                val rows = root.getAsJsonArray("rows")
                if (rows == null || rows.size() == 0) return Pair(0.0, 0)
                val elements = rows[0].asJsonObject.getAsJsonArray("elements")
                if (elements == null || elements.size() == 0) return Pair(0.0, 0)
                val element0 = elements[0].asJsonObject
                val status = element0.get("status")?.asString ?: ""
                if (status != "OK") return Pair(0.0, 0)
                val distanceObj = element0.getAsJsonObject("distance")
                val meters = distanceObj.get("value")?.asDouble ?: 0.0
                val km = meters / 1000.0
                val fee = computeShippingFee(km)
                Pair(km, fee)
            }
        } catch (e: Exception) {
            Pair(0.0, 0)
        }
    }
    
    private fun computeShippingFee(distanceKm: Double): Int {
        if (distanceKm <= 0.0) return 0
        val blocks = ceil(distanceKm / Constant.SHIPPING_BLOCK_KM)
        return (blocks.toInt() * Constant.SHIPPING_FEE_PER_BLOCK)
    }

    private fun geocodeAndDistanceFallback() {
        val addrText = addressSelected?.address
        if (addrText.isNullOrBlank()) {
            createOrder()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val (lat, lng) = withContext(Dispatchers.IO) { geocodeAddressFor(addrText) }
            if (lat == 0.0 && lng == 0.0) {
                createOrder()
                return@launch
            }
            val distanceKm = haversineDistanceKm(
                Constant.STORE_LATITUDE,
                Constant.STORE_LONGITUDE,
                lat,
                lng
            )
            val fee = computeShippingFee(distanceKm)
            if (fee > 0) {
                createOrderWithShipping(fee, distanceKm, lat, lng)
            } else {
                createOrder()
            }
        }
    }

    private fun geocodeAddressFor(addressText: String): Pair<Double, Double> = try {
        val geocoder = Geocoder(this, Locale.getDefault())
        val results: List<Address>? = geocoder.getFromLocationName(addressText, 1)
        if (!results.isNullOrEmpty()) {
            Pair(results[0].latitude, results[0].longitude)
        } else Pair(0.0, 0.0)
    } catch (e: IOException) {
        Pair(0.0, 0.0)
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
    
    private fun createOrderWithShipping(shippingFee: Int, distanceKm: Double, destLat: Double, destLng: Double) {
        val orderBooking = Order()
        orderBooking.id = System.currentTimeMillis()
        orderBooking.userEmail = user!!.email
        orderBooking.dateTime = System.currentTimeMillis().toString()
        val drinks: MutableList<DrinkOrder> = ArrayList()
        for (drink in listDrinkCart!!) {
            drinks.add(
                DrinkOrder(
                    drink.name, drink.option, drink.count,
                    drink.priceOneDrink, drink.image
                )
            )
        }
        orderBooking.drinks = drinks
        orderBooking.price = priceDrink
        orderBooking.voucher = mDiscount
        if (voucherSelected != null) {
            orderBooking.voucherId = voucherSelected!!.id
            orderBooking.voucherCode = voucherSelected!!.code
        }
        // include shipping fee in total
        orderBooking.total = (mAmount + shippingFee)
        orderBooking.paymentMethod = paymentMethodSelected!!.name
        orderBooking.address = addressSelected
        orderBooking.status = Order.STATUS_NEW
        // Save destination coordinates actually used for delivery
        orderBooking.latitude = destLat
        orderBooking.longitude = destLng
        orderBooking.shippingFee = shippingFee
        orderBooking.distanceKm = distanceKm
        
        val bundle = Bundle()
        bundle.putSerializable(Constant.ORDER_OBJECT, orderBooking)
        // If Blockchain payment selected, go to BlockchainPaymentActivity for QR & on-chain payment
        if (paymentMethodSelected?.id == Constant.TYPE_BLOCKCHAIN) {
            startActivity(this@CartActivity, BlockchainPaymentActivity::class.java, bundle)
        } else if (paymentMethodSelected?.id == Constant.TYPE_BANK) {
            // VNPAY bank transfer: build sandbox payment URL and open SDK
            val amountVnd = orderBooking.total.toLong() * 1000L
            val orderInfo = "Order ${orderBooking.id}"
            val paymentUrl = VnPayHelper.buildPaymentUrl(
                amountVnd = amountVnd,
                orderId = orderBooking.id,
                orderInfo = orderInfo,
                returnUrl = "${Constant.VNPAY_SCHEME}://${Constant.VNPAY_RETURN_HOST}",
                baseUrl = Constant.VNPAY_BASE_URL,
                tmnCode = Constant.VNPAY_TMN_CODE,
                hashSecret = Constant.VNPAY_HASH_SECRET
            )
            // Lưu pending order để xác nhận sau khi nhận callback
            PendingOrders.put(orderBooking)
            val intent = VnPayPaymentActivity.buildIntent(
                this,
                url = paymentUrl,
                tmnCode = Constant.VNPAY_TMN_CODE,
                scheme = Constant.VNPAY_SCHEME,
                isSandbox = Constant.VNPAY_IS_SANDBOX
            )
            startActivity(intent)
        } else {
            startActivity(this@CartActivity, PaymentActivity::class.java, bundle)
        }
    }
    
    private fun createOrder() {
        val orderBooking = Order()
        orderBooking.id = System.currentTimeMillis()
        orderBooking.userEmail = user!!.email
        orderBooking.dateTime = System.currentTimeMillis().toString()
        val drinks: MutableList<DrinkOrder> = ArrayList()
        for (drink in listDrinkCart!!) {
            drinks.add(
                DrinkOrder(
                    drink.name, drink.option, drink.count,
                    drink.priceOneDrink, drink.image
                )
            )
        }
        orderBooking.drinks = drinks
        orderBooking.price = priceDrink
        orderBooking.voucher = mDiscount
        if (voucherSelected != null) {
            orderBooking.voucherId = voucherSelected!!.id
            orderBooking.voucherCode = voucherSelected!!.code
        }
        orderBooking.total = mAmount
        orderBooking.paymentMethod = paymentMethodSelected!!.name
        orderBooking.address = addressSelected
        orderBooking.status = Order.STATUS_NEW
        orderBooking.latitude = currentLatitude
        orderBooking.longitude = currentLongitude
        
        val bundle = Bundle()
        bundle.putSerializable(Constant.ORDER_OBJECT, orderBooking)
        // If Blockchain payment selected, go to BlockchainPaymentActivity for QR & on-chain payment
        if (paymentMethodSelected?.id == Constant.TYPE_BLOCKCHAIN) {
            startActivity(this@CartActivity, BlockchainPaymentActivity::class.java, bundle)
        } else if (paymentMethodSelected?.id == Constant.TYPE_BANK) {
            // VNPAY bank transfer: build sandbox payment URL and open SDK
            val amountVnd = orderBooking.total.toLong() * 1000L
            val orderInfo = "Order ${orderBooking.id}"
            val paymentUrl = VnPayHelper.buildPaymentUrl(
                amountVnd = amountVnd,
                orderId = orderBooking.id,
                orderInfo = orderInfo,
                returnUrl = "${Constant.VNPAY_SCHEME}://${Constant.VNPAY_RETURN_HOST}",
                baseUrl = Constant.VNPAY_BASE_URL,
                tmnCode = Constant.VNPAY_TMN_CODE,
                hashSecret = Constant.VNPAY_HASH_SECRET
            )
            // Lưu pending order để xác nhận sau khi nhận callback
            PendingOrders.put(orderBooking)
            val intent = VnPayPaymentActivity.buildIntent(
                this,
                url = paymentUrl,
                tmnCode = Constant.VNPAY_TMN_CODE,
                scheme = Constant.VNPAY_SCHEME,
                isSandbox = Constant.VNPAY_IS_SANDBOX
            )
            startActivity(intent)
        } else {
            startActivity(this@CartActivity, PaymentActivity::class.java, bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
