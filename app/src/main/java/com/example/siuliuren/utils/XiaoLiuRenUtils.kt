package com.example.siuliuren.utils

import com.tyme.solar.SolarDay
import java.util.Calendar
import kotlin.random.Random

/**
 * 小六壬核心運算工具
 */
object XiaoLiuRenUtils {
    val PALACES = listOf("大安 (吉)", "留連 (平)", "速喜 (吉)", "赤口 (凶)", "小吉 (吉)", "空亡 (大凶)")
    val BRANCHES = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    data class CalculationResult(
        val monthPalace: String,
        val dayPalace: String,
        val finalPalace: String
    )

    data class CalculationParams(
        val month: Int,
        val day: Int,
        val hourBranch: Int,
        val isRandom: Boolean
    )

    /**
     * 基本運算邏輯
     * @param month 農曆月份
     * @param day 農曆日期
     * @param hourBranch 時辰索引 (1-12)
     */
    fun calculate(month: Int, day: Int, hourBranch: Int): CalculationResult {
        val monthIdx = (month - 1) % 6
        val dayIdx = (monthIdx + day - 1) % 6
        val hourIdx = (dayIdx + hourBranch - 1) % 6
        return CalculationResult(PALACES[monthIdx], PALACES[dayIdx], PALACES[hourIdx])
    }

    /**
     * 獲取當前時間的起卦參數
     */
    fun getCurrentTimeParams(): CalculationParams {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val branchIndex = ((hour + 1) % 24 / 2) + 1
        val lunarDay = SolarDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        ).getLunarDay()
        
        return CalculationParams(lunarDay.month, lunarDay.day, branchIndex, false)
    }

    /**
     * 獲取隨機三數的起卦參數
     */
    fun getRandomParams(): CalculationParams {
        return CalculationParams(
            Random.nextInt(1, 100),
            Random.nextInt(1, 100),
            Random.nextInt(1, 100),
            true
        )
    }
}
