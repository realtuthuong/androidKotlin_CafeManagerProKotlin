package com.pro.shopfee.adapter.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pro.shopfee.fragment.admin.AdminCategoryFragment
import com.pro.shopfee.fragment.admin.AdminDrinkFragment
import com.pro.shopfee.fragment.admin.AdminOrderFragment
import com.pro.shopfee.fragment.admin.AdminSettingsFragment

class AdminViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> AdminDrinkFragment()
            2 -> AdminOrderFragment()
            3 -> AdminSettingsFragment()
            else -> AdminCategoryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}