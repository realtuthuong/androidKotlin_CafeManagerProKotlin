package com.pro.shopfee.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.model.Voucher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val application: MyApplication
) {
    fun observeAllVouchers(): Flow<List<Voucher>> = callbackFlow {
        val voucherRef = application.getVoucherDatabaseReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vouchers = mutableListOf<Voucher>()
                for (dataSnapshot in snapshot.children) {
                    val voucher = dataSnapshot.getValue(Voucher::class.java)
                    if (voucher != null) {
                        vouchers.add(voucher)
                    }
                }
                trySend(vouchers)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        voucherRef?.addValueEventListener(listener)
        awaitClose { voucherRef?.removeEventListener(listener) }
    }
}

