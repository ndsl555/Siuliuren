package com.example.siuliuren.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.siuliuren.repository.DivinationRepository

class DivinationViewModelFactory(private val repository: DivinationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DivinationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DivinationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
