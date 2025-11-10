package com.pro.shopfee.repository

import com.pro.shopfee.database.DrinkDAO
import com.pro.shopfee.model.Drink
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val drinkDAO: DrinkDAO
) {
    fun observeCartItems(): Flow<List<Drink>> = flow {
        while (true) {
            val items = drinkDAO.listDrinkCart ?: emptyList()
            emit(items)
            delay(500) // Poll every 500ms for changes
        }
    }

    fun addToCart(drink: Drink) {
        drinkDAO.insertDrink(drink)
    }

    fun updateCartItem(drink: Drink) {
        drinkDAO.updateDrink(drink)
    }

    fun removeFromCart(drink: Drink) {
        drinkDAO.deleteDrink(drink)
    }

    fun clearCart() {
        drinkDAO.deleteAllDrink()
    }

    fun getCartItems(): List<Drink> {
        return drinkDAO.listDrinkCart ?: emptyList()
    }
}

