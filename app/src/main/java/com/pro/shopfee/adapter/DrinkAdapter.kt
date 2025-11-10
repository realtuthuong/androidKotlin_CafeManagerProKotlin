package com.pro.shopfee.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.DrinkAdapter.DrinkViewHolder
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlideUtils.loadUrl

class DrinkAdapter(
    private val listDrink: List<Drink>?,
    private val iClickDrinkListener: IClickDrinkListener
) : RecyclerView.Adapter<DrinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink, parent, false)
        return DrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val drink = listDrink!![position]
        loadUrl(drink.image, holder.imgDrink)
        holder.tvName.text = drink.name
        holder.tvDescription.text = drink.description
        holder.tvRate.text = drink.rate.toString()
        if (drink.sale <= 0) {
            holder.tvPrice.visibility = View.GONE
            val strPrice = drink.price.toString() + Constant.CURRENCY
            holder.tvPriceSale.text = strPrice
        } else {
            holder.tvPrice.visibility = View.VISIBLE
            val strOldPrice = drink.price.toString() + Constant.CURRENCY
            holder.tvPrice.text = strOldPrice
            holder.tvPrice.paintFlags = holder.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice = drink.realPrice.toString() + Constant.CURRENCY
            holder.tvPriceSale.text = strRealPrice
        }
        holder.layoutItem.setOnClickListener {
            iClickDrinkListener.onClickDrinkItem(
                drink
            )
        }
    }

    override fun getItemCount(): Int {
        return listDrink?.size ?: 0
    }

    class DrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDrink: ImageView
        val tvName: TextView
        val tvPrice: TextView
        val tvPriceSale: TextView
        val tvDescription: TextView
        val tvRate: TextView
        val layoutItem: LinearLayout

        init {
            imgDrink = itemView.findViewById(R.id.img_drink)
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            tvPriceSale = itemView.findViewById(R.id.tv_price_sale)
            tvDescription = itemView.findViewById(R.id.tv_description)
            tvRate = itemView.findViewById(R.id.tv_rate)
            layoutItem = itemView.findViewById(R.id.layout_item)
        }
    }
}