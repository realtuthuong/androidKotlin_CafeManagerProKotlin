package com.pro.shopfee.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    private const val DEFAULT_FORMAT_DATE = "dd-MM-yyyy, hh:mm a"

    @JvmStatic
    fun convertTimeStampToDate(timeStamp: Long): String {
        var result = ""
        try {
            val sdf = SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.ENGLISH)
            val date = Date(timeStamp)
            result = sdf.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}