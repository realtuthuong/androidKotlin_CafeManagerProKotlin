package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.CartAdapter.CartViewHolder
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlideUtils.loadUrl

class CartAdapter(
    private val listDrink: List<Drink>?,
    private val iClickCartListener: IClickCartListener
) : RecyclerView.Adapter<CartViewHolder>() {

    interface IClickCartListener {
        fun onClickDeleteItem(drink: Drink?, position: Int)
        fun onClickUpdateItem(drink: Drink?, position: Int)
        fun onClickEditItem(drink: Drink?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val drink = listDrink!![position]
        loadUrl(drink.image, holder.imgDrink)
        holder.tvName.text = drink.name
        val strPrice = drink.priceOneDrink.toString() + Constant.CURRENCY
        holder.tvPrice.text = strPrice
        holder.tvOption.text = drink.option
        val strQuantity = "x" + drink.count
        holder.tvQuantity.text = strQuantity
        holder.tvCount.text = drink.count.toString()
        holder.tvSub.setOnClickListener {
            val strCount = holder.tvCount.text.toString()
            val count = strCount.toInt()
            if (count <= 1) {
                return@setOnClickListener
            }
            val newCount = count - 1
            holder.tvCount.text = newCount.toString()
            val totalPrice = drink.priceOneDrink * newCount
            drink.count = newCount
            drink.totalPrice = totalPrice
            iClickCartListener.onClickUpdateItem(drink, holder.adapterPosition)
        }
        holder.tvAdd.setOnClickListener {
            val newCount = holder.tvCount.text.toString().toInt() + 1
            holder.tvCount.text = newCount.toString()
            val totalPrice = drink.priceOneDrink * newCount
            drink.count = newCount
            drink.totalPrice = totalPrice
            iClickCartListener.onClickUpdateItem(drink, holder.adapterPosition)
        }
        holder.imgEdit.setOnClickListener { iClickCartListener.onClickEditItem(drink) }
        holder.imgDelete.setOnClickListener {
            iClickCartListener.onClickDeleteItem(
                drink,
                holder.adapterPosition
            )
        }
    }

    override fun getItemCount(): Int {
        return listDrink?.size ?: 0
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDrink: ImageView
        val tvName: TextView
        val tvPrice: TextView
        val tvOption: TextView
        val tvQuantity: TextView
        val tvSub: TextView
        val tvCount: TextView
        val tvAdd: TextView
        val imgEdit: ImageView
        val imgDelete: ImageView

        init {
            imgDrink = itemView.findViewById(R.id.img_drink)
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            tvOption = itemView.findViewById(R.id.tv_option)
            tvQuantity = itemView.findViewById(R.id.tv_quantity)
            tvSub = itemView.findViewById(R.id.tv_sub)
            tvAdd = itemView.findViewById(R.id.tv_add)
            tvCount = itemView.findViewById(R.id.tv_count)
            imgEdit = itemView.findViewById(R.id.img_edit)
            imgDelete = itemView.findViewById(R.id.img_delete)
        }
    }
}