package com.pro.shopfee.blockchain

import com.pro.shopfee.utils.Constant
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TransactionPoller(
    private val expectedWei: BigDecimal,
    private val onTransactionFound: (String) -> Unit
) {
    private var pollingJob: Job? = null
    private var isActive = false

    fun start() {
        isActive = true
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    delay(10000) // Poll every 10 seconds
                    if (!isActive) break

                    val txHash = checkIncomingTransaction()
                    if (txHash != null) {
                        withContext(Dispatchers.Main) {
                            onTransactionFound(txHash)
                        }
                        stop()
                        break
                    }
                } catch (e: Exception) {
                    // Continue polling on error
                }
            }
        }
    }

    fun stop() {
        isActive = false
        pollingJob?.cancel()
    }

    private suspend fun checkIncomingTransaction(): String? {
        try {
            val api = createSepoliaRetrofit().create(SepoliaRpcApi::class.java)
            val receiverAddress = Constant.BLOCKCHAIN_RECEIVER_ADDRESS.lowercase()
            val expectedAmount = expectedWei.setScale(0, RoundingMode.DOWN).toBigInteger()

            if (expectedAmount <= BigInteger.ZERO) return null

            // Get latest block
            val blockResp = api.getLatestBlock(
                RpcRequest(method = "eth_blockNumber", params = emptyList())
            )
            val latestBlockHex = blockResp.result ?: return null
            val latestBlock = hexToBigInteger(latestBlockHex)

            // Check last 10 blocks
            for (i in 0..9) {
                val blockNum = latestBlock - BigInteger.valueOf(i.toLong())
                if (blockNum < BigInteger.ZERO) break

                val blockHex = "0x${blockNum.toString(16)}"
                val blockResp = api.getBlockByNumber(
                    RpcRequest(method = "eth_getBlockByNumber", params = listOf(blockHex, true))
                )

                val transactions = blockResp.result?.transactions ?: continue

                for (tx in transactions) {
                    val txTo = tx.to?.lowercase() ?: continue
                    val txValue = hexToBigInteger(tx.value)

                    if (txTo == receiverAddress && txValue >= expectedAmount) {
                        return tx.hash
                    }
                }
            }
        } catch (e: Exception) {
            // Log if needed
        }
        return null
    }

    private fun createSepoliaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://rpc.sepolia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun hexToBigInteger(hex: String?): BigInteger {
        if (hex.isNullOrEmpty()) return BigInteger.ZERO
        val clean = if (hex.startsWith("0x")) hex.substring(2) else hex
        return if (clean.isEmpty()) BigInteger.ZERO else BigInteger(clean, 16)
    }
}