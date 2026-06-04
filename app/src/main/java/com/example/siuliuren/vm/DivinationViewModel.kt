package com.example.siuliuren.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.siuliuren.entity.DivinationRecord
import com.example.siuliuren.repository.DivinationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DivinationViewModel(private val repository: DivinationRepository) : ViewModel() {

    // 目前選中的日期，預設為今天
    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate

    // 控制頁面狀態：0 為首頁，1 為起卦功能頁
    private val _screenState = MutableStateFlow(0)
    val screenState: StateFlow<Int> = _screenState

    // 根據日期自動切換的資料流
    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<DivinationRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getRecordsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun setScreen(state: Int) {
        _screenState.value = state
    }

    fun saveRecord(question: String, result: String, process: String) {
        viewModelScope.launch {
            val record = DivinationRecord(
                date = _selectedDate.value,
                question = question,
                result = result,
                process = process
            )
            repository.insert(record)
            _screenState.value = 0 // 儲存後跳回首頁
        }
    }

    fun deleteRecord(record: DivinationRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}
