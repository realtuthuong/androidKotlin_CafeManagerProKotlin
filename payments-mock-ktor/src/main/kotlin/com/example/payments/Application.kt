package com.example.payments

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

fun main() {
    val port = (System.getenv("PORT") ?: "8081").toInt()
    embeddedServer(Netty, port = port) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }

    val store = PaymentStore()

    routing {
        route("/payments") {
            post {
                val apiKey = call.request.headers["X-Api-Key"]
                val expected = System.getenv("API_KEY")
                if (!expected.isNullOrEmpty() && apiKey != expected) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_api_key"))
                    return@post
                }

                val req = call.receive<CreatePaymentReq>()
                val payment = store.create(req)
                call.respond(HttpStatusCode.OK, payment.toCreateRes())
            }

            get("/{id}") {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing_id"))
                    return@get
                }
                val payment = store.get(id)
                if (payment == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
                    return@get
                }
                call.respond(HttpStatusCode.OK, payment.toStatusRes())
            }
        }

        get("/") {
            call.respond(mapOf("service" to "payments-mock-ktor", "status" to "ok"))
        }
    }
}

@Serializable
data class CreatePaymentReq(
    val amount_fiat: Double,
    val currency: String,
    val order_id: String
)

@Serializable
data class CreatePaymentRes(
    val payment_id: String,
    val status: String,
    val chain: String,
    val address_or_qr: String,
    val amount_crypto: Double,
    val expires_at: Long
)

@Serializable
data class PaymentStatusRes(
    val payment_id: String,
    val status: String,
    val tx_hash: String? = null
)

enum class PaymentStatus { PENDING, CONFIRMED, EXPIRED, FAILED }

class PaymentStore {
    private val payments = ConcurrentHashMap<String, Payment>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun create(req: CreatePaymentReq): Payment {
        val id = UUID.randomUUID().toString()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(120) // 2 minutes expiry for demo
        val address = "mock:qr:" + id.take(8)
        val amountCrypto = (req.amount_fiat / 100_000.0).let { (Math.round(it * 1e8) / 1e8) } // mock rate

        val p = Payment(
            id = id,
            orderId = req.order_id,
            amountFiat = req.amount_fiat,
            amountCrypto = amountCrypto,
            addressOrQr = address,
            status = PaymentStatus.PENDING,
            createdAt = now.epochSecond,
            expiresAt = expiresAt.epochSecond,
            txHash = null
        )
        payments[id] = p

        // Auto-confirm after 30 seconds for demo
        scope.launch {
            delay(30_000)
            payments.computeIfPresent(id) { _, old ->
                if (Instant.now().epochSecond >= old.expiresAt) {
                    old.copy(status = PaymentStatus.EXPIRED)
                } else {
                    old.copy(status = PaymentStatus.CONFIRMED, txHash = "0x" + id.take(16))
                }
            }
        }

        // Auto-expire after 2 minutes if still pending
        scope.launch {
            val wait = (expiresAt.epochSecond - Instant.now().epochSecond).coerceAtLeast(0)
            delay(wait * 1000)
            payments.computeIfPresent(id) { _, old ->
                if (old.status == PaymentStatus.PENDING) old.copy(status = PaymentStatus.EXPIRED) else old
            }
        }

        return p
    }

    fun get(id: String): Payment? = payments[id]
}

@Serializable
data class Payment(
    val id: String,
    val orderId: String,
    val amountFiat: Double,
    val amountCrypto: Double,
    val addressOrQr: String,
    val status: PaymentStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val txHash: String?
) {
    fun toCreateRes(): CreatePaymentRes = CreatePaymentRes(
        payment_id = id,
        status = status.name.lowercase(),
        chain = "mock",
        address_or_qr = addressOrQr,
        amount_crypto = amountCrypto,
        expires_at = expiresAt
    )

    fun toStatusRes(): PaymentStatusRes = PaymentStatusRes(
        payment_id = id,
        status = status.name.lowercase(),
        tx_hash = txHash
    )
}
