package com.pro.shopfee.prefs

import android.content.Context
import com.google.gson.Gson
import com.pro.shopfee.model.User
import com.pro.shopfee.utils.StringUtil.isEmpty

class DataStoreManager {
    private var sharedPreferences: MySharedPreferences? = null

    companion object {
        private const val PREF_USER_INFO = "PREF_USER_INFO"
        private var instance: DataStoreManager? = null
        fun init(context: Context) {
            instance = DataStoreManager()
            instance!!.sharedPreferences = MySharedPreferences(context)
        }

        fun getInstance(): DataStoreManager? {
            return if (instance != null) {
                instance
            } else {
                throw IllegalStateException("Not initialized")
            }
        }

        @JvmStatic
        var user: User?
            get() {
                val jsonUser = getInstance()!!.sharedPreferences!!.getStringValue(PREF_USER_INFO)
                return if (!isEmpty(jsonUser)) {
                    Gson().fromJson(
                        jsonUser,
                        User::class.java
                    )
                } else User()
            }
            set(user) {
                var jsonUser: String? = ""
                if (user != null) {
                    jsonUser = user.toJSon()
                }
                getInstance()!!.sharedPreferences?.putStringValue(PREF_USER_INFO, jsonUser)
            }
    }
}