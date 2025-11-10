package com.pro.shopfee.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object VnPayHelper {
    private const val VNP_VERSION = "2.1.0"
    private const val VNP_COMMAND = "pay"
    private const val VNP_CURR_CODE = "VND"
    private const val VNP_LOCALE = "vn"
    private const val VNP_ORDER_TYPE = "other"

    fun buildPaymentUrl(
        amountVnd: Long,
        orderId: Long,
        orderInfo: String,
        returnUrl: String = "${Constant.VNPAY_SCHEME}://${Constant.VNPAY_RETURN_HOST}",
        baseUrl: String = Constant.VNPAY_BASE_URL,
        tmnCode: String = Constant.VNPAY_TMN_CODE,
        hashSecret: String = Constant.VNPAY_HASH_SECRET,
    ): String {
        val amount = amountVnd * 100 // VNPAY dùng đơn vị x100
        val createDate = now("yyyyMMddHHmmss")
        val expireDate = nowPlusMinutes("yyyyMMddHHmmss", 15)
        val txnRef = orderId.toString()
        val ipAddr = "0.0.0.0" // demo

        val params = linkedMapOf(
            "vnp_Version" to VNP_VERSION,
            "vnp_Command" to VNP_COMMAND,
            "vnp_TmnCode" to tmnCode,
            "vnp_Amount" to amount.toString(),
            "vnp_CurrCode" to VNP_CURR_CODE,
            "vnp_TxnRef" to txnRef,
            "vnp_OrderInfo" to orderInfo,
            "vnp_OrderType" to VNP_ORDER_TYPE,
            "vnp_Locale" to VNP_LOCALE,
            "vnp_ReturnUrl" to returnUrl,
            "vnp_IpAddr" to ipAddr,
            "vnp_CreateDate" to createDate,
            "vnp_ExpireDate" to expireDate,
        )

        // Tạo chuỗi query đã URL-encode theo chuẩn application/x-www-form-urlencoded của VNPAY
        val query = params.entries
            .sortedBy { it.key }
            .joinToString("&") { e ->
                urlEncode(e.key) + "=" + urlEncode(e.value)
            }

        // Chuỗi raw để ký: dùng đúng định dạng encode như query; KHÔNG bao gồm SecureHash/Type
        val rawToSign = params.entries
            .sortedBy { it.key }
            .joinToString("&") { e ->
                urlEncode(e.key) + "=" + urlEncode(e.value)
            }

        val secureHash = hmacSHA512(hashSecret, rawToSign)
        val fullUrl = "$baseUrl?$query&vnp_SecureHashType=HmacSHA512&vnp_SecureHash=$secureHash"
        return fullUrl
    }

    private fun urlEncode(v: String): String = URLEncoder.encode(v, StandardCharsets.UTF_8.name())

    private fun now(pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())

    private fun nowPlusMinutes(pattern: String, minutes: Int): String {
        val df = SimpleDateFormat(pattern, Locale.getDefault())
        val t = System.currentTimeMillis() + minutes * 60_000L
        return df.format(Date(t))
    }

    private fun hmacSHA512(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512")
        mac.init(secretKey)
        val bytes = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { b -> String.format("%02x", b) }
    }

    // ================= RETURN VERIFY =================
    fun parseQuery(query: String?): Map<String, String> {
        if (query.isNullOrEmpty()) return emptyMap()
        return query.split('&')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null else part.substring(0, idx) to part.substring(idx + 1)
            }
            .toMap()
    }

    fun verifyReturnSignature(params: Map<String, String>, secret: String): Boolean {
        if (params.isEmpty()) return false
        val secure = params["vnp_SecureHash"] ?: return false
        val filtered = params.filterKeys { it != "vnp_SecureHash" && it != "vnp_SecureHashType" }
        val raw = filtered.entries
            .sortedBy { it.key }
            .joinToString("&") { e -> e.key + "=" + e.value }
        val calc = hmacSHA512(secret, raw)
        return secure.equals(calc, ignoreCase = true)
    }
}
