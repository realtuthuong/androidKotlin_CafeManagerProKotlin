package com.pro.shopfee.model

import com.google.gson.Gson

class User {
    var email: String? = null
    var password: String? = null
    var isAdmin = false
    var totalSpent: Long = 0
    var rankLevel: Int = 0 // 0=Normal,1=Silver,2=Gold,3=Diamond

    constructor()
    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

    fun toJSon(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    val rankName: String
        get() = when (rankLevel) {
            3 -> "Kim cương"
            2 -> "Vàng"
            1 -> "Bạc"
            else -> "Thường"
        }
}