package com.pro.shopfee.adapter.admin

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.admin.AdminDrinkAdapter.AdminDrinkViewHolder
import com.pro.shopfee.listener.IOnAdminManagerDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlideUtils.loadUrl

class AdminDrinkAdapter(
    private val listDrink: List<Drink>?,
    private val mListener: IOnAdminManagerDrinkListener
) : RecyclerView.Adapter<AdminDrinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminDrinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_drink, parent, false)
        return AdminDrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminDrinkViewHolder, position: Int) {
        val drink = listDrink!![position]
        loadUrl(drink.image, holder.imgProduct)
        holder.tvName.text = drink.name
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
        if (drink.category_id > 0) {
            holder.layoutCategory.visibility = View.VISIBLE
            holder.tvCategory.text = drink.category_name
        } else {
            holder.layoutCategory.visibility = View.GONE
        }
        if (drink.isFeatured) {
            holder.tvFeatured.text = "Có"
        } else {
            holder.tvFeatured.text = "Không"
        }
        holder.imgEdit.setOnClickListener { mListener.onClickUpdateDrink(drink) }
        holder.imgDelete.setOnClickListener { mListener.onClickDeleteDrink(drink) }
    }

    override fun getItemCount(): Int {
        return listDrink?.size ?: 0
    }

    class AdminDrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView
        val tvName: TextView
        val tvPrice: TextView
        val tvPriceSale: TextView
        val layoutCategory: LinearLayout
        val tvCategory: TextView
        val tvFeatured: TextView
        val imgEdit: ImageView
        val imgDelete: ImageView

        init {
            imgProduct = itemView.findViewById(R.id.img_product)
            tvName = itemView.findViewById(R.id.tv_name)
            tvPrice = itemView.findViewById(R.id.tv_price)
            tvPriceSale = itemView.findViewById(R.id.tv_price_sale)
            layoutCategory = itemView.findViewById(R.id.layout_category)
            tvCategory = itemView.findViewById(R.id.tv_category)
            tvFeatured = itemView.findViewById(R.id.tv_featured)
            imgEdit = itemView.findViewById(R.id.img_edit)
            imgDelete = itemView.findViewById(R.id.img_delete)
        }
    }
}