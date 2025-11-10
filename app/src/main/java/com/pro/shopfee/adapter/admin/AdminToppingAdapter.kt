package com.pro.shopfee.adapter.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.admin.AdminToppingAdapter.AdminToppingViewHolder
import com.pro.shopfee.listener.IOnAdminManagerToppingListener
import com.pro.shopfee.model.Topping
import com.pro.shopfee.utils.Constant

class AdminToppingAdapter(
    private val mListTopping: List<Topping>?,
    private val mListener: IOnAdminManagerToppingListener
) : RecyclerView.Adapter<AdminToppingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminToppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_topping, parent, false)
        return AdminToppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminToppingViewHolder, position: Int) {
        val topping = mListTopping!![position]
        holder.tvName.text = topping.name
        val strPrice = topping.price.toString() + Constant.CURRENCY
        holder.tvPrice.text = strPrice
        holder.imgEdit.setOnClickListener { mListener.onClickUpdateTopping(topping) }
        holder.imgDelete.setOnClickListener { mListener.onClickDeleteTopping(topping) }
    }

    override fun getItemCount(): Int {
        return mListTopping?.size ?: 0
    }

    class AdminToppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView
        val tvPrice: TextView
        val imgEdit: ImageView
        val imgDelete: ImageView

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            imgEdit = itemView.findViewById(R.id.img_edit)
            imgDelete = itemView.findViewById(R.id.img_delete)
        }
    }
}