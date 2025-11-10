package com.pro.shopfee.listener

import com.pro.shopfee.model.Category

interface IOnAdminManagerCategoryListener {
    fun onClickUpdateCategory(category: Category)
    fun onClickDeleteCategory(category: Category)
    fun onClickItemCategory(category: Category)
}