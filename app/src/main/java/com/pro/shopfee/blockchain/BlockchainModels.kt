package com.pro.shopfee.blockchain

data class RpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: List<Any?>,
    val id: Int = 1
)

data class RpcResponse<T>(val result: T?)

data class EthTx(val to: String?, val value: String?)

data class EthReceipt(val status: String?)

data class EthBlock(val transactions: List<EthTransaction>?)

data class EthTransaction(
    val hash: String?,
    val to: String?,
    val value: String?
)