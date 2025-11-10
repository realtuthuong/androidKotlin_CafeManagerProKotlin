package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.User
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.repository.CartRepository
import com.pro.shopfee.repository.OrderRepository
import com.pro.shopfee.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun createOrder(order: Order) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val result = if (order.voucherId > 0) {
                orderRepository.reserveVoucherAndCreateOrder(order, order.voucherId)
            } else {
                orderRepository.createOrder(order)
            }

            result.fold(
                onSuccess = {
                    // Clear cart
                    cartRepository.clearCart()
                    // Update user rank and spent
                    updateUserRankAndSpent(order.total.toLong())
                    _paymentState.value = PaymentState.Success(order.id)
                },
                onFailure = { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Tạo đơn hàng thất bại")
                }
            )
        }
    }

    private fun updateUserRankAndSpent(orderTotal: Long) {
        viewModelScope.launch {
            val currentUser = DataStoreManager.user ?: return@launch
            val email = currentUser.email ?: return@launch
            val newTotal = currentUser.totalSpent + orderTotal
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
            DataStoreManager.user = currentUser

            userRepository.updateUserRankAndSpent(email, newTotal, newRank)
        }
    }

    private fun calculateRankLevel(totalSpent: Long): Int {
        return when {
            totalSpent >= 5_000L -> 3 // Kim cương
            totalSpent >= 2_000L -> 2 // Vàng
            totalSpent >= 500L -> 1 // Bạc
            else -> 0 // Thường
        }
    }

    sealed class PaymentState {
        object Idle : PaymentState()
        object Loading : PaymentState()
        data class Success(val orderId: Long) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }
}

