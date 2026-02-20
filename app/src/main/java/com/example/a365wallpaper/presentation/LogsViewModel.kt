package com.example.a365wallpaper.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a365wallpaper.data.database.LogDao
import com.example.a365wallpaper.data.database.LogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogsViewModel(private val logDao: LogDao) : ViewModel() {

    val logsUiState: StateFlow<List<LogEntity>> = logDao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val totalLogsCount: StateFlow<Int> = logDao.getTotalLogsCount()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    fun deleteAllLogs() {
        viewModelScope.launch {
            logDao.deleteAllLogs()
        }
    }

    fun insertLog(logEntity: LogEntity) {
        viewModelScope.launch {
            logDao.insertLog(logEntity)
        }
    }
}
