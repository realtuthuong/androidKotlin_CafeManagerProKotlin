package com.pro.shopfee.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.pro.shopfee.R
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.event.OrderSuccessEvent
import com.pro.shopfee.model.Order
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class PaymentActivity : BaseActivity() {

    private val viewModel: PaymentViewModel by viewModels()

    private var mOrderBooking: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        loadDataIntent()
        observeViewModel()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ createOrderFirebase() }, 2000)
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        mOrderBooking = bundle[Constant.ORDER_OBJECT] as Order?
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.paymentState.collect { state ->
                when (state) {
                    is PaymentViewModel.PaymentState.Idle -> {
                        // Do nothing
                    }
                    is PaymentViewModel.PaymentState.Loading -> {
                        showProgressDialog(true)
                    }
                    is PaymentViewModel.PaymentState.Success -> {
                        showProgressDialog(false)
                        EventBus.getDefault().post(DisplayCartEvent())
                        EventBus.getDefault().post(OrderSuccessEvent())

                        val bundle = Bundle()
                        bundle.putLong(Constant.ORDER_ID, state.orderId)
                        startActivity(this@PaymentActivity, ReceiptOrderActivity::class.java, bundle)
                        finish()
                    }
                    is PaymentViewModel.PaymentState.Error -> {
                        showProgressDialog(false)
                        showToastMessage(state.message)
                    }
                }
            }
        }
    }

    private fun createOrderFirebase() {
        val order = mOrderBooking ?: return
        viewModel.createOrder(order)
    }
}