package com.pro.shopfee.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.Address
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val application: MyApplication
) {
    fun observeAddressesByUserEmail(userEmail: String): Flow<List<Address>> = callbackFlow {
        val addressRef = application.getAddressDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addresses = mutableListOf<Address>()
                for (dataSnapshot in snapshot.children) {
                    val address = dataSnapshot.getValue(Address::class.java)
                    if (address != null && address.userEmail == userEmail) {
                        addresses.add(address)
                    }
                }
                trySend(addresses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addressRef?.orderByChild("userEmail")
            ?.equalTo(userEmail)
            ?.addValueEventListener(listener)
        awaitClose { addressRef?.removeEventListener(listener) }
    }
}

