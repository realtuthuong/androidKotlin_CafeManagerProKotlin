package com.pro.shopfee.model

import java.io.Serializable

class Topping : Serializable {
    var id: Long = 0
    var name: String? = null
    var price = 0
    var isSelected = false

    constructor()
    constructor(id: Long, name: String?, price: Int) {
        this.id = id
        this.name = name
        this.price = price
    }

    companion object {
        const val VARIANT_ICE = "variant_ice"
        const val VARIANT_HOT = "variant_hot"
        const val SIZE_REGULAR = "size_regular"
        const val SIZE_MEDIUM = "size_medium"
        const val SIZE_LARGE = "size_large"
        const val SUGAR_NORMAL = "sugar_normal"
        const val SUGAR_LESS = "sugar_less"
        const val ICE_NORMAL = "ice_normal"
        const val ICE_LESS = "ice_less"
    }
}