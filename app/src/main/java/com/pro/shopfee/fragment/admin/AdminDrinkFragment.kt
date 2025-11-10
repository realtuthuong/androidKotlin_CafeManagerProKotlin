package com.pro.shopfee.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.admin.AdminAddDrinkActivity
import com.pro.shopfee.adapter.admin.AdminDrinkAdapter
import com.pro.shopfee.listener.IOnAdminManagerDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.utils.Utils.getTextSearch
import java.util.*

class AdminDrinkFragment : Fragment() {

    private var mView: View? = null
    private var mListDrink: MutableList<Drink>? = null
    private var mAdminDrinkAdapter: AdminDrinkAdapter? = null
    private var mChildEventListener: ChildEventListener? = null
    private var edtSearchName: EditText? = null
    private var imgSearch: ImageView? = null
    private var btnAdd: FloatingActionButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_admin_drink, container, false)
        initUi()
        initView()
        initListener()
        loadListDrink("")
        return mView
    }

    private fun initUi() {
        edtSearchName = mView!!.findViewById(R.id.edt_search_name)
        imgSearch = mView!!.findViewById(R.id.img_search)
        btnAdd = mView!!.findViewById(R.id.btn_add)
    }

    private fun initView() {
        val rcvData = mView!!.findViewById<RecyclerView>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(activity)
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
        rcvData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    btnAdd!!.hide()
                } else {
                    btnAdd!!.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initListener() {
        btnAdd!!.setOnClickListener { onClickAddDrink() }
        imgSearch!!.setOnClickListener { searchDrink() }
        edtSearchName!!.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrink()
                return@setOnEditorActionListener true
            }
            false
        }
        edtSearchName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchDrink()
                }
            }
        })
    }

    private fun onClickAddDrink() {
        startActivity(activity!!, AdminAddDrinkActivity::class.java)
    }

    private fun onClickEditDrink(drink: Drink?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_DRINK_OBJECT, drink)
        startActivity(activity!!, AdminAddDrinkActivity::class.java, bundle)
    }

    private fun deleteDrinkItem(drink: Drink?) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.msg_delete_title))
            .setMessage(getString(R.string.msg_confirm_delete))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                if (activity == null) {
                    return@setPositiveButton
                }
                MyApplication[activity!!].getDrinkDatabaseReference()
                    ?.child(drink!!.id.toString())
                    ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                        Toast.makeText(
                            activity,
                            getString(R.string.msg_delete_drink_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun searchDrink() {
        val strKey = edtSearchName!!.text.toString().trim { it <= ' ' }
        resetListDrink()
        if (activity != null && mChildEventListener != null) {
            MyApplication[activity!!].getDrinkDatabaseReference()
                ?.removeEventListener(mChildEventListener!!)
        }
        loadListDrink(strKey)
        GlobalFunction.hideSoftKeyboard(activity!!)
    }

    private fun resetListDrink() {
        if (mListDrink != null) {
            mListDrink!!.clear()
        } else {
            mListDrink = ArrayList()
        }
    }

    private fun loadListDrink(keyword: String?) {
        if (activity == null) return
        mChildEventListener = object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val drink = dataSnapshot.getValue(Drink::class.java)
                if (drink == null || mListDrink == null) return
                if (isEmpty(keyword)) {
                    mListDrink!!.add(0, drink)
                } else {
                    if (getTextSearch(drink.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                            .contains(getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                        mListDrink!!.add(0, drink)
                    }
                }
                if (mAdminDrinkAdapter != null) mAdminDrinkAdapter!!.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val drink = dataSnapshot.getValue(Drink::class.java)
                if (drink == null || mListDrink == null || mListDrink!!.isEmpty()) return
                for (i in mListDrink!!.indices) {
                    if (drink.id == mListDrink!![i].id) {
                        mListDrink!![i] = drink
                        break
                    }
                }
                if (mAdminDrinkAdapter != null) mAdminDrinkAdapter!!.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val drink = dataSnapshot.getValue(Drink::class.java)
                if (drink == null || mListDrink == null || mListDrink!!.isEmpty()) return
                for (drinkObject in mListDrink!!) {
                    if (drink.id == drinkObject.id) {
                        mListDrink!!.remove(drinkObject)
                        break
                    }
                }
                if (mAdminDrinkAdapter != null) mAdminDrinkAdapter!!.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        MyApplication[activity!!].getDrinkDatabaseReference()
            ?.addChildEventListener(mChildEventListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (activity != null && mChildEventListener != null) {
            MyApplication[activity!!].getDrinkDatabaseReference()
                ?.removeEventListener(mChildEventListener!!)
        }
    }
}