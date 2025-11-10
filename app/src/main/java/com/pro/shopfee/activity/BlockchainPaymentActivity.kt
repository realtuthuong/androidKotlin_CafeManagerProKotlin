package com.pro.shopfee.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.annotation.WorkerThread
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.pro.shopfee.R
import com.pro.shopfee.model.Order
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.MyApplication
import com.pro.shopfee.database.DrinkDatabase
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.event.OrderSuccessEvent
import org.greenrobot.eventbus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal
import java.math.RoundingMode
import java.math.BigInteger
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

class BlockchainPaymentActivity : BaseActivity() {

    private var order: Order? = null

    private lateinit var tvAmountVnd: TextView
    private lateinit var tvAmountEth: TextView
    private lateinit var imgQr: ImageView
    private lateinit var btnOpenWallet: Button
    private lateinit var btnConfirmPaid: Button
    private lateinit var progress: ProgressBar

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // Auto-polling variables
    private var autoPollActive = false
    private var autoPollJob: Job? = null
    private var lastWeiAmount: BigDecimal? = null
    private var startBlock: java.math.BigInteger? = null
    private var lastCheckedBlock: java.math.BigInteger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blockchain_payment)
        initUi()
        loadOrderFromIntent()
        if (order == null) {
            finish()
            return
        }
        fetchPriceAndRender()
    }

    private fun initUi() {
        tvAmountVnd = findViewById(R.id.tv_amount_vnd)
        tvAmountEth = findViewById(R.id.tv_amount_eth)
        imgQr = findViewById(R.id.img_qr)
        btnOpenWallet = findViewById(R.id.btn_open_wallet)
        btnConfirmPaid = findViewById(R.id.btn_confirm_paid)
        progress = findViewById(R.id.progress_bar)

        findViewById<ImageView>(R.id.img_toolbar_back)?.setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_toolbar_title)?.text = getString(R.string.title_blockchain_payment)

        btnOpenWallet.setOnClickListener {
            val uri = buildPaymentUriString(lastWeiAmount ?: BigDecimal.ZERO)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
        // Fully automatic flow: no manual input required
        if (!Constant.BLOCKCHAIN_DEMO_AUTO_SUCCESS) {
            btnConfirmPaid.visibility = View.GONE
        } else {
            btnConfirmPaid.setOnClickListener { completeDemoAndFinish() }
        }
    }

    private fun loadOrderFromIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            order = bundle.getSerializable(Constant.ORDER_OBJECT) as? Order
        }
    }

    private fun fetchPriceAndRender() {
        progress.visibility = View.VISIBLE
        uiScope.launch {
            try {
                val vndAmount = (order?.total ?: 0) * 1000L
                val priceVndPerEth = withContext(Dispatchers.IO) { fetchEthVndPrice() }
                val ethAmount = if (priceVndPerEth > BigDecimal.ZERO) {
                    BigDecimal(vndAmount).divide(priceVndPerEth, 18, RoundingMode.HALF_UP)
                } else BigDecimal.ZERO
                val wei = ethAmount.multiply(BigDecimal("1000000000000000000"))
                lastWeiAmount = wei

                tvAmountVnd.text = String.format("%,d VND", vndAmount)
                tvAmountEth.text = "~ ${ethAmount.stripTrailingZeros().toPlainString()} ETH"

                val uri = buildPaymentUriString(wei)
                val bitmap = withContext(Dispatchers.Default) { generateQr(uri) }
                imgQr.setImageBitmap(bitmap)

                if (Constant.BLOCKCHAIN_DEMO_AUTO_SUCCESS) {
                    // In demo mode, mark as paid immediately after showing QR
                    completeDemoAndFinish()
                }
            } catch (e: Exception) {
                tvAmountEth.text = getString(R.string.error_loading_rate)
            } finally {
                progress.visibility = View.GONE
                // Always ensure auto-polling is running in non-demo mode, even if price fetch failed
                if (!Constant.BLOCKCHAIN_DEMO_AUTO_SUCCESS && order != null && !autoPollActive) {
                    startAutoPoll()
                }
            }
        }
    }

    private fun completeDemoAndFinish() {
        val currentOrder = order ?: return
        // Update basic payment fields to reflect demo success
        currentOrder.paymentStatus = "paid_demo"
        currentOrder.paymentTxHash = "demo"
        val wei = (lastWeiAmount ?: BigDecimal.ZERO).setScale(0, RoundingMode.DOWN)
        currentOrder.paymentAmountWei = wei.toPlainString()
        currentOrder.paymentChainId = Constant.ETHEREUM_SEPOLIA_CHAIN_ID.toLong()

        setUiBusy(true)
        uiScope.launch {
            reserveVoucherThenSave(currentOrder)
            setUiBusy(false)
        }
    }

    private fun buildPaymentUriString(weiAmount: BigDecimal): String {
        val to = Constant.BLOCKCHAIN_RECEIVER_ADDRESS
        val chainId = Constant.ETHEREUM_SEPOLIA_CHAIN_ID
        val clean = weiAmount.setScale(0, RoundingMode.DOWN).toPlainString()
        val message = Uri.encode("Order ${order?.id}")
        return "ethereum:$to@$chainId?value=$clean&message=$message"
    }

    @WorkerThread
    private fun generateQr(content: String, size: Int = 768): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)
        return bmp
    }

    // ==================== AUTO-POLLING LOGIC ====================

    private fun startAutoPoll() {
        autoPollActive = true
        autoPollJob = uiScope.launch {
            if (startBlock == null) {
                withContext(Dispatchers.IO) {
                    try {
                        val api = createSepoliaRetrofit().create(SepoliaRpcApi::class.java)
                        val blockResp = api.getLatestBlock(
                            RpcRequest(method = "eth_blockNumber", params = emptyList())
                        )
                        val latestBlockHex = blockResp.result
                        if (!latestBlockHex.isNullOrEmpty()) {
                            val latest = hexToBigInteger(latestBlockHex)
                            startBlock = latest
                            lastCheckedBlock = latest.subtract(java.math.BigInteger.ONE)
                        }
                    } catch (_: Exception) { }
                }
            }
            while (autoPollActive) {
                try {
                    val foundTxHash = withContext(Dispatchers.IO) { checkIncomingTransaction() }

                    if (foundTxHash != null) {
                        autoPollActive = false
                        // Auto-verify the found transaction
                        verifyOnChainAndComplete(foundTxHash)
                        break
                    }
                    // Wait before next check
                    delay(Constant.BLOCKCHAIN_POLL_INTERVAL_MS.toLong())
                    if (!autoPollActive) break
                } catch (e: Exception) {
                    // Continue polling on error
                }
            }
        }
    }

    @WorkerThread
    private suspend fun checkIncomingTransaction(): String? {
        try {
            val api = createSepoliaRetrofit().create(SepoliaRpcApi::class.java)
            val receiverAddress = Constant.BLOCKCHAIN_RECEIVER_ADDRESS.lowercase()
            val expectedWei = (lastWeiAmount ?: BigDecimal.ZERO)
                .setScale(0, RoundingMode.DOWN).toBigInteger()
            val bps = Constant.BLOCKCHAIN_AMOUNT_TOLERANCE_BPS
            val toleranceWei = expectedWei.multiply(BigInteger.valueOf(bps.toLong())).divide(BigInteger.valueOf(10000))
            val acceptAny = Constant.BLOCKCHAIN_ACCEPT_ANY_POSITIVE_AMOUNT || expectedWei <= BigInteger.ZERO
            val minWei = if (!acceptAny) expectedWei.subtract(toleranceWei) else BigInteger.ONE

            val blockResp = api.getLatestBlock(
                RpcRequest(method = "eth_blockNumber", params = emptyList())
            )
            val latestBlockHex = blockResp.result ?: return null
            val latestBlock = hexToBigInteger(latestBlockHex)

            val fromBlock = ((lastCheckedBlock ?: latestBlock).add(BigInteger.ONE)).max(BigInteger.ZERO)
            val maxScan = BigInteger.valueOf(Constant.BLOCKCHAIN_POLL_LOOKBACK_BLOCKS.toLong())
            val endBlock = if (latestBlock.subtract(fromBlock) > maxScan) fromBlock.add(maxScan) else latestBlock

            var blockNum = fromBlock
            while (blockNum <= endBlock) {
                val blockHex = "0x${blockNum.toString(16)}"
                val blockResp = api.getBlockByNumber(
                    RpcRequest(method = "eth_getBlockByNumber", params = listOf(blockHex, true))
                )

                val transactions = blockResp.result?.transactions ?: continue

                for (tx in transactions) {
                    val txTo = tx.to?.lowercase() ?: continue
                    val txValue = hexToBigInteger(tx.value)

                    if (txTo == receiverAddress && txValue >= minWei) {
                        return tx.hash
                    }
                }
                blockNum = blockNum.add(BigInteger.ONE)
            }
            lastCheckedBlock = endBlock
        } catch (e: Exception) {
            // Log error if needed
        }
        return null
    }

    // ==================== MANUAL VERIFICATION ====================

    private fun promptAndVerifyTransaction() {
        val input = EditText(this)
        input.hint = "0x..."
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_blockchain_payment))
            .setMessage("Nhập mã giao dịch (tx hash) Sepolia sau khi bạn đã chuyển tiền")
            .setView(input)
            .setPositiveButton("Xác nhận") { d, _ ->
                val tx = input.text?.toString()?.trim()
                if (!tx.isNullOrEmpty()) {
                    verifyOnChainAndComplete(tx)
                }
                d.dismiss()
            }
            .setNegativeButton("Hủy") { d, _ -> d.dismiss() }
            .show()
    }

    private fun setUiBusy(busy: Boolean) {
        progress.visibility = if (busy) View.VISIBLE else View.GONE
        btnOpenWallet.isEnabled = !busy
        btnConfirmPaid.isEnabled = !busy
    }

    private fun verifyOnChainAndComplete(txHash: String) {
        val currentOrder = order ?: return
        val expectedTo = Constant.BLOCKCHAIN_RECEIVER_ADDRESS.lowercase()
        val expectedWei = (lastWeiAmount ?: BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).toBigInteger()
        // Allow same tolerance as polling to avoid rejecting minor rounding/slippage
        val bps = Constant.BLOCKCHAIN_AMOUNT_TOLERANCE_BPS
        val toleranceWei = expectedWei.multiply(BigInteger.valueOf(bps.toLong())).divide(BigInteger.valueOf(10000))
        val minWei = expectedWei.subtract(toleranceWei)

        setUiBusy(true)
        uiScope.launch {
            try {
                val api = createSepoliaRetrofit().create(SepoliaRpcApi::class.java)
                val txResp = withContext(Dispatchers.IO) {
                    api.getTx(RpcRequest(method = "eth_getTransactionByHash", params = listOf(txHash)))
                }
                val receiptResp = withContext(Dispatchers.IO) {
                    api.getReceipt(RpcRequest(method = "eth_getTransactionReceipt", params = listOf(txHash)))
                }

                val tx = txResp.result
                val receipt = receiptResp.result
                val okStatus = receipt?.status?.lowercase() == "0x1"
                val okTo = tx?.to?.lowercase() == expectedTo
                val onChainValue = hexToBigInteger(tx?.value)
                val acceptAny = Constant.BLOCKCHAIN_ACCEPT_ANY_POSITIVE_AMOUNT || expectedWei <= BigInteger.ZERO
                val okAmount = if (!acceptAny) onChainValue >= minWei else onChainValue > BigInteger.ZERO

                if (okStatus && okTo && okAmount) {
                    // Update order payment info
                    currentOrder.paymentStatus = "paid"
                    currentOrder.paymentTxHash = txHash
                    currentOrder.paymentAmountWei = onChainValue.toString(10)
                    currentOrder.paymentChainId = Constant.ETHEREUM_SEPOLIA_CHAIN_ID.toLong()

                    reserveVoucherThenSave(currentOrder)
                } else {
                    tvAmountEth.text = "Giao dịch không hợp lệ. Số tiền phải khớp chính xác với QR."
                }
            } catch (e: Exception) {
                tvAmountEth.text = "Lỗi xác thực giao dịch: ${e.message}"
            } finally {
                setUiBusy(false)
            }
        }
    }

    private fun reserveVoucherThenSave(currentOrder: Order) {
        if (currentOrder.voucherId <= 0) {
            saveOrder(currentOrder)
            return
        }
        val voucherRef = MyApplication[this].getVoucherDatabaseReference()
            ?.child(currentOrder.voucherId.toString()) ?: run {
            saveOrder(currentOrder)
            return
        }
        voucherRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val total = (currentData.child("totalQuantity").getValue(Int::class.java) ?: 0)
                val used = (currentData.child("usedQuantity").getValue(Int::class.java) ?: 0)
                if (total > 0 && used >= total) {
                    return com.google.firebase.database.Transaction.abort()
                }
                currentData.child("usedQuantity").value = used + 1
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {
                if (!committed) {
                    // Out of stock: save without voucher
                    currentOrder.voucher = 0
                    currentOrder.voucherId = 0
                    currentOrder.voucherCode = null
                    currentOrder.total = currentOrder.price
                }
                saveOrder(currentOrder)
            }
        })
    }

    private fun saveOrder(currentOrder: Order) {
        MyApplication[this]
            .getOrderDatabaseReference()
            ?.child(currentOrder.id.toString())
            ?.setValue(currentOrder) { _: com.google.firebase.database.DatabaseError?, _: com.google.firebase.database.DatabaseReference? ->
                // Clear cart and navigate to receipt
                updateUserRankAndSpent(currentOrder.total.toLong())
                DrinkDatabase.getInstance(this@BlockchainPaymentActivity)!!.drinkDAO()!!.deleteAllDrink()
                EventBus.getDefault().post(DisplayCartEvent())
                EventBus.getDefault().post(OrderSuccessEvent())
                val bundle = Bundle()
                bundle.putLong(Constant.ORDER_ID, currentOrder.id)
                startActivity(this@BlockchainPaymentActivity, ReceiptOrderActivity::class.java, bundle)
                finish()
            }
    }

    private fun updateUserRankAndSpent(orderTotal: Long) {
        val currentUser = com.pro.shopfee.prefs.DataStoreManager.user ?: return
        val email = currentUser.email ?: return
        val userKey = sanitizeEmail(email)
        val newTotal = (currentUser.totalSpent) + orderTotal
        val baseRank = calculateRankLevel(newTotal)
        var newRank = baseRank
        if (orderTotal >= 500L && newRank < 2) {
            newRank = 2
        }
        if (newRank < currentUser.rankLevel) {
            newRank = currentUser.rankLevel
        }

        currentUser.totalSpent = newTotal
        currentUser.rankLevel = newRank
        com.pro.shopfee.prefs.DataStoreManager.user = currentUser

        MyApplication[this].getUserDatabaseReference()
            ?.child(userKey)
            ?.updateChildren(mapOf(
                "totalSpent" to newTotal,
                "rankLevel" to newRank
            ))
    }

    private fun calculateRankLevel(totalSpent: Long): Int {
        return when {
            totalSpent >= 5_000L -> 3 // Kim cương
            totalSpent >= 2_000L -> 2 // Vàng
            totalSpent >= 500L -> 1 // Bạc
            else -> 0 // Thường
        }
    }

    private fun sanitizeEmail(email: String): String = email.replace(".", ",")

    // ==================== BLOCKCHAIN API MODELS ====================

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

    private fun createSepoliaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BLOCKCHAIN_RPC_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun hexToBigInteger(hex: String?): BigInteger {
        if (hex.isNullOrEmpty()) return BigInteger.ZERO
        val clean = if (hex.startsWith("0x")) hex.substring(2) else hex
        return if (clean.isEmpty()) BigInteger.ZERO else BigInteger(clean, 16)
    }

    // ==================== PRICE API ====================

    interface CoinGeckoApi {
        @GET("simple/price")
        suspend fun getPrice(
            @Query("ids") ids: String = "ethereum",
            @Query("vs_currencies") vs: String = "vnd"
        ): Map<String, Map<String, Double>>
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @WorkerThread
    private suspend fun fetchEthVndPrice(): BigDecimal {
        val api = createRetrofit().create(CoinGeckoApi::class.java)
        val res = api.getPrice()
        val vnd = res["ethereum"]?.get("vnd") ?: 0.0
        return BigDecimal(vnd).setScale(0, RoundingMode.HALF_UP)
    }

    // ==================== LIFECYCLE ====================

    override fun onDestroy() {
        super.onDestroy()
        autoPollActive = false
        autoPollJob?.cancel()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        // If user returned from wallet, ensure polling continues
        if (!Constant.BLOCKCHAIN_DEMO_AUTO_SUCCESS && order != null && !autoPollActive) {
            startAutoPoll()
        }
    }
}