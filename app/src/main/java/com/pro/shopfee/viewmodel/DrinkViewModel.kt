package com.pro.shopfee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.Filter
import com.pro.shopfee.repository.DrinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class DrinkViewModel @Inject constructor(
    private val drinkRepository: DrinkRepository
) : ViewModel() {

    private val _allDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val allDrinks: StateFlow<List<Drink>> = _allDrinks.asStateFlow()

    private val _filteredDrinks = MutableStateFlow<List<Drink>>(emptyList())
    val filteredDrinks: StateFlow<List<Drink>> = _filteredDrinks.asStateFlow()

    private val _currentFilter = MutableStateFlow<Filter?>(null)
    val currentFilter: StateFlow<Filter?> = _currentFilter.asStateFlow()

    private val _searchKeyword = MutableStateFlow<String>("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    fun loadDrinksByCategory(categoryId: Long) {
        viewModelScope.launch {
            drinkRepository.observeDrinksByCategory(categoryId).collect { drinks ->
                _allDrinks.value = drinks
                applyFilterAndSearch()
            }
        }
    }

    fun setFilter(filter: Filter) {
        _currentFilter.value = filter
        applyFilterAndSearch()
    }

    fun setSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        val drinks = _allDrinks.value
        val keyword = _searchKeyword.value.trim()
        val filter = _currentFilter.value

        var filtered = if (keyword.isNotEmpty()) {
            drinks.filter { drink ->
                val drinkName = getTextSearch(drink.name ?: "")
                val searchText = getTextSearch(keyword)
                drinkName.contains(searchText, ignoreCase = true)
            }
        } else {
            drinks
        }

        when (filter?.id) {
            Filter.TYPE_FILTER_ALL -> {
                // Already filtered by keyword
            }
            Filter.TYPE_FILTER_RATE -> {
                filtered = filtered.sortedByDescending { it.rate }
            }
            Filter.TYPE_FILTER_PRICE -> {
                filtered = filtered.sortedBy { it.realPrice }
            }
            Filter.TYPE_FILTER_PROMOTION -> {
                filtered = filtered.filter { it.sale > 0 }
            }
        }

        _filteredDrinks.value = filtered
    }

    private fun getTextSearch(input: String?): String {
        if (input == null) return ""
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }
}

