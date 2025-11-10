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
import com.pro.shopfee.activity.DrinkDetailActivity
import com.pro.shopfee.adapter.DrinkAdapter
import com.pro.shopfee.adapter.FilterAdapter
import com.pro.shopfee.adapter.FilterAdapter.IClickFilterListener
import com.pro.shopfee.event.SearchKeywordEvent
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.Filter
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.viewmodel.DrinkViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class DrinkFragment : Fragment() {

    private val viewModel: DrinkViewModel by viewModels()

    private var mView: View? = null
    private var rcvFilter: RecyclerView? = null
    private var rcvDrink: RecyclerView? = null
    private var listDrinkDisplay: MutableList<Drink> = mutableListOf()
    private var listFilter: ArrayList<Filter> = ArrayList()
    private var drinkAdapter: DrinkAdapter? = null
    private var filterAdapter: FilterAdapter? = null
    private var categoryId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_drink, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        loadDataArguments()
        initUi()
        initListener()
        getListFilter()
        observeViewModel()
        viewModel.loadDrinksByCategory(categoryId)
        return mView
    }

    private fun loadDataArguments() {
        val bundle = arguments ?: return
        categoryId = bundle.getLong(Constant.CATEGORY_ID)
    }

    private fun initUi() {
        rcvFilter = mView!!.findViewById(R.id.rcv_filter)
        rcvDrink = mView!!.findViewById(R.id.rcv_drink)
        displayListDrink()
    }

    private fun initListener() {}
    private fun getListFilter() {
        listFilter.add(Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)))
        listFilter.add(Filter(Filter.TYPE_FILTER_RATE, getString(R.string.filter_rate)))
        listFilter.add(Filter(Filter.TYPE_FILTER_PRICE, getString(R.string.filter_price)))
        listFilter.add(Filter(Filter.TYPE_FILTER_PROMOTION, getString(R.string.filter_promotion)))
        val linearLayoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL, false
        )
        rcvFilter!!.layoutManager = linearLayoutManager
        val defaultFilter = listFilter[0]
        defaultFilter.isSelected = true
        viewModel.setFilter(defaultFilter)
        filterAdapter = FilterAdapter(
            activity,
            listFilter,
            object : IClickFilterListener {
                override fun onClickFilterItem(filter: Filter) {
                    handleClickFilter(filter)
                }
            })
        rcvFilter!!.adapter = filterAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleClickFilter(filter: Filter) {
        viewModel.setFilter(filter)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredDrinks.collect { drinks ->
                listDrinkDisplay.clear()
                listDrinkDisplay.addAll(drinks)
                drinkAdapter?.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentFilter.collect { filter ->
                if (filter != null) {
                    updateFilterSelection(filter)
                }
            }
        }
    }

    private fun updateFilterSelection(selectedFilter: Filter) {
        listFilter.forEach { it.isSelected = it.id == selectedFilter.id }
        filterAdapter?.notifyDataSetChanged()
    }

    private fun displayListDrink() {
        if (activity == null) return
        val linearLayoutManager = LinearLayoutManager(activity)
        rcvDrink!!.layoutManager = linearLayoutManager
        drinkAdapter = DrinkAdapter(listDrinkDisplay, object : IClickDrinkListener {
            override fun onClickDrinkItem(drink: Drink) {
                val bundle = Bundle()
                bundle.putLong(Constant.DRINK_ID, drink.id)
                startActivity(activity!!, DrinkDetailActivity::class.java, bundle)
            }
        })
        rcvDrink!!.adapter = drinkAdapter
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearchKeywordEvent(event: SearchKeywordEvent) {
        viewModel.setSearchKeyword(event.keyword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterAdapter?.release()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    companion object {
        fun newInstance(categoryId: Long): DrinkFragment {
            val drinkFragment = DrinkFragment()
            val bundle = Bundle()
            bundle.putLong(Constant.CATEGORY_ID, categoryId)
            drinkFragment.arguments = bundle
            return drinkFragment
        }
    }
}