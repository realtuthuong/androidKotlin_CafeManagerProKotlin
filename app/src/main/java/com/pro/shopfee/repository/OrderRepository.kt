package com.pro.shopfee.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.Order
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val application: MyApplication
) {
    fun observeAllOrders(): Flow<List<Order>> = callbackFlow {
        val orderRef = application.getOrderDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        orders.add(order)
                    }
                }
                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        orderRef?.addValueEventListener(listener)
        awaitClose { orderRef?.removeEventListener(listener) }
    }

    fun observeOrdersByUserEmail(userEmail: String): Flow<List<Order>> = callbackFlow {
        val orderRef = application.getOrderDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)
                    if (order != null && order.userEmail == userEmail) {
                        orders.add(order)
                    }
                }
                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        orderRef?.orderByChild("userEmail")
            ?.equalTo(userEmail)
            ?.addValueEventListener(listener)
        awaitClose { orderRef?.removeEventListener(listener) }
    }

    fun observeOrderById(orderId: Long): Flow<Order?> = callbackFlow {
        val orderRef = application.getOrderDetailDatabaseReference(orderId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java)
                trySend(order)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        orderRef?.addValueEventListener(listener)
        awaitClose { orderRef?.removeEventListener(listener) }
    }

    suspend fun createOrder(order: Order): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val orderRef = application.getOrderDatabaseReference()
            ?.child(order.id.toString())
        orderRef?.setValue(order) { error, _ ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(error.toException()))
            }
        }
    }

    private fun createOrderCopy(
        id: Long,
        userEmail: String?,
        dateTime: String?,
        drinks: List<com.pro.shopfee.model.DrinkOrder>?,
        price: Int,
        voucher: Int,
        voucherId: Long,
        voucherCode: String?,
        total: Int,
        paymentMethod: String?,
        status: Int,
        rate: Double,
        review: String?,
        address: com.pro.shopfee.model.Address?,
        latitude: Double,
        longitude: Double,
        shippingFee: Int,
        distanceKm: Double,
        paymentStatus: String?,
        paymentTxHash: String?,
        paymentAmountWei: String?,
        paymentChainId: Long
    ): Order {
        return Order().apply {
            this.id = id
            this.userEmail = userEmail
            this.dateTime = dateTime
            this.drinks = drinks
            this.price = price
            this.voucher = voucher
            this.voucherId = voucherId
            this.voucherCode = voucherCode
            this.total = total
            this.paymentMethod = paymentMethod
            this.status = status
            this.rate = rate
            this.review = review
            this.address = address
            this.latitude = latitude
            this.longitude = longitude
            this.shippingFee = shippingFee
            this.distanceKm = distanceKm
            this.paymentStatus = paymentStatus
            this.paymentTxHash = paymentTxHash
            this.paymentAmountWei = paymentAmountWei
            this.paymentChainId = paymentChainId
        }
    }

    suspend fun reserveVoucherAndCreateOrder(
        order: Order,
        voucherId: Long
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        // Create a copy of order to avoid capture issues - copy all fields first
        val orderId = order.id
        val orderUserEmail = order.userEmail
        val orderDateTime = order.dateTime
        val orderDrinks = order.drinks
        val orderPrice = order.price
        val orderVoucher = order.voucher
        val orderVoucherId = order.voucherId
        val orderVoucherCode = order.voucherCode
        val orderTotal = order.total
        val orderPaymentMethod = order.paymentMethod
        val orderStatus = order.status
        val orderRate = order.rate
        val orderReview = order.review
        val orderAddress = order.address
        val orderLatitude = order.latitude
        val orderLongitude = order.longitude
        val orderShippingFee = order.shippingFee
        val orderDistanceKm = order.distanceKm
        val orderPaymentStatus = order.paymentStatus
        val orderPaymentTxHash = order.paymentTxHash
        val orderPaymentAmountWei = order.paymentAmountWei
        val orderPaymentChainId = order.paymentChainId
        
        val voucherRef = application.getVoucherDatabaseReference()?.child(voucherId.toString())
        if (voucherRef == null) {
            continuation.resume(Result.failure(Exception("Voucher reference not found")))
            return@suspendCancellableCoroutine
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
                val orderToCreate = if (committed) {
                    createOrderCopy(
                        orderId, orderUserEmail, orderDateTime, orderDrinks, orderPrice,
                        orderVoucher, orderVoucherId, orderVoucherCode, orderTotal,
                        orderPaymentMethod, orderStatus, orderRate, orderReview,
                        orderAddress, orderLatitude, orderLongitude, orderShippingFee,
                        orderDistanceKm, orderPaymentStatus, orderPaymentTxHash,
                        orderPaymentAmountWei, orderPaymentChainId
                    )
                } else {
                    // Voucher out of stock - create order without voucher
                    createOrderCopy(
                        orderId, orderUserEmail, orderDateTime, orderDrinks, orderPrice,
                        0, 0, null, orderPrice,
                        orderPaymentMethod, orderStatus, orderRate, orderReview,
                        orderAddress, orderLatitude, orderLongitude, orderShippingFee,
                        orderDistanceKm, orderPaymentStatus, orderPaymentTxHash,
                        orderPaymentAmountWei, orderPaymentChainId
                    )
                }
                
                // Create order directly using Firebase API (not suspend function)
                val finalOrderRef = application.getOrderDatabaseReference()
                    ?.child(orderToCreate.id.toString())
                finalOrderRef?.setValue(orderToCreate) { dbError, _ ->
                    if (dbError == null) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        continuation.resume(Result.failure(dbError.toException()))
                    }
                }
            }
        })
    }

    suspend fun updateOrderStatus(orderId: Long, status: Int): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val orderRef = application.getOrderDetailDatabaseReference(orderId)
        orderRef?.child("status")?.setValue(status) { error, _ ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(error.toException()))
            }
        }
    }

    suspend fun updateOrderRating(orderId: Long, rate: Double, review: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val orderRef = application.getOrderDetailDatabaseReference(orderId)
        orderRef?.updateChildren(
            mapOf(
                "rate" to rate,
                "review" to review
            )
        ) { error, _ ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(error.toException()))
            }
        }
    }
}

