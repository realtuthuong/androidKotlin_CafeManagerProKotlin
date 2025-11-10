package com.pro.shopfee.model

import java.io.Serializable

class Rating : Serializable {
    var review: String? = null
    var rate = 0.0

    constructor()
    constructor(review: String?, rate: Double) {
        this.review = review
        this.rate = rate
    }
}