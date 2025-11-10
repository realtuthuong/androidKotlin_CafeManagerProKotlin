package com.pro.shopfee.listener

import com.pro.shopfee.model.Topping

interface IOnAdminManagerToppingListener {
    fun onClickUpdateTopping(topping: Topping)
    fun onClickDeleteTopping(topping: Topping)
}