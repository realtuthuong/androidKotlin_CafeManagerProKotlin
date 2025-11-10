package com.pro.shopfee.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.VoucherAdapter.VoucherViewHolder
import com.pro.shopfee.model.Voucher
import com.pro.shopfee.utils.StringUtil.isEmpty

class VoucherAdapter(
    private var context: Context?,
    private val listVoucher: List<Voucher>?,
    private val amount: Int,
    private val iClickVoucherListener: IClickVoucherListener
) : RecyclerView.Adapter<VoucherViewHolder>() {

    interface IClickVoucherListener {
        fun onClickVoucherItem(voucher: Voucher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = listVoucher!![position]
        holder.tvVoucherTitle.text = voucher.title
        
        // Display voucher code
        if (!isEmpty(voucher.code)) {
            holder.tvVoucherCode.visibility = View.VISIBLE
            holder.tvVoucherCode.text = "Mã: ${voucher.code}"
        } else {
            holder.tvVoucherCode.visibility = View.GONE
        }
        
        holder.tvVoucherMinimum.text = voucher.minimumText
        if (isEmpty(voucher.getCondition(amount))) {
            holder.tvVoucherCondition.visibility = View.GONE
        } else {
            holder.tvVoucherCondition.visibility = View.VISIBLE
            holder.tvVoucherCondition.text = voucher.getCondition(amount)
        }
        val enabledByAmount = voucher.isVoucherEnable(amount)
        val inStock = voucher.isAvailable()
        val selectable = enabledByAmount && inStock
        if (selectable) {
            holder.imgStatus.visibility = View.VISIBLE
            holder.tvVoucherTitle.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.textColorHeading
                )
            )
            holder.tvVoucherMinimum.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.textColorSecondary
                )
            )
        } else {
            holder.imgStatus.visibility = View.GONE
            holder.tvVoucherTitle.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.textColorAccent
                )
            )
            holder.tvVoucherMinimum.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.textColorAccent
                )
            )
            if (!inStock) {
                holder.tvVoucherCondition.visibility = View.VISIBLE
                holder.tvVoucherCondition.text = "Đã hết lượt"
            }
        }
        if (voucher.isSelected) {
            holder.imgStatus.setImageResource(R.drawable.ic_item_selected)
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_item_unselect)
        }
        holder.layoutItem.setOnClickListener {
            if (!selectable) return@setOnClickListener
            iClickVoucherListener.onClickVoucherItem(voucher)
        }
    }

    override fun getItemCount(): Int {
        return listVoucher?.size ?: 0
    }

    fun release() {
        if (context != null) context = null
    }

    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgStatus: ImageView
        val tvVoucherTitle: TextView
        val tvVoucherCode: TextView
        val tvVoucherMinimum: TextView
        val tvVoucherCondition: TextView
        val layoutItem: LinearLayout

        init {
            imgStatus = itemView.findViewById(R.id.img_status)
            tvVoucherTitle = itemView.findViewById(R.id.tv_voucher_title)
            tvVoucherCode = itemView.findViewById(R.id.tv_voucher_code)
            tvVoucherMinimum = itemView.findViewById(R.id.tv_voucher_minimum)
            tvVoucherCondition = itemView.findViewById(R.id.tv_voucher_condition)
            layoutItem = itemView.findViewById(R.id.layout_item)
        }
    }
}