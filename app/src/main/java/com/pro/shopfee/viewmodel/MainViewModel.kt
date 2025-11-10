package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Drink
import com.pro.shopfee.repository.CartRepository
import com.pro.shopfee.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<CartState>(CartState.Empty)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.observeCartItems().collect { items ->
                if (items.isEmpty()) {
                    _cartState.value = CartState.Empty
                } else {
                    val totalPrice = items.sumOf { it.totalPrice }
                    val itemNames = items.joinToString(", ") { it.name ?: "" }
                    _cartState.value = CartState.Loaded(
                        itemCount = items.size,
                        itemNames = itemNames,
                        totalAmount = totalPrice
                    )
                }
            }
        }
    }

    sealed class CartState {
        object Empty : CartState()
        data class Loaded(
            val itemCount: Int,
            val itemNames: String,
            val totalAmount: Int
        ) : CartState()
    }
}

