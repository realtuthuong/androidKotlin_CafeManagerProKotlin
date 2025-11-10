package com.pro.shopfee.utils

import com.pro.shopfee.model.Order
import java.util.concurrent.ConcurrentHashMap

object PendingOrders {
    private val store = ConcurrentHashMap<Long, Order>()

    fun put(order: Order) {
        store[order.id] = order
    }

    fun get(id: Long): Order? = store[id]

    fun remove(id: Long): Order? = store.remove(id)
}
