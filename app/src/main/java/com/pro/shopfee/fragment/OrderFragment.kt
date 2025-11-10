package com.pro.shopfee.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.activity.ReceiptOrderActivity
import com.pro.shopfee.activity.TrackingOrderActivity
import com.pro.shopfee.adapter.OrderAdapter
import com.pro.shopfee.adapter.OrderAdapter.IClickOrderListener
import com.pro.shopfee.model.Order
import com.pro.shopfee.model.TabOrder
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderFragment : Fragment() {

    private val viewModel: OrderViewModel by viewModels()

    private var mView: View? = null
    private var orderTabType = 0
    private var listOrder: MutableList<Order> = mutableListOf()
    private var orderAdapter: OrderAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_order, container, false)
        loadDataArguments()
        initUi()
        observeViewModel()
        viewModel.loadOrders(orderTabType)
        return mView
    }

    private fun loadDataArguments() {
        val bundle = arguments ?: return
        orderTabType = bundle.getInt(Constant.ORDER_TAB_TYPE)
    }

    private fun initUi() {
        val rcvOrder = mView!!.findViewById<RecyclerView>(R.id.rcv_order)
        val linearLayoutManager = LinearLayoutManager(activity)
        rcvOrder.layoutManager = linearLayoutManager
        orderAdapter = OrderAdapter(activity, listOrder, object : IClickOrderListener {
            override fun onClickTrackingOrder(orderId: Long) {
                val bundle = Bundle()
                bundle.putLong(Constant.ORDER_ID, orderId)
                startActivity(activity!!, TrackingOrderActivity::class.java, bundle)
            }

            override fun onClickReceiptOrder(order: Order?) {
                val bundle = Bundle()
                bundle.putLong(Constant.ORDER_ID, order!!.id)
                startActivity(activity!!, ReceiptOrderActivity::class.java, bundle)
            }
        })
        rcvOrder.adapter = orderAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                listOrder.clear()
                listOrder.addAll(orders)
                orderAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderAdapter?.release()
    }

    companion object {
        fun newInstance(type: Int): OrderFragment {
            val orderFragment = OrderFragment()
            val bundle = Bundle()
            bundle.putInt(Constant.ORDER_TAB_TYPE, type)
            orderFragment.arguments = bundle
            return orderFragment
        }
    }
}