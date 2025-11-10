package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pro.shopfee.R
import com.pro.shopfee.activity.admin.AdminMainActivity
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ goToActivity() }, 2000)
    }

    private fun goToActivity() {
        if (user != null
            && !isEmpty(user!!.email)
        ) {
            if (user!!.isAdmin) {
                startActivity(this, AdminMainActivity::class.java)
            } else {
                startActivity(this, MainActivity::class.java)
            }
        } else {
            startActivity(this, LoginActivity::class.java)
        }
        finish()
    }
}