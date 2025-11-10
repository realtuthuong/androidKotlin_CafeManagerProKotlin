package com.pro.shopfee

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pro.shopfee.prefs.DataStoreManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    private var mFirebaseDatabase: FirebaseDatabase? = null

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL)
        DataStoreManager.init(applicationContext)
    }

    fun getUserDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("users")
    }

    fun getVoucherDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("voucher")
    }

    fun getAddressDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("address")
    }

    fun getCategoryDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("category")
    }

    fun getDrinkDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("drink")
    }

    fun getDrinkDetailDatabaseReference(drinkId: Long): DatabaseReference? {
        return mFirebaseDatabase?.getReference("drink/$drinkId")
    }

    fun getToppingDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("topping")
    }

    fun getFeedbackDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/feedback")
    }

    fun getOrderDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("orders")
    }

    fun getRatingDrinkDatabaseReference(drinkId: String): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/drink/$drinkId/rating")
    }

    fun getOrderDetailDatabaseReference(orderId: Long): DatabaseReference? {
        return mFirebaseDatabase?.getReference("orders/$orderId")
    }

    companion object {
        private const val FIREBASE_URL = "https://testdoantotnghiep-1c58a-default-rtdb.firebaseio.com"
        @JvmStatic
        operator fun get(context: Context): MyApplication {
            return context.applicationContext as MyApplication
        }
    }
}