package com.pro.shopfee.model

import java.io.Serializable

class Category : Serializable {
    var id: Long = 0
    var name: String? = null

    constructor()
    constructor(id: Long, name: String?) {
        this.id = id
        this.name = name
    }
}