/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schoolselection.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.model.SchoolHistoryModel
import com.chisacwnu.hicwnu.data.repository.SchoolHistoryRepository
import com.chisacwnu.hicwnu.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import school_index.AdapterCategory
import school_index.School
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class SchoolSelectionViewModel(
    private val schoolRepository: SchoolRepository,
    private val historyRepository: SchoolHistoryRepository
) : ViewModel() {

    private val _allSchools = MutableStateFlow<List<School>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow(AdapterCategory.BACHELOR_AND_ASSOCIATE)
    val selectedCategory: StateFlow<AdapterCategory> = _selectedCategory

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

        val schoolHistory: StateFlow<SchoolHistoryModel> = historyRepository.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchoolHistoryModel()
        )

    init {
        loadSchools()
    }

    val displayCategories: List<AdapterCategory> = listOf(
        AdapterCategory.BACHELOR_AND_ASSOCIATE,
        AdapterCategory.POSTGRADUATE,
        AdapterCategory.GENERAL_TOOL
    )

        val filteredSchools: StateFlow<List<School>> = combine(
        _allSchools,
        _searchQuery,
        _selectedCategory
    ) { allSchools, query, category ->
        val categoryFiltered = allSchools.filter { school ->
            school.adapters.any { adapter -> adapter.category == category }
        }

        if (query.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { school ->
                school.name.contains(query, ignoreCase = true) ||
                        school.initial.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun loadSchools() {
        viewModelScope.launch {
            _isLoading.value = true
            val schools = schoolRepository.getSchools()
            _allSchools.value = schools
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: AdapterCategory) {
        _selectedCategory.value = category
    }

    fun saveLastSchool(school: School) {
        viewModelScope.launch {
            historyRepository.saveLastSchool(_selectedCategory.value, school)
        }
    }

    fun clearHistory(category: AdapterCategory) {
        viewModelScope.launch {
            historyRepository.clearHistory(category)
        }
    }

    
    suspend fun getAdaptersForSchoolAndCategory(schoolId: String): List<school_index.Adapter> {
        val allAdapters = schoolRepository.getAdaptersForSchool(schoolId)
        val currentCategory = _selectedCategory.value
        return allAdapters.filter { adapter ->
            adapter.category == currentCategory
        }
    }
}