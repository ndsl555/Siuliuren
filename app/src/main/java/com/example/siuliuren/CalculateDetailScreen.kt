package com.example.siuliuren

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siuliuren.entity.DivinationRecord
import com.example.siuliuren.utils.XiaoLiuRenUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateDetailScreen(
    targetDate: String,
    onSave: (DivinationRecord) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    // 處理實體返回鍵
    BackHandler { onCancel() }
    // 1. 初始化音效產生器
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    var questionText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var processSteps by remember { mutableStateOf("") }

    // --- 動畫相關狀態 ---
    val scope = rememberCoroutineScope()
    var highlightedIndex by remember { mutableStateOf<Int?>(null) } // 目前亮起的宮位索引
    var isAnimating by remember { mutableStateOf(false) } // 是否正在動畫中

    val currentTimeStr = stringResource(R.string.current_time)
    val randomNumbersStr = stringResource(R.string.random_numbers)

    val options = remember(currentTimeStr, randomNumbersStr) {
        mutableStateListOf(
            ToggleableInfo(isChecked = true, text = currentTimeStr),
            ToggleableInfo(isChecked = false, text = randomNumbersStr)
        )
    }

    // 確保組件銷毀時釋放資源
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.calculate_title, targetDate),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text(stringResource(R.string.input_question_hint)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.input_question_placeholder)) },
                enabled = !isAnimating
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                options.forEachIndexed { index, info ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = !isAnimating) {
                                options.forEachIndexed { i, _ -> options[i] = options[i].copy(isChecked = i == index) }
                            }
                            .padding(horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = info.isChecked,
                            enabled = !isAnimating,
                            onClick = {
                                options.forEachIndexed { i, _ -> options[i] = options[i].copy(isChecked = i == index) }
                            }
                        )
                        Text(text = info.text)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 六宮格顯示 ---
            PalaceGrid(highlightedIndex = highlightedIndex)

            Spacer(modifier = Modifier.height(24.dp))
            if (!isAnimating && resultText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.result_format, resultText),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isAnimating) return@Button

                    val params = if (options[0].isChecked) {
                        XiaoLiuRenUtils.getCurrentTimeParams()
                    } else {
                        XiaoLiuRenUtils.getRandomParams()
                    }

                    val calculation = XiaoLiuRenUtils.calculate(params.month, params.day, params.hourBranch)
                    // 找出最終結果在 PALACES 中的索引
                    val finalIdx = XiaoLiuRenUtils.PALACES.indexOf(calculation.finalPalace)

                    scope.launch {
                        isAnimating = true
                        resultText = ""

                        // 動畫邏輯：跑 2 圈 (12步) 再加上停在結果位置的步數
                        val totalSteps = 12 + finalIdx

                        for (i in 0..totalSteps) {
                            highlightedIndex = i % 6
                            // 速度模擬：最後幾步逐漸變慢
                            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
                            val delayTime = if (i > totalSteps - 6) 100L + (totalSteps - i) * -20L + 120L else 100L
                            delay(delayTime.coerceAtLeast(80L))
                        }

                        // 動畫結束，顯示結果
                        resultText = calculation.finalPalace
                        processSteps = buildString {
                            if (params.isRandom) {
                                append(context.getString(R.string.random_process_format, params.month, params.day, params.hourBranch))
                            } else {
                                append(context.getString(R.string.lunar_process_format, params.month, params.day, XiaoLiuRenUtils.BRANCHES[params.hourBranch - 1]))
                            }
                            append(context.getString(R.string.calculation_process_format, calculation.monthPalace, calculation.dayPalace, calculation.finalPalace))
                        }
                        isAnimating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnimating
            ) {
                Text(if (isAnimating) stringResource(R.string.calculating) else stringResource(R.string.start_calculating))
            }

            if (resultText.isNotEmpty() && !isAnimating) {
                OutlinedButton(
                    onClick = {
                        resultText = ""
                        processSteps = ""
                        highlightedIndex = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.recalculate))
                }

                val unclassifiedQuestion = context.getString(R.string.unclassified_question)
                Button(
                    onClick = {
                        onSave(
                            DivinationRecord(
                                date = targetDate,
                                question = questionText.ifBlank { unclassifiedQuestion },
                                result = resultText,
                                process = processSteps
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(stringResource(R.string.save_record))
                }
            }
        }
    }
}

/**
 * 繪製六型宮格元件
 */
@Composable
fun PalaceGrid(highlightedIndex: Int?) {
    // 六宮名稱 (從 Utils 獲取並去掉後括號)
    val names = XiaoLiuRenUtils.PALACES.map { it.substringBefore(" ") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 上排: 0, 1, 2
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PalaceBox(names[0], highlightedIndex == 0)
            PalaceBox(names[1], highlightedIndex == 1)
            PalaceBox(names[2], highlightedIndex == 2)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 下排: 5, 4, 3 (依順時針排列習慣)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PalaceBox(names[5], highlightedIndex == 5)
            PalaceBox(names[4], highlightedIndex == 4)
            PalaceBox(names[3], highlightedIndex == 3)
        }
    }
}

/**
 * 單個宮位格子
 */
@Composable
fun PalaceBox(name: String, isHighlighted: Boolean) {
    val bgColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val borderColor = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(80.dp, 60.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

data class ToggleableInfo(val isChecked: Boolean, val text: String)
