package com.pro.shopfee.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Proxy

class VnPayPaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL)
        val tmn = intent.getStringExtra(EXTRA_TMN)
        val scheme = intent.getStringExtra(EXTRA_SCHEME)
        val sandbox = intent.getBooleanExtra(EXTRA_SANDBOX, true)
        if (url.isNullOrBlank() || tmn.isNullOrBlank() || scheme.isNullOrBlank()) {
            finish()
            return
        }
        try {
            // Register SDK callback via reflection to avoid direct SDK imports
            tryRegisterCallback()
            val cls = Class.forName(DEFAULT_SDK_CLASS)
            val i = Intent().setComponent(ComponentName(this, cls))
            i.putExtra("url", url)
            i.putExtra("tmn_code", tmn)
            i.putExtra("scheme", scheme)
            i.putExtra("is_sandbox", sandbox)
            startActivity(i)
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "VNPAY SDK class not found: $DEFAULT_SDK_CLASS", e)
        } catch (e: Throwable) {
            Log.e(TAG, "VNPAY start failed", e)
        } finally {
            finish()
        }
    }

    private fun tryRegisterCallback() {
        try {
            val authClazz = Class.forName(DEFAULT_SDK_CLASS)
            val cbIface = Class.forName(CALLBACK_IFACE)
            val method = authClazz.getMethod("setSdkCompletedCallback", cbIface)

            val handler = java.lang.reflect.InvocationHandler { _, m, args ->
                if (m.name == "sdkAction" && args != null && args.isNotEmpty()) {
                    val action = args[0] as? String
                    Log.w(TAG, "VNPAY action: $action")
                    when (action) {
                        "AppBackAction" -> Unit
                        "CallMobileBankingApp" -> Unit
                        "WebBackAction" -> Unit
                        "FaildBackAction" -> Unit
                        "SuccessBackAction" -> Unit
                    }
                }
                null
            }
            val proxy = Proxy.newProxyInstance(cbIface.classLoader, arrayOf(cbIface), handler)
            method.invoke(null, proxy)
        } catch (_: Throwable) {
            // Ignore if reflection fails; SDK may still proceed
        }
    }

    companion object {
        private const val TAG = "VNPAY"
        private const val DEFAULT_SDK_CLASS = "com.vnpay.authentication.VNP_AuthenticationActivity"
        private const val CALLBACK_IFACE = "com.vnpay.authentication.VNP_SdkCompletedCallback"
        const val EXTRA_URL = "vnp_url"
        const val EXTRA_TMN = "vnp_tmn"
        const val EXTRA_SCHEME = "vnp_scheme"
        const val EXTRA_SANDBOX = "vnp_sandbox"

        fun buildIntent(
            host: AppCompatActivity,
            url: String,
            tmnCode: String,
            scheme: String,
            isSandbox: Boolean
        ): Intent {
            return Intent(host, VnPayPaymentActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TMN, tmnCode)
                putExtra(EXTRA_SCHEME, scheme)
                putExtra(EXTRA_SANDBOX, isSandbox)
            }
        }
    }
}
