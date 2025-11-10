package com.pro.shopfee.utils

/**
 * Application Constants
 * 
 * Note: API keys and sensitive information have been moved to ApiKeys.kt
 * ApiKeys.kt is in .gitignore and will not be committed to version control.
 * 
 * All API keys are accessed through ApiKeys object.
 */
object Constant {
    const val CURRENCY = ".000vnd"
    const val CATEGORY_ID = "category_id"
    const val DRINK_ID = "drink_id"
    const val PAYMENT_METHOD_ID = "payment_method_id"
    const val VOUCHER_ID = "voucher_id"
    const val ADDRESS_ID = "address_id"
    const val DRINK_OBJECT = "drink_object"
    const val ORDER_OBJECT = "order_object"
    const val ORDER_ID = "order_id"
    const val ORDER_TAB_TYPE = "order_tab_type"
    const val RATING_REVIEW_OBJECT = "rating_review_object"
    const val AMOUNT_VALUE = "amount_value"
    const val TYPE_GOPAY = 1
    const val TYPE_CREDIT = 2
    const val TYPE_BANK = 3
    const val TYPE_ZALO_PAY = 4
    // Blockchain (Sepolia) payment type
    const val TYPE_BLOCKCHAIN = 5
    const val ADMIN_EMAIL_FORMAT = "@admin.com"
    const val KEY_INTENT_VOUCHER_OBJECT = "voucher_object"
    const val KEY_INTENT_TOPPING_OBJECT = "color_object"
    const val KEY_INTENT_CATEGORY_OBJECT = "category_object"
    const val KEY_INTENT_DRINK_OBJECT = "drink_object"
    const val BLOCKCHAIN_RECEIVER_ADDRESS = ApiKeys.BLOCKCHAIN_RECEIVER_ADDRESS
    const val ETHEREUM_SEPOLIA_CHAIN_ID = 11155111
    const val BLOCKCHAIN_RPC_BASE_URL = ApiKeys.BLOCKCHAIN_RPC_BASE_URL
    
    // Demo mode: mark blockchain payments as successful immediately
    const val BLOCKCHAIN_DEMO_AUTO_SUCCESS = false
    // If true, accept any positive on-chain amount to receiver (skip strict amount check)
    const val BLOCKCHAIN_ACCEPT_ANY_POSITIVE_AMOUNT = true
    
    // Auto-poll configuration for blockchain payment detection
    const val BLOCKCHAIN_POLL_INTERVAL_MS = 3_000
    const val BLOCKCHAIN_POLL_LOOKBACK_BLOCKS = 500
    // Accept transactions that are within this tolerance below expected wei amount (basis points)
    // Example: 50 bps = 0.5%
    const val BLOCKCHAIN_AMOUNT_TOLERANCE_BPS = 500
   
    // VNPAY sandbox constants
    const val VNPAY_TMN_CODE = ApiKeys.VNPAY_TMN_CODE
    const val VNPAY_HASH_SECRET = ApiKeys.VNPAY_HASH_SECRET
    const val VNPAY_BASE_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    const val VNPAY_SCHEME = "shopfee"
    const val VNPAY_RETURN_HOST = "vnpay-result"
    const val VNPAY_IS_SANDBOX = true

    // Google Distance Matrix API
    const val GOOGLE_MAPS_API_KEY = ApiKeys.GOOGLE_MAPS_API_KEY
    const val STORE_LATITUDE = 16.032192640426718
    const val STORE_LONGITUDE = 108.21948517589183
    const val SHIPPING_BLOCK_KM = 3.0
    // Unit aligned with app pricing (x.000vnd). 5 means 5,000 VND per block.
    const val SHIPPING_FEE_PER_BLOCK = 5
}