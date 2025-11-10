package com.pro.shopfee.model

class TabOrder(var type: Int, var name: String) {

    companion object {
        const val TAB_ORDER_PROCESS = 1
        const val TAB_ORDER_DONE = 2
    }
}