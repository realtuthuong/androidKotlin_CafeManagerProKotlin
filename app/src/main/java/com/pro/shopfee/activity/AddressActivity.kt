package com.pro.shopfee.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.AddressAdapter
import com.pro.shopfee.adapter.AddressAdapter.IClickAddressListener
import com.pro.shopfee.event.AddressSelectedEvent
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.GlobalFunction.showToastMessage
import com.pro.shopfee.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.Locale

class AddressActivity : BaseActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
    }

    private var listAddress: MutableList<com.pro.shopfee.model.Address>? = null
    private var addressAdapter: AddressAdapter? = null
    private var addressSelectedId: Long = 0
    private var mValueEventListener: ValueEventListener? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentAddressText: String = ""
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        loadDataIntent()
        initToolbar()
        initUi()
        loadListAddressFromFirebase()
        requestLocationPermission()
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        addressSelectedId = bundle.getLong(Constant.ADDRESS_ID, 0)
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.address_title)
    }

    private fun initUi() {
        val rcvAddress = findViewById<RecyclerView>(R.id.rcv_address)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvAddress.layoutManager = linearLayoutManager
        listAddress = ArrayList()
        addressAdapter = AddressAdapter(
            listAddress,
            object : IClickAddressListener {
                override fun onClickAddressItem(address: com.pro.shopfee.model.Address) {
                    handleClickAddress(address)
                }
            })
        rcvAddress.adapter = addressAdapter
        val btnAddAddress = findViewById<Button>(R.id.btn_add_address)
        btnAddAddress.setOnClickListener { onClickAddAddress() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListAddressFromFirebase() {
        showProgressDialog(true)
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressDialog(false)
                resetListAddress()
                for (dataSnapshot in snapshot.children) {
                    val address = dataSnapshot.getValue(
                        com.pro.shopfee.model.Address::class.java
                    )
                    if (address != null) {
                        listAddress!!.add(0, address)
                    }
                }
                if (addressSelectedId > 0 && listAddress != null && listAddress!!.isNotEmpty()) {
                    for (address in listAddress!!) {
                        if (address.id == addressSelectedId) {
                            address.isSelected = true
                            break
                        }
                    }
                }
                if (addressAdapter != null) addressAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressDialog(false)
                showToastMessage(getString(R.string.msg_get_date_error))
            }
        }
        MyApplication[this].getAddressDatabaseReference()
            ?.orderByChild("userEmail")
            ?.equalTo(user!!.email)
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun resetListAddress() {
        if (listAddress != null) {
            listAddress!!.clear()
        } else {
            listAddress = ArrayList()
        }
    }

    private fun handleClickAddress(address: com.pro.shopfee.model.Address) {
        EventBus.getDefault().post(AddressSelectedEvent(address))
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
            getCurrentLocationAddress()
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
                getCurrentLocationAddress()
            }
        }
    }

    private fun getCurrentLocationAddress() {
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
                    getAddressFromLocation(it.latitude, it.longitude)
                }
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    // Số nhà và tên đường
                    if (address.subThoroughfare != null) {
                        append(address.subThoroughfare)
                        append(" ")
                    }
                    if (address.thoroughfare != null) {
                        append(address.thoroughfare)
                        append(", ")
                    }
                    // Phường/Xã
                    if (address.subLocality != null) {
                        append(address.subLocality)
                        append(", ")
                    }
                    // Quận/Huyện
                    if (address.subAdminArea != null) {
                        append(address.subAdminArea)
                        append(", ")
                    }
                    // Tỉnh/Thành phố
                    if (address.adminArea != null) {
                        append(address.adminArea)
                    }
                    // Quốc gia
                    if (address.countryName != null && address.countryName != "Vietnam") {
                        append(", ")
                        append(address.countryName)
                    }
                }
                
                currentAddressText = addressText.trim().trimEnd(',')
            }
        } catch (e: IOException) {
            e.printStackTrace()
            currentAddressText = "Không thể lấy địa chỉ từ vị trí hiện tại"
        }
    }

    @SuppressLint("InflateParams, MissingInflatedId")
    fun onClickAddAddress() {
        val viewDialog = layoutInflater.inflate(R.layout.layout_bottom_sheet_add_address, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(viewDialog)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // init ui
        val edtName = viewDialog.findViewById<TextView>(R.id.edt_name)
        val edtPhone = viewDialog.findViewById<TextView>(R.id.edt_phone)
        val edtAddress = viewDialog.findViewById<TextView>(R.id.edt_address)
        val tvCancel = viewDialog.findViewById<TextView>(R.id.tv_cancel)
        val tvAdd = viewDialog.findViewById<TextView>(R.id.tv_add)

        // Tự động điền địa chỉ từ GPS
        if (currentAddressText.isNotEmpty()) {
            edtAddress.text = currentAddressText
        }

        // Set listener
        tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        tvAdd.setOnClickListener {
            val strName = edtName.text.toString().trim { it <= ' ' }
            val strPhone = edtPhone.text.toString().trim { it <= ' ' }
            val strAddress = edtAddress.text.toString().trim { it <= ' ' }
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(this, getString(R.string.message_enter_infor))
            } else {
                val id = System.currentTimeMillis()
                val address = com.pro.shopfee.model.Address(id, strName, strPhone, strAddress, user!!.email)

                // Determine latitude/longitude for the address being saved
                var lat = 0.0
                var lng = 0.0
                try {
                    // If user uses the auto-filled current location text and we have GPS coords, use them
                    if (currentAddressText.isNotEmpty() && currentAddressText == strAddress &&
                        currentLatitude != 0.0 && currentLongitude != 0.0
                    ) {
                        lat = currentLatitude
                        lng = currentLongitude
                    } else {
                        // Geocode the typed address
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address>? = geocoder.getFromLocationName(strAddress, 1)
                        if (!list.isNullOrEmpty()) {
                            lat = list[0].latitude
                            lng = list[0].longitude
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                address.latitude = lat
                address.longitude = lng
                MyApplication[this].getAddressDatabaseReference()
                    ?.child(id.toString())
                    ?.setValue(address) { _: DatabaseError?, _: DatabaseReference? ->
                        showToastMessage(
                            this,
                            getString(R.string.msg_add_address_success)
                        )
                        GlobalFunction.hideSoftKeyboard(this)
                        bottomSheetDialog.dismiss()
                    }
            }
        }
        bottomSheetDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mValueEventListener?.let {
            MyApplication[this].getAddressDatabaseReference()?.removeEventListener(it)
        }
    }
}
