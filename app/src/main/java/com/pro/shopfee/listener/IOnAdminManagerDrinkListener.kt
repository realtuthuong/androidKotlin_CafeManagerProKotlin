package com.pro.shopfee.listener

import com.pro.shopfee.model.Drink

interface IOnAdminManagerDrinkListener {
    fun onClickUpdateDrink(drink: Drink?)
    fun onClickDeleteDrink(drink: Drink?)
}