package com.pro.shopfee.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.ContactAdapter
import com.pro.shopfee.adapter.ContactAdapter.ICallPhone
import com.pro.shopfee.constant.AboutUsConfig
import com.pro.shopfee.model.Contact
import com.pro.shopfee.utils.GlobalFunction.callPhoneNumber

class ContactActivity : BaseActivity() {

    private var tvAboutUsTitle: TextView? = null
    private var tvAboutUsContent: TextView? = null
    private var tvAboutUsWebsite: TextView? = null
    private var layoutWebsite: LinearLayout? = null
    private var rcvData: RecyclerView? = null
    private var mContactAdapter: ContactAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        initToolbar()
        initUi()
        initData()
        initListener()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.contact)
    }

    private fun initUi() {
        tvAboutUsTitle = findViewById(R.id.tv_about_us_title)
        tvAboutUsContent = findViewById(R.id.tv_about_us_content)
        tvAboutUsWebsite = findViewById(R.id.tv_about_us_website)
        layoutWebsite = findViewById(R.id.layout_website)
        rcvData = findViewById(R.id.rcvData)
    }

    private fun initData() {
        tvAboutUsTitle!!.text = AboutUsConfig.ABOUT_US_TITLE
        tvAboutUsContent!!.text = AboutUsConfig.ABOUT_US_CONTENT
        tvAboutUsWebsite!!.text = AboutUsConfig.ABOUT_US_WEBSITE_TITLE
        mContactAdapter = ContactAdapter(this, listContact,
            object : ICallPhone {
                override fun onClickCallPhone() {
                    callPhoneNumber(this@ContactActivity)
                }
            })
        val layoutManager = GridLayoutManager(this, 3)
        rcvData!!.isNestedScrollingEnabled = false
        rcvData!!.isFocusable = false
        rcvData!!.layoutManager = layoutManager
        rcvData!!.adapter = mContactAdapter
    }

    private fun initListener() {
        layoutWebsite!!.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(AboutUsConfig.WEBSITE)
                )
            )
        }
    }

    private val listContact: List<Contact>
        get() {
            val contactArrayList: MutableList<Contact> = ArrayList()
            contactArrayList.add(Contact(Contact.FACEBOOK, R.drawable.ic_facebook))
            contactArrayList.add(Contact(Contact.HOTLINE, R.drawable.ic_hotline))
            contactArrayList.add(Contact(Contact.GMAIL, R.drawable.ic_gmail))
            contactArrayList.add(Contact(Contact.SKYPE, R.drawable.ic_skype))
            contactArrayList.add(Contact(Contact.YOUTUBE, R.drawable.ic_youtube))
            contactArrayList.add(Contact(Contact.ZALO, R.drawable.ic_zalo))
            return contactArrayList
        }

    public override fun onDestroy() {
        super.onDestroy()
        mContactAdapter!!.release()
    }
}