package com.pro.shopfee.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkRepository @Inject constructor(
    private val application: MyApplication
) {
    fun observeAllDrinks(): Flow<List<Drink>> = callbackFlow {
        val drinkRef = application.getDrinkDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val drinks = mutableListOf<Drink>()
                for (dataSnapshot in snapshot.children) {
                    val drink = dataSnapshot.getValue(Drink::class.java)
                    if (drink != null) {
                        drinks.add(drink)
                    }
                }
                trySend(drinks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        drinkRef?.addValueEventListener(listener)
        awaitClose { drinkRef?.removeEventListener(listener) }
    }

    fun observeFeaturedDrinks(): Flow<List<Drink>> = callbackFlow {
        val drinkRef = application.getDrinkDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val drinks = mutableListOf<Drink>()
                for (dataSnapshot in snapshot.children) {
                    val drink = dataSnapshot.getValue(Drink::class.java)
                    if (drink != null && drink.isFeatured) {
                        drinks.add(drink)
                    }
                }
                trySend(drinks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        drinkRef?.addValueEventListener(listener)
        awaitClose { drinkRef?.removeEventListener(listener) }
    }

    fun observeDrinksByCategory(categoryId: Long): Flow<List<Drink>> = callbackFlow {
        val drinkRef = application.getDrinkDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val drinks = mutableListOf<Drink>()
                for (dataSnapshot in snapshot.children) {
                    val drink = dataSnapshot.getValue(Drink::class.java)
                    if (drink != null && drink.category_id == categoryId) {
                        drinks.add(drink)
                    }
                }
                trySend(drinks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        drinkRef?.orderByChild(Constant.CATEGORY_ID)
            ?.equalTo(categoryId.toDouble())
            ?.addValueEventListener(listener)
        awaitClose { drinkRef?.removeEventListener(listener) }
    }

    fun observeDrinkById(drinkId: Long): Flow<Drink?> = callbackFlow {
        val drinkRef = application.getDrinkDetailDatabaseReference(drinkId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val drink = snapshot.getValue(Drink::class.java)
                trySend(drink)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        drinkRef?.addValueEventListener(listener)
        awaitClose { drinkRef?.removeEventListener(listener) }
    }
}

