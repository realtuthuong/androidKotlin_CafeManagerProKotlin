package com.pro.shopfee.model

import com.pro.shopfee.utils.Constant
import java.io.Serializable

class Voucher : Serializable {
    var id: Long = 0
    var discount = 0 // For percentage discount
    var fixedDiscount = 0 // For fixed amount discount
    var minimum = 0
    var isSelected = false
    var code: String = "" // Discount code
    var isFixedAmount: Boolean = false // true for fixed amount, false for percentage
    var minRankLevel: Int = 0 // 0=Thường,1=Bạc,2=Vàng,3=Kim cương
    var totalQuantity: Int = 0 // 0 = unlimited
    var usedQuantity: Int = 0

    constructor()
    
    @JvmOverloads
    constructor(id: Long, discount: Int, minimum: Int, code: String = "", isFixedAmount: Boolean = false) {
        this.id = id
        if (isFixedAmount) {
            this.fixedDiscount = discount
        } else {
            this.discount = discount
        }
        this.minimum = minimum
        this.code = code
        this.isFixedAmount = isFixedAmount
        this.minRankLevel = 0
    }

    val title: String
        get() = if (isFixedAmount) {
            "Giảm giá ${String.format("%,d", fixedDiscount) + Constant.CURRENCY}"
        } else {
            "Giảm giá $discount%"
        }
        
    val minimumText: String
        get() = if (minimum > 0) {
            "Áp dụng cho đơn hàng tối thiểu ${String.format("%,d", minimum) + Constant.CURRENCY}"
        } else "Áp dụng cho mọi đơn hàng"

    fun getCondition(amount: Int): String {
        if (minimum <= 0) return ""
        val condition = minimum - amount
        return if (condition > 0) {
            "Hãy mua thêm ${String.format("%,d", condition) + Constant.CURRENCY} để nhận được khuyến mại này"
        } else ""
    }

    fun isVoucherEnable(amount: Int): Boolean {
        if (minimum <= 0) return true
        return amount >= minimum
    }

    fun getPriceDiscount(amount: Int): Int {
        return if (isFixedAmount) {
            fixedDiscount
        } else {
            (amount * discount) / 100
        }
    }
    
    fun getFormattedDiscountText(): String {
        return if (isFixedAmount) {
            "-${String.format("%,d", fixedDiscount) + Constant.CURRENCY}"
        } else {
            "-$discount%"
        }
    }

    fun remainingQuantity(): Int {
        return if (totalQuantity <= 0) Int.MAX_VALUE else (totalQuantity - usedQuantity).coerceAtLeast(0)
    }

    fun isAvailable(): Boolean {
        return remainingQuantity() > 0
    }
}