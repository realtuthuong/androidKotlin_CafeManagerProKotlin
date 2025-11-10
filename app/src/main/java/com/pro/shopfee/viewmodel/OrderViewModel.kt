package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.TabOrder
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun loadOrders(orderTabType: Int) {
        viewModelScope.launch {
            val user = DataStoreManager.user
            val flow = if (user?.isAdmin == true) {
                orderRepository.observeAllOrders()
            } else {
                orderRepository.observeOrdersByUserEmail(user?.email ?: "")
            }

            flow.collect { allOrders ->
                val filteredOrders = allOrders.filter { order ->
                    when (orderTabType) {
                        TabOrder.TAB_ORDER_PROCESS -> order.status != Order.STATUS_COMPLETE
                        TabOrder.TAB_ORDER_DONE -> order.status == Order.STATUS_COMPLETE
                        else -> true
                    }
                }.sortedByDescending { it.id }
                _orders.value = filteredOrders
            }
        }
    }
}

