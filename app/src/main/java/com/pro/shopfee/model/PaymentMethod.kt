package com.pro.shopfee.model

class PaymentMethod {
    var id = 0
    var name: String? = null
    var description: String? = null
    var isSelected = false

    constructor()
    constructor(id: Int, name: String?, description: String?) {
        this.id = id
        this.name = name
        this.description = description
    }
}