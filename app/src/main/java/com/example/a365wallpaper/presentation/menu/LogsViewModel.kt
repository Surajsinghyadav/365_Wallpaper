package com.example.a365wallpaper.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a365wallpaper.data.database.Dao.LogDao
import com.example.a365wallpaper.data.database.Entity.LogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogsViewModel(private val logDao: LogDao) : ViewModel() {

    val logsUiState: StateFlow<List<LogEntity>> = logDao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val totalLogsCount: StateFlow<Int> = logDao.getTotalLogsCountFlow()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000L),
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