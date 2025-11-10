package com.pro.shopfee.model

import java.io.Serializable

class DrinkOrder : Serializable {
    var name: String? = null
    var option: String? = null
    var count = 0
    var price = 0
    var image: String? = null

    constructor()
    constructor(name: String?, option: String?, count: Int, price: Int, image: String?) {
        this.name = name
        this.option = option
        this.count = count
        this.price = price
        this.image = image
    }
}