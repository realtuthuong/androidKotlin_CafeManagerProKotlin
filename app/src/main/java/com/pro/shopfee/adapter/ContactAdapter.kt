package com.pro.shopfee.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ContactAdapter.ContactViewHolder
import com.pro.shopfee.model.Contact
import com.pro.shopfee.utils.GlobalFunction.onClickOpenFacebook
import com.pro.shopfee.utils.GlobalFunction.onClickOpenGmail
import com.pro.shopfee.utils.GlobalFunction.onClickOpenSkype
import com.pro.shopfee.utils.GlobalFunction.onClickOpenYoutubeChannel
import com.pro.shopfee.utils.GlobalFunction.onClickOpenZalo

class ContactAdapter(
    private var context: Context?,
    private val listContact: List<Contact>?,
    private val iCallPhone: ICallPhone
) : RecyclerView.Adapter<ContactViewHolder>() {

    interface ICallPhone {
        fun onClickCallPhone()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = listContact!![position]
        holder.imgContact.setImageResource(contact.image)
        when (contact.id) {
            Contact.FACEBOOK -> holder.tvContact.text = context!!.getString(R.string.label_facebook)
            Contact.HOTLINE -> holder.tvContact.text = context!!.getString(R.string.label_call)
            Contact.GMAIL -> holder.tvContact.text = context!!.getString(R.string.label_gmail)
            Contact.SKYPE -> holder.tvContact.text = context!!.getString(R.string.label_skype)
            Contact.YOUTUBE -> holder.tvContact.text = context!!.getString(R.string.label_youtube)
            Contact.ZALO -> holder.tvContact.text = context!!.getString(R.string.label_zalo)
        }
        holder.layoutItem.setOnClickListener {
            when (contact.id) {
                Contact.FACEBOOK -> onClickOpenFacebook(
                    context!!
                )
                Contact.HOTLINE -> iCallPhone.onClickCallPhone()
                Contact.GMAIL -> onClickOpenGmail(context!!)
                Contact.SKYPE -> onClickOpenSkype(context!!)
                Contact.YOUTUBE -> onClickOpenYoutubeChannel(context!!)
                Contact.ZALO -> onClickOpenZalo(context!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return listContact?.size ?: 0
    }

    fun release() {
        context = null
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutItem: LinearLayout
        val imgContact: ImageView
        val tvContact: TextView

        init {
            layoutItem = itemView.findViewById(R.id.layout_item)
            imgContact = itemView.findViewById(R.id.img_contact)
            tvContact = itemView.findViewById(R.id.tv_contact)
        }
    }
}