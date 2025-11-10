package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.User
import com.pro.shopfee.repository.UserRepository
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.utils.StringUtil.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isButtonEnabled = MutableStateFlow(false)
    val isButtonEnabled: StateFlow<Boolean> = _isButtonEnabled.asStateFlow()

    fun validateInput(email: String, password: String) {
        _isButtonEnabled.value = !isEmpty(email) && !isEmpty(password)
    }

    fun validateLogin(email: String, password: String, isAdmin: Boolean): ValidationResult {
        if (isEmpty(email)) {
            return ValidationResult.Error("Email không được để trống")
        }
        if (isEmpty(password)) {
            return ValidationResult.Error("Mật khẩu không được để trống")
        }
        if (!isValidEmail(email)) {
            return ValidationResult.Error("Email không hợp lệ")
        }
        if (isAdmin && !email.contains(Constant.ADMIN_EMAIL_FORMAT)) {
            return ValidationResult.Error("Email admin không hợp lệ")
        }
        if (!isAdmin && email.contains(Constant.ADMIN_EMAIL_FORMAT)) {
            return ValidationResult.Error("Email người dùng không hợp lệ")
        }
        return ValidationResult.Success
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = userRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = LoginState.Success(user)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Đăng nhập thất bại")
                }
            )
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}

