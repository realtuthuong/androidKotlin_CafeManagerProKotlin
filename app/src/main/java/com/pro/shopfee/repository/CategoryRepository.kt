package com.pro.shopfee.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val application: MyApplication
) {
    fun observeAllCategories(): Flow<List<Category>> = callbackFlow {
        val categoryRef = application.getCategoryDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<Category>()
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(Category::class.java)
                    if (category != null) {
                        categories.add(category)
                    }
                }
                trySend(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        categoryRef?.addValueEventListener(listener)
        awaitClose { categoryRef?.removeEventListener(listener) }
    }
}

