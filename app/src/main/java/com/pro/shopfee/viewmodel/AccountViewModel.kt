package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pro.shopfee.model.User
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(DataStoreManager.user)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            val email = DataStoreManager.user?.email ?: return@launch
            userRepository.observeUser(email).collect { user ->
                if (user != null) {
                    DataStoreManager.user = user
                    _user.value = user
                }
            }
        }
    }

    fun signOut() {
        userRepository.signOut()
        DataStoreManager.user = null
    }
}

