package com.pro.shopfee.activity.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.BaseActivity
import com.pro.shopfee.adapter.admin.AdminDrinkAdapter
import com.pro.shopfee.listener.IOnAdminManagerDrinkListener
import com.pro.shopfee.model.Category
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity

class AdminDrinkByCategoryActivity : BaseActivity() {

    private var mListDrink: MutableList<Drink>? = null
    private var mAdminDrinkAdapter: AdminDrinkAdapter? = null
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_drink_by_category)
        loadDataIntent()
        initView()
        loadListDrink()
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            mCategory = bundleReceived[Constant.KEY_INTENT_CATEGORY_OBJECT] as Category?
        }
    }

    private fun initView() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        tvToolbarTitle.text = mCategory!!.name
        val rcvData = findViewById<RecyclerView>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvData.layoutManager = linearLayoutManager
        mListDrink = ArrayList()
        mAdminDrinkAdapter = AdminDrinkAdapter(mListDrink, object : IOnAdminManagerDrinkListener {
            override fun onClickUpdateDrink(drink: Drink?) {
                onClickEditDrink(drink)
            }

            override fun onClickDeleteDrink(drink: Drink?) {
                deleteDrinkItem(drink)
            }
        })
        rcvData.adapter = mAdminDrinkAdapter
    }

    private fun onClickEditDrink(drink: Drink?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_DRINK_OBJECT, drink)
        startActivity(this, AdminAddDrinkActivity::class.java, bundle)
    }

    private fun deleteDrinkItem(drink: Drink?) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.msg_delete_title))
            .setMessage(getString(R.string.msg_confirm_delete))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                MyApplication[this].getDrinkDatabaseReference()
                    ?.child(drink!!.id.toString())
                    ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                        Toast.makeText(
                            this,
                            getString(R.string.msg_delete_drink_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun resetListDrink() {
        if (mListDrink != null) {
            mListDrink!!.clear()
        } else {
            mListDrink = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadListDrink() {
        MyApplication[this].getDrinkDatabaseReference()
            ?.orderByChild("category_id")
            ?.equalTo(mCategory!!.id.toDouble())
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    resetListDrink()
                    for (dataSnapshot in snapshot.children) {
                        val drink = dataSnapshot.getValue(Drink::class.java) ?: return
                        mListDrink!!.add(0, drink)
                    }
                    if (mAdminDrinkAdapter != null) mAdminDrinkAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}