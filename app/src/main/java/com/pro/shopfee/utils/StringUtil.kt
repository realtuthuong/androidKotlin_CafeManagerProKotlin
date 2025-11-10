package com.pro.shopfee.utils

import android.util.Patterns

object StringUtil {
    @JvmStatic
    fun isEmpty(input: String?): Boolean {
        return input == null || input.isEmpty() || "" == input.trim { it <= ' ' }
    }

    @JvmStatic
    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) false else Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}