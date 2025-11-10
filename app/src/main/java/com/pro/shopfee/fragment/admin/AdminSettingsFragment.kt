package com.pro.shopfee.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.pro.shopfee.R
import com.pro.shopfee.activity.LoginActivity
import com.pro.shopfee.activity.admin.AdminFeedbackActivity
import com.pro.shopfee.activity.admin.AdminToppingActivity
import com.pro.shopfee.activity.admin.AdminVoucherActivity
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.GlobalFunction.startActivity

class AdminSettingsFragment : Fragment() {

    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_admin_settings, container, false)
        setupScreen()
        return mView
    }

    private fun setupScreen() {
        val tvEmail = mView!!.findViewById<TextView>(R.id.tv_email)
        tvEmail.text = user!!.email
        mView!!.findViewById<View>(R.id.tv_manage_topping)
            .setOnClickListener { onClickManageProductColor() }
        mView!!.findViewById<View>(R.id.tv_manage_voucher)
            .setOnClickListener { onClickManageVoucher() }
        mView!!.findViewById<View>(R.id.tv_manage_feedback)
            .setOnClickListener { onClickManageFeedback() }
        mView!!.findViewById<View>(R.id.tv_sign_out)
            .setOnClickListener { onClickSignOut() }
    }

    private fun onClickManageProductColor() {
        startActivity(activity!!, AdminToppingActivity::class.java)
    }

    private fun onClickManageVoucher() {
        startActivity(activity!!, AdminVoucherActivity::class.java)
    }

    private fun onClickManageFeedback() {
        startActivity(activity!!, AdminFeedbackActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) return
        FirebaseAuth.getInstance().signOut()
        user = null
        startActivity(activity!!, LoginActivity::class.java)
        activity!!.finishAffinity()
    }
}