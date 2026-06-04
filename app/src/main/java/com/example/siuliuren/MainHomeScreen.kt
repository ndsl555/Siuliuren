package com.example.siuliuren

import android.widget.CalendarView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.siuliuren.entity.DivinationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    records: List<DivinationRecord>,
    onDeleteRecord: (DivinationRecord) -> Unit,
    onNavigateToCalculate: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.main_title), style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCalculate) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_record))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    CalendarView(context).apply {
                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            onDateChange(dateStr)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(320.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Text(
                text = stringResource(R.string.records_on_date, selectedDate),
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (records.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_records), color = Color.Gray)
                        }
                    }
                }
                items(records) { record ->
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.question_format, record.question), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.result_format, record.result), color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteRecord(record) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
