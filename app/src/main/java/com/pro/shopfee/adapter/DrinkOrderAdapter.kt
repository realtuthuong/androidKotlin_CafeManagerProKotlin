package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.DrinkOrderAdapter.DrinkOrderViewHolder
import com.pro.shopfee.model.DrinkOrder
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlideUtils.loadUrl

class DrinkOrderAdapter(private val listDrinkOrder: List<DrinkOrder>?) :
    RecyclerView.Adapter<DrinkOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink_order, parent, false)
        return DrinkOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkOrderViewHolder, position: Int) {
        val drinkOrder = listDrinkOrder!![position]
        loadUrl(drinkOrder.image, holder.imgDrink)
        holder.tvName.text = drinkOrder.name
        val strPrice = drinkOrder.price.toString() + Constant.CURRENCY
        holder.tvPrice.text = strPrice
        holder.tvOption.text = drinkOrder.option
        val strCount = "x" + drinkOrder.count
        holder.tvCount.text = strCount
    }

    override fun getItemCount(): Int {
        return listDrinkOrder?.size ?: 0
    }

    class DrinkOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDrink: ImageView
        val tvName: TextView
        val tvPrice: TextView
        val tvCount: TextView
        val tvOption: TextView

        init {
            imgDrink = itemView.findViewById(R.id.img_drink)
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            tvCount = itemView.findViewById(R.id.tv_count)
            tvOption = itemView.findViewById(R.id.tv_option)
        }
    }
}