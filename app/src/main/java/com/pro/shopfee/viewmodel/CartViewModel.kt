package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Address
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.PaymentMethod
import com.pro.shopfee.model.Voucher
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<Drink>>(emptyList())
    val cartItems: StateFlow<List<Drink>> = _cartItems.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()

    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()

    private val _selectedVoucher = MutableStateFlow<Voucher?>(null)
    val selectedVoucher: StateFlow<Voucher?> = _selectedVoucher.asStateFlow()

    private val _cartSummary = MutableStateFlow<CartSummary>(CartSummary())
    val cartSummary: StateFlow<CartSummary> = _cartSummary.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.observeCartItems().collect { items ->
                _cartItems.value = items
                calculateSummary(items)
            }
        }
    }

    fun setPaymentMethod(paymentMethod: PaymentMethod) {
        _selectedPaymentMethod.value = paymentMethod
        calculateSummary(_cartItems.value)
    }

    fun setAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun setVoucher(voucher: Voucher?) {
        _selectedVoucher.value = voucher
        calculateSummary(_cartItems.value)
    }

    fun removeCartItem(drink: Drink, position: Int) {
        cartRepository.removeFromCart(drink)
    }

    fun updateCartItem(drink: Drink) {
        cartRepository.updateCartItem(drink)
    }

    fun createOrder(shippingFee: Int = 0, distanceKm: Double = 0.0, latitude: Double = 0.0, longitude: Double = 0.0): Order? {
        val items = _cartItems.value
        if (items.isEmpty()) return null

        val order = Order()
        order.id = System.currentTimeMillis()
        order.userEmail = DataStoreManager.user?.email
        order.dateTime = System.currentTimeMillis().toString()
        order.drinks = items.map { drink ->
            com.pro.shopfee.model.DrinkOrder(
                drink.name,
                drink.option,
                drink.count,
                drink.priceOneDrink,
                drink.image
            )
        }
        order.price = _cartSummary.value.subtotal
        order.voucher = _cartSummary.value.discount
        if (_selectedVoucher.value != null) {
            order.voucherId = _selectedVoucher.value!!.id
            order.voucherCode = _selectedVoucher.value!!.code
        }
        order.total = _cartSummary.value.total + shippingFee
        order.paymentMethod = _selectedPaymentMethod.value?.name
        order.address = _selectedAddress.value
        order.status = Order.STATUS_NEW
        order.latitude = latitude
        order.longitude = longitude
        order.shippingFee = shippingFee
        order.distanceKm = distanceKm

        return order
    }

    private fun calculateSummary(items: List<Drink>) {
        if (items.isEmpty()) {
            _cartSummary.value = CartSummary()
            return
        }

        val subtotal = items.sumOf { it.totalPrice }
        val user = DataStoreManager.user
        val rank = user?.rankLevel ?: 0

        var discount = 0
        var discountLabel: String? = null

        if (_selectedVoucher.value != null) {
            discount = _selectedVoucher.value!!.getPriceDiscount(subtotal)
            discountLabel = _selectedVoucher.value!!.title
        } else if (rank == 0 && subtotal >= 400) {
            discount = (subtotal * 3) / 100
            discountLabel = "Giảm 3% hạng Thường"
        }

        val total = subtotal - discount

        _cartSummary.value = CartSummary(
            itemCount = items.size,
            subtotal = subtotal,
            discount = discount,
            discountLabel = discountLabel,
            total = total
        )
    }

    data class CartSummary(
        val itemCount: Int = 0,
        val subtotal: Int = 0,
        val discount: Int = 0,
        val discountLabel: String? = null,
        val total: Int = 0
    )
}

