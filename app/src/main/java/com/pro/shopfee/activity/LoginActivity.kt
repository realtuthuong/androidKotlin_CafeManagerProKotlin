package com.pro.shopfee.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.pro.shopfee.R
import com.pro.shopfee.activity.admin.AdminMainActivity
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private var btnLogin: Button? = null
    private var layoutRegister: LinearLayout? = null
    private var tvForgotPassword: TextView? = null
    private var rdbAdmin: RadioButton? = null
    private var rdbUser: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        initUi()
        initListener()
        observeViewModel()
    }

    private fun initUi() {
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLogin = findViewById(R.id.btn_login)
        layoutRegister = findViewById(R.id.layout_register)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        rdbAdmin = findViewById(R.id.rdb_admin)
        rdbUser = findViewById(R.id.rdb_user)
    }

    private fun initListener() {
        rdbUser!!.isChecked = true
        edtEmail!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val email = s.toString()
                val password = edtPassword!!.text.toString().trim()
                viewModel.validateInput(email, password)
                updateEmailBackground(!email.isEmpty())
            }
        })
        edtPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val email = edtEmail!!.text.toString().trim()
                val password = s.toString()
                viewModel.validateInput(email, password)
                updatePasswordBackground(!password.isEmpty())
            }
        })
        layoutRegister!!.setOnClickListener {
            startActivity(
                this,
                RegisterActivity::class.java
            )
        }
        btnLogin!!.setOnClickListener { onClickValidateLogin() }
        tvForgotPassword!!.setOnClickListener {
            startActivity(
                this,
                ForgotPasswordActivity::class.java
            )
        }
    }

    private fun updateEmailBackground(isValid: Boolean) {
        edtEmail!!.setBackgroundResource(
            if (isValid) R.drawable.bg_white_corner_16_border_main
            else R.drawable.bg_white_corner_16_border_gray
        )
    }

    private fun updatePasswordBackground(isValid: Boolean) {
        edtPassword!!.setBackgroundResource(
            if (isValid) R.drawable.bg_white_corner_16_border_main
            else R.drawable.bg_white_corner_16_border_gray
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isButtonEnabled.collect { enabled ->
                btnLogin!!.setBackgroundResource(
                    if (enabled) R.drawable.bg_button_enable_corner_16
                    else R.drawable.bg_button_disable_corner_16
                )
            }
        }

        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginViewModel.LoginState.Idle -> {
                        // Do nothing
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        showProgressDialog(true)
                    }
                    is LoginViewModel.LoginState.Success -> {
                        showProgressDialog(false)
                        DataStoreManager.user = state.user
                        goToMainActivity(state.user.isAdmin)
                    }
                    is LoginViewModel.LoginState.Error -> {
                        showProgressDialog(false)
                        showToastMessage(state.message)
                    }
                }
            }
        }
    }

    private fun onClickValidateLogin() {
        val strEmail = edtEmail!!.text.toString().trim()
        val strPassword = edtPassword!!.text.toString().trim()
        val isAdmin = rdbAdmin!!.isChecked

        val validationResult = viewModel.validateLogin(strEmail, strPassword, isAdmin)
        when (validationResult) {
            is LoginViewModel.ValidationResult.Error -> {
                showToastMessage(validationResult.message)
            }
            is LoginViewModel.ValidationResult.Success -> {
                viewModel.login(strEmail, strPassword)
            }
        }
    }

    private fun goToMainActivity(isAdmin: Boolean) {
        if (isAdmin) {
            startActivity(this, AdminMainActivity::class.java)
        } else {
            startActivity(this, MainActivity::class.java)
        }
        finishAffinity()
    }
}