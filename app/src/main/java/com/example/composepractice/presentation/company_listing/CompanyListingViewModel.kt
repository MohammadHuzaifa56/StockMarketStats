package com.example.composepractice.presentation.company_listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composepractice.domain.repository.StockRepository
import com.example.composepractice.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingViewModel @Inject constructor(
    private val stockRepository: StockRepository
): ViewModel() {
    var state by mutableStateOf(CompanyListingState())
    private var searchJob: Job?= null

    init {
        getCompanyListings()
    }

    fun onEvent(event: CompanyListingEvent) {
        when(event){
            is CompanyListingEvent.Refresh -> {
                getCompanyListings(query = "", fetchFromRemote = true)
            }
            is CompanyListingEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }
            }
        }
    }

    private fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            stockRepository.getCompanyListings(fetchFromRemote,query)
                .collect{result->
                    when(result){
                        is Resource.Success -> {
                            result.data?.let { listing->
                                state = state.copy(companies = listing)
                            }
                        }
                        is Resource.Loading -> {
                            state = state.copy(isLoading = result.isLoading)
                        }
                        is Resource.Error -> Unit
                    }
                }
        }
    }
}