package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.PaymentMethodAdapter
import com.pro.shopfee.adapter.PaymentMethodAdapter.IClickPaymentMethodListener
import com.pro.shopfee.event.PaymentMethodSelectedEvent
import com.pro.shopfee.model.PaymentMethod
import com.pro.shopfee.utils.Constant
import org.greenrobot.eventbus.EventBus

class PaymentMethodActivity : BaseActivity() {

    private var listPaymentMethod: MutableList<PaymentMethod>? = null
    private var paymentMethodAdapter: PaymentMethodAdapter? = null
    private var paymentMethodSelectedId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)
        loadDataIntent()
        initToolbar()
        initUi()
        loadListPaymentMethodFromFirebase()
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        paymentMethodSelectedId = bundle.getInt(Constant.PAYMENT_METHOD_ID, 0)
    }

    private fun initUi() {
        val rcvPaymentMethod = findViewById<RecyclerView>(R.id.rcv_payment_method)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvPaymentMethod.layoutManager = linearLayoutManager
        listPaymentMethod = ArrayList()
        paymentMethodAdapter = PaymentMethodAdapter(
            listPaymentMethod,
            object : IClickPaymentMethodListener {
                override fun onClickPaymentMethodItem(paymentMethod: PaymentMethod) {
                    handleClickPaymentMethod(
                        paymentMethod
                    )
                }
            })
        rcvPaymentMethod.adapter = paymentMethodAdapter
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.title_payment_method)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListPaymentMethodFromFirebase() {
        resetListPaymentMethod()
        listPaymentMethod!!.add(
            PaymentMethod(
                1,
                "Thanh toán tiền mặt",
                "(Thanh toán khi nhận hàng)"
            )
        )
        listPaymentMethod!!.add(
            PaymentMethod(
                2,
                "Credit or debit card",
                "(Thẻ Visa hoặc Mastercard)"
            )
        )
        listPaymentMethod!!.add(
            PaymentMethod(
                3,
                "Chuyển khoản ngân hàng",
                "(Tự động xác nhận)"
            )
        )
        // Thêm phương thức thanh toán Blockchain (Sepolia)
        listPaymentMethod!!.add(
            PaymentMethod(
                Constant.TYPE_BLOCKCHAIN,
                "Blockchain (Sepolia)",
                "(Thanh toán bằng ETH qua ví)"
            )
        )
        if (paymentMethodSelectedId > 0 && listPaymentMethod != null) {
            for (paymentMethod in listPaymentMethod!!) {
                if (paymentMethod.id == paymentMethodSelectedId) {
                    paymentMethod.isSelected = true
                    break
                }
            }
        }
        if (paymentMethodAdapter != null) paymentMethodAdapter!!.notifyDataSetChanged()
    }

    private fun resetListPaymentMethod() {
        if (listPaymentMethod != null) {
            listPaymentMethod!!.clear()
        } else {
            listPaymentMethod = ArrayList()
        }
    }

    private fun handleClickPaymentMethod(paymentMethod: PaymentMethod) {
        EventBus.getDefault().post(PaymentMethodSelectedEvent(paymentMethod))
        finish()
    }
}