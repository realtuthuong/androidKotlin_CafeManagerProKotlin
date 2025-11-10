package com.pro.shopfee.model

class Filter {
    var id = 0
    var name: String? = null
    var isSelected = false

    constructor()
    constructor(id: Int, name: String?) {
        this.id = id
        this.name = name
    }

    companion object {
        const val TYPE_FILTER_ALL = 1
        const val TYPE_FILTER_RATE = 2
        const val TYPE_FILTER_PRICE = 3
        const val TYPE_FILTER_PROMOTION = 4
    }
}