package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.PaymentMethodAdapter.PaymentMethodViewHolder
import com.pro.shopfee.model.PaymentMethod
import com.pro.shopfee.utils.Constant

class PaymentMethodAdapter(
    private val listPaymentMethod: List<PaymentMethod>?,
    private val iClickPaymentMethodListener: IClickPaymentMethodListener
) : RecyclerView.Adapter<PaymentMethodViewHolder>() {

    interface IClickPaymentMethodListener {
        fun onClickPaymentMethodItem(paymentMethod: PaymentMethod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val paymentMethod = listPaymentMethod!![position]
        when (paymentMethod.id) {
            Constant.TYPE_GOPAY -> holder.imgPaymentMethod.setImageResource(R.drawable.ic_gopay)
            Constant.TYPE_CREDIT -> holder.imgPaymentMethod.setImageResource(R.drawable.ic_credit)
            Constant.TYPE_BANK -> holder.imgPaymentMethod.setImageResource(R.drawable.ic_bank)
            Constant.TYPE_ZALO_PAY -> holder.imgPaymentMethod.setImageResource(R.drawable.ic_zalopay)
            Constant.TYPE_BLOCKCHAIN -> holder.imgPaymentMethod.setImageResource(R.drawable.ic_credit)
        }
        holder.tvName.text = paymentMethod.name
        holder.tvDescription.text = paymentMethod.description
        if (paymentMethod.isSelected) {
            holder.imgStatus.setImageResource(R.drawable.ic_item_selected)
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_item_unselect)
        }
        holder.layoutItem.setOnClickListener {
            iClickPaymentMethodListener.onClickPaymentMethodItem(
                paymentMethod
            )
        }
    }

    override fun getItemCount(): Int {
        return listPaymentMethod?.size ?: 0
    }

    class PaymentMethodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPaymentMethod: ImageView
        val imgStatus: ImageView
        val tvName: TextView
        val tvDescription: TextView
        val layoutItem: LinearLayout

        init {
            imgPaymentMethod = itemView.findViewById(R.id.img_payment_method)
            imgStatus = itemView.findViewById(R.id.img_status)
            tvName = itemView.findViewById(R.id.tv_name)
            tvDescription = itemView.findViewById(R.id.tv_description)
            layoutItem = itemView.findViewById(R.id.layout_item)
        }
    }
}