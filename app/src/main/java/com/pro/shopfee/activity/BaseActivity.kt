package com.pro.shopfee.activity

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.pro.shopfee.R

abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: MaterialDialog? = null
    private var alertDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** ---------------------- HỘP THOẠI TIẾN TRÌNH ---------------------- **/
    private fun ensureProgressDialog() {
        if (progressDialog == null) {
            progressDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.msg_waiting_message)
                .progress(true, 0)
                .cancelable(false)
                .build()
        }
    }

    fun showProgressDialog(show: Boolean) {
        ensureProgressDialog()
        if (show) {
            if (progressDialog?.isShowing != true) progressDialog?.show()
        } else {
            if (progressDialog?.isShowing == true) progressDialog?.dismiss()
        }
    }

    fun dismissProgressDialog() {
        progressDialog?.dismiss()
        alertDialog?.dismiss()
    }

    /** ---------------------- HỘP THOẠI CẢNH BÁO ---------------------- **/
    private fun ensureAlertDialog() {
        if (alertDialog == null) {
            alertDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content("")
                .positiveText(R.string.action_ok)
                .cancelable(false)
                .build()
        }
    }

    fun showAlertDialog(errorMessage: String?) {
        ensureAlertDialog()
        alertDialog?.setContent(errorMessage)
        alertDialog?.show()
    }

    fun showAlertDialog(@StringRes resourceId: Int) {
        ensureAlertDialog()
        alertDialog?.setContent(resourceId)
        alertDialog?.show()
    }

    /** ---------------------- CÁC HÀM TIỆN ÍCH ---------------------- **/
    fun setCancelProgress(isCancelable: Boolean) {
        progressDialog?.setCancelable(isCancelable)
    }

    fun showToastMessage(message: String?) {
        Toast.makeText(this, message ?: "", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        alertDialog?.dismiss()
        super.onDestroy()
    }
}
