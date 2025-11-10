package com.pro.shopfee.listener

import com.pro.shopfee.model.Drink

interface IClickDrinkListener {
    fun onClickDrinkItem(drink: Drink)
}