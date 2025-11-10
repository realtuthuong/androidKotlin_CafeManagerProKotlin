package com.pro.shopfee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.BannerViewPagerAdapter.BannerViewHolder
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.GlideUtils.loadUrlBanner

class BannerViewPagerAdapter(
    private val mListDrink: List<Drink>?,
    private val iClickDrinkListener: IClickDrinkListener
) : RecyclerView.Adapter<BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val drink = mListDrink!![position]
        loadUrlBanner(drink.banner, holder.imgBanner)
        holder.imgBanner.setOnClickListener {
            iClickDrinkListener.onClickDrinkItem(
                drink
            )
        }
    }

    override fun getItemCount(): Int {
        return mListDrink?.size ?: 0
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBanner: ImageView

        init {
            imgBanner = itemView.findViewById(R.id.img_banner)
        }
    }
}