package com.pro.shopfee.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pro.shopfee.R
import com.pro.shopfee.activity.DrinkDetailActivity
import com.pro.shopfee.adapter.BannerViewPagerAdapter
import com.pro.shopfee.adapter.CategoryPagerAdapter
import com.pro.shopfee.event.SearchKeywordEvent
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Category
import com.pro.shopfee.model.Drink
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.Utils
import com.pro.shopfee.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import org.greenrobot.eventbus.EventBus
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var mView: View? = null
    private var viewPagerDrinkFeatured: ViewPager2? = null
    private var indicatorDrinkFeatured: CircleIndicator3? = null
    private var viewPagerCategory: ViewPager2? = null
    private var tabCategory: TabLayout? = null
    private var edtSearchName: EditText? = null
    private var imgSearch: ImageView? = null
    private var listDrinkFeatured: MutableList<Drink> = mutableListOf()
    private var listCategory: MutableList<Category> = mutableListOf()
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private val mRunnableBanner = Runnable {
        if (viewPagerDrinkFeatured == null || listDrinkFeatured.isEmpty()) {
            return@Runnable
        }
        if (viewPagerDrinkFeatured!!.currentItem == listDrinkFeatured.size - 1) {
            viewPagerDrinkFeatured!!.currentItem = 0
            return@Runnable
        }
        viewPagerDrinkFeatured!!.currentItem = viewPagerDrinkFeatured!!.currentItem + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        initUi()
        initListener()
        observeViewModel()
        return mView
    }

    private fun initUi() {
        viewPagerDrinkFeatured = mView!!.findViewById(R.id.view_pager_drink_featured)
        indicatorDrinkFeatured = mView!!.findViewById(R.id.indicator_drink_featured)
        viewPagerCategory = mView!!.findViewById(R.id.view_pager_category)
        viewPagerCategory?.isUserInputEnabled = false
        tabCategory = mView!!.findViewById(R.id.tab_category)
        edtSearchName = mView!!.findViewById(R.id.edt_search_name)
        imgSearch = mView!!.findViewById(R.id.img_search)
    }

    private fun initListener() {
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
        imgSearch!!.setOnClickListener { searchDrink() }
        edtSearchName!!.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrink()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.featuredDrinks.collect { drinks ->
                listDrinkFeatured.clear()
                listDrinkFeatured.addAll(drinks)
                displayListBanner()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                listCategory.clear()
                listCategory.addAll(categories)
                displayTabsCategory()
            }
        }
    }

    private fun displayListBanner() {
        val adapter =
            BannerViewPagerAdapter(listDrinkFeatured, object : IClickDrinkListener {
                override fun onClickDrinkItem(drink: Drink) {
                    val bundle = Bundle()
                    bundle.putLong(Constant.DRINK_ID, drink.id)
                    startActivity(activity!!, DrinkDetailActivity::class.java, bundle)
                }
            })
        viewPagerDrinkFeatured!!.adapter = adapter
        indicatorDrinkFeatured!!.setViewPager(viewPagerDrinkFeatured)
        viewPagerDrinkFeatured!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }


    private fun displayTabsCategory() {
        if (activity == null || listCategory.isEmpty()) return
        viewPagerCategory!!.offscreenPageLimit = listCategory.size
        val adapter = CategoryPagerAdapter(activity!!, listCategory)
        viewPagerCategory!!.adapter = adapter
        TabLayoutMediator(
            tabCategory!!, viewPagerCategory!!
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = listCategory[position].name!!.toLowerCase(Locale.getDefault())
        }
            .attach()
    }

    private fun searchDrink() {
        val strKey = edtSearchName!!.text.toString().trim { it <= ' ' }
        EventBus.getDefault().post(SearchKeywordEvent(strKey))
        Utils.hideSoftKeyboard(activity!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandlerBanner.removeCallbacks(mRunnableBanner)
    }
}