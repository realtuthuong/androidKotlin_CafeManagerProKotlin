package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ToppingAdapter.ToppingViewHolder
import com.pro.shopfee.model.Topping
import com.pro.shopfee.utils.Constant

class ToppingAdapter(
    private val listTopping: List<Topping>?,
    private val iClickToppingListener: IClickToppingListener
) : RecyclerView.Adapter<ToppingViewHolder>() {

    interface IClickToppingListener {
        fun onClickToppingItem(topping: Topping)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topping, parent, false)
        return ToppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToppingViewHolder, position: Int) {
        val topping = listTopping!![position]
        holder.tvName.text = topping.name
        val strPrice = "+" + topping.price + Constant.CURRENCY
        holder.tvPrice.text = strPrice
        if (topping.isSelected) {
            holder.imgSelected.setBackgroundResource(R.drawable.ic_item_selected)
        } else {
            holder.imgSelected.setBackgroundResource(R.drawable.ic_item_unselect)
        }
        holder.layoutItem.setOnClickListener {
            iClickToppingListener.onClickToppingItem(
                topping
            )
        }
    }

    override fun getItemCount(): Int {
        return listTopping?.size ?: 0
    }

    class ToppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView
        val tvPrice: TextView
        val imgSelected: ImageView
        val layoutItem: RelativeLayout

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            imgSelected = itemView.findViewById(R.id.img_selected)
            layoutItem = itemView.findViewById(R.id.layout_item)
        }
    }
}