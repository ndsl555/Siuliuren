package com.example.siuliuren

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siuliuren.data.AppDatabase
import com.example.siuliuren.repository.DivinationRepository
import com.example.siuliuren.ui.theme.SiuliurenTheme
import com.example.siuliuren.vm.DivinationViewModel
import com.example.siuliuren.vm.DivinationViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SiuliurenTheme {
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val repository = DivinationRepository(database.divinationDao())
                val factory = DivinationViewModelFactory(repository)

                val viewModel: DivinationViewModel = viewModel(factory = factory)

                // 觀察 ViewModel 的狀態
                val screenState by viewModel.screenState.collectAsState()
                val selectedDate by viewModel.selectedDate.collectAsState()
                val records by viewModel.records.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (screenState) {
                        0 -> MainHomeScreen(
                            selectedDate = selectedDate,
                            onDateChange = { viewModel.updateDate(it) },
                            records = records,
                            onDeleteRecord = { viewModel.deleteRecord(it) },
                            onNavigateToCalculate = { viewModel.setScreen(1) }
                        )
                        1 -> CalculateDetailScreen(
                            targetDate = selectedDate,
                            onSave = { record ->
                                viewModel.saveRecord(
                                    question = record.question,
                                    result = record.result,
                                    process = record.process
                                )
                            },
                            onCancel = { viewModel.setScreen(0) }
                        )
                    }
                }
            }
        }
    }
}
