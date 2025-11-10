package com.pro.shopfee.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.User
import com.pro.shopfee.utils.Constant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val application: MyApplication
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Result<User> = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fbUser = firebaseAuth.currentUser
                    if (fbUser != null) {
                        val userKey = sanitizeEmail(fbUser.email ?: email)
                        val userRef = application.getUserDatabaseReference()?.child(userKey)
                        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var userObject = snapshot.getValue(User::class.java)
                                if (userObject == null) {
                                    userObject = User(fbUser.email, password)
                                    if (fbUser.email?.contains(Constant.ADMIN_EMAIL_FORMAT) == true) {
                                        userObject.isAdmin = true
                                    }
                                    userRef.setValue(userObject)
                                } else {
                                    userObject.isAdmin = fbUser.email?.contains(Constant.ADMIN_EMAIL_FORMAT) == true
                                }
                                continuation.resume(Result.success(userObject), null)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                val userObject = User(fbUser.email, password)
                                userObject.isAdmin = fbUser.email?.contains(Constant.ADMIN_EMAIL_FORMAT) == true
                                continuation.resume(Result.success(userObject), null)
                            }
                        })
                    } else {
                        continuation.resume(Result.failure(Exception("User not found")), null)
                    }
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception("Login failed")), null)
                }
            }
    }

    fun observeUser(email: String): Flow<User?> = callbackFlow {
        val userKey = sanitizeEmail(email)
        val userRef = application.getUserDatabaseReference()?.child(userKey)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userRef?.addValueEventListener(listener)
        awaitClose { userRef?.removeEventListener(listener) }
    }

    suspend fun updateUserRankAndSpent(email: String, totalSpent: Long, rankLevel: Int): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val userKey = sanitizeEmail(email)
        val userRef = application.getUserDatabaseReference()?.child(userKey)
        userRef?.updateChildren(
            mapOf(
                "totalSpent" to totalSpent,
                "rankLevel" to rankLevel
            )
        ) { error, _ ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(error.toException()))
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun sanitizeEmail(email: String): String = email.replace(".", ",")
}

