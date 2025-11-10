package com.pro.shopfee.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pro.shopfee.MyApplication
import com.pro.shopfee.database.DrinkDatabase
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.event.OrderSuccessEvent
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.PendingOrders
import com.pro.shopfee.utils.VnPayHelper
import org.greenrobot.eventbus.EventBus
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.DataSnapshot
import com.pro.shopfee.model.Order

class PaymentResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent?.data
        try {
            val params = VnPayHelper.parseQuery(data?.query)
            Log.d("VNPAY", "deeplink=" + data?.toString())
            val okSig = VnPayHelper.verifyReturnSignature(params, Constant.VNPAY_HASH_SECRET)
            val resp = params["vnp_ResponseCode"]
            val txnRef = params["vnp_TxnRef"]
            if (okSig && resp == "00" && !txnRef.isNullOrEmpty()) {
                val orderId = txnRef.toLongOrNull()
                val order = if (orderId != null) PendingOrders.remove(orderId) else null
                if (order != null) {
                    // Mark paid and save
                    order.paymentStatus = "paid_vnpay"
                    order.paymentTxHash = params["vnp_TransactionNo"] ?: ""
                    reserveVoucherThenSave(order)
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("VNPAY", "handle result error", e)
        }
        finish()
    }

    private fun reserveVoucherThenSave(order: Order) {
        if (order.voucherId <= 0) {
            saveOrder(order)
            return
        }
        val voucherRef = MyApplication[this].getVoucherDatabaseReference()
            ?.child(order.voucherId.toString()) ?: run {
            saveOrder(order)
            return
        }
        voucherRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val total = (currentData.child("totalQuantity").getValue(Int::class.java) ?: 0)
                val used = (currentData.child("usedQuantity").getValue(Int::class.java) ?: 0)
                if (total > 0 && used >= total) {
                    return Transaction.abort()
                }
                currentData.child("usedQuantity").value = used + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (!committed) {
                    // Out of stock after payment: still save order but without voucher
                    order.voucher = 0
                    order.voucherId = 0
                    order.voucherCode = null
                    order.total = order.price
                }
                saveOrder(order)
            }
        })
    }

    private fun saveOrder(order: Order) {
        MyApplication[this].getOrderDatabaseReference()
            ?.child(order.id.toString())
            ?.setValue(order) { _: DatabaseError?, _: DatabaseReference? ->
                // Update user's totalSpent and rank in Firebase
                updateUserRankAndSpent(order.total.toLong())

                DrinkDatabase.getInstance(this)!!.drinkDAO()!!.deleteAllDrink()
                EventBus.getDefault().post(DisplayCartEvent())
                EventBus.getDefault().post(OrderSuccessEvent())
                val b = Bundle()
                b.putLong(Constant.ORDER_ID, order.id)
                com.pro.shopfee.utils.GlobalFunction.startActivity(
                    this@PaymentResultActivity,
                    ReceiptOrderActivity::class.java,
                    b
                )
                finish()
            }
    }

    private fun updateUserRankAndSpent(orderTotal: Long) {
        val currentUser = com.pro.shopfee.prefs.DataStoreManager.user ?: return
        val email = currentUser.email ?: return
        val userKey = sanitizeEmail(email)
        val newTotal = (currentUser.totalSpent) + orderTotal
        val baseRank = calculateRankLevel(newTotal)
        var newRank = baseRank
        if (orderTotal >= 500L && newRank < 2) {
            newRank = 2
        }
        if (newRank < currentUser.rankLevel) {
            newRank = currentUser.rankLevel
        }

        currentUser.totalSpent = newTotal
        currentUser.rankLevel = newRank
        com.pro.shopfee.prefs.DataStoreManager.user = currentUser

        MyApplication[this].getUserDatabaseReference()
            ?.child(userKey)
            ?.updateChildren(mapOf(
                "totalSpent" to newTotal,
                "rankLevel" to newRank
            ))
    }

    private fun calculateRankLevel(totalSpent: Long): Int {
        return when {
            totalSpent >= 5_000L -> 3 // Kim cương
            totalSpent >= 2_000L -> 2 // Vàng
            totalSpent >= 500L -> 1 // Bạc
            else -> 0 // Thường
        }
    }

    private fun sanitizeEmail(email: String): String = email.replace(".", ",")
}
