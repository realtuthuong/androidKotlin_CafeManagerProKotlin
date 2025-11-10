package com.pro.shopfee.listener

import com.pro.shopfee.model.Voucher

interface IOnAdminManagerVoucherListener {
    fun onClickUpdateVoucher(voucher: Voucher)
    fun onClickDeleteVoucher(voucher: Voucher)
}