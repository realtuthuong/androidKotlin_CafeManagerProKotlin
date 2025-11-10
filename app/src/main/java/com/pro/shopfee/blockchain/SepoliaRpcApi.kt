package com.pro.shopfee.blockchain

import retrofit2.http.Body
import retrofit2.http.POST

interface SepoliaRpcApi {
    @POST(".")
    suspend fun getTx(@Body body: RpcRequest): RpcResponse<EthTx>

    @POST(".")
    suspend fun getReceipt(@Body body: RpcRequest): RpcResponse<EthReceipt>

    @POST(".")
    suspend fun getLatestBlock(@Body body: RpcRequest): RpcResponse<String>

    @POST(".")
    suspend fun getBlockByNumber(@Body body: RpcRequest): RpcResponse<EthBlock>
}