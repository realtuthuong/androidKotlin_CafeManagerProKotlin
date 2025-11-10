package com.pro.shopfee.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pro.shopfee.R
import com.pro.shopfee.adapter.MyViewPagerAdapter
import com.pro.shopfee.event.DisplayCartEvent
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var mBottomNavigationView: BottomNavigationView? = null
    private var viewPager2: ViewPager2? = null
    private var layoutCartBottom: RelativeLayout? = null
    private var tvCountItem: TextView? = null
    private var tvDrinksName: TextView? = null
    private var tvAmount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Đăng ký EventBus (chỉ 1 lần)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        // Khởi tạo UI
        initUi()

        // Thiết lập Bottom Navigation và ViewPager2
        setupViewPagerAndBottomNav()

        // Observe ViewModel
        observeViewModel()
    }

    private fun initUi() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation)
        viewPager2 = findViewById(R.id.viewpager_2)
        layoutCartBottom = findViewById(R.id.layout_cart_bottom)
        tvCountItem = findViewById(R.id.tv_count_item)
        tvDrinksName = findViewById(R.id.tv_drinks_name)
        tvAmount = findViewById(R.id.tv_amount)
    }

    private fun setupViewPagerAndBottomNav() {
        viewPager2?.isUserInputEnabled = false
        val myViewPagerAdapter = MyViewPagerAdapter(this)
        viewPager2?.adapter = myViewPagerAdapter

        // Đồng bộ khi vuốt trang
        viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> mBottomNavigationView?.menu?.findItem(R.id.nav_home)?.isChecked = true
                    1 -> mBottomNavigationView?.menu?.findItem(R.id.nav_history)?.isChecked = true
                    2 -> mBottomNavigationView?.menu?.findItem(R.id.nav_account)?.isChecked = true
                }
            }
        })

        // Chuyển tab khi click bottom nav
        mBottomNavigationView?.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> setMainPage(0)
                R.id.nav_history -> setMainPage(1)
                R.id.nav_account -> setMainPage(2)
            }
            true
        }
    }

    fun setMainPage(index: Int) {
        viewPager2?.currentItem = index
    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
            .title(getString(R.string.app_name))
            .content(getString(R.string.msg_exit_app))
            .positiveText(getString(R.string.action_ok))
            .onPositive { _: MaterialDialog?, _: DialogAction? -> finish() }
            .negativeText(getString(R.string.action_cancel))
            .cancelable(false)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.cartState.collect { state ->
                displayLayoutCartBottom(state)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDisplayCartEvent(event: DisplayCartEvent?) {
        // Khi có event cập nhật giỏ hàng, ViewModel sẽ tự động cập nhật
    }

    private fun displayLayoutCartBottom(state: MainViewModel.CartState) {
        // Kiểm tra null trước
        if (layoutCartBottom == null) return

        when (state) {
            is MainViewModel.CartState.Empty -> {
                layoutCartBottom?.visibility = View.GONE
            }
            is MainViewModel.CartState.Loaded -> {
                layoutCartBottom?.visibility = View.VISIBLE

                // Số lượng item
                tvCountItem?.text = "${state.itemCount} ${getString(R.string.label_item)}"

                // Tên sản phẩm
                if (isEmpty(state.itemNames)) {
                    tvDrinksName?.visibility = View.GONE
                } else {
                    tvDrinksName?.visibility = View.VISIBLE
                    tvDrinksName?.text = state.itemNames
                }

                // Tổng tiền
                tvAmount?.text = "${state.totalAmount}${Constant.CURRENCY}"

                // Sự kiện click mở giỏ hàng
                layoutCartBottom?.setOnClickListener {
                    startActivity(this, CartActivity::class.java)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}