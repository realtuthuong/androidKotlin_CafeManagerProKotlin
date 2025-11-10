package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Category
import com.pro.shopfee.model.Drink
import com.pro.shopfee.repository.CategoryRepository
import com.pro.shopfee.repository.DrinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val drinkRepository: DrinkRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _featuredDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val featuredDrinks: StateFlow<List<Drink>> = _featuredDrinks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadFeaturedDrinks()
        loadCategories()
    }

    private fun loadFeaturedDrinks() {
        viewModelScope.launch {
            drinkRepository.observeFeaturedDrinks().collect { drinks ->
                _featuredDrinks.value = drinks
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.observeAllCategories().collect { categories ->
                _categories.value = categories
            }
        }
    }
}

