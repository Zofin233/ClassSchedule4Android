package com.example.classschedule.data

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 学期设置（SharedPreferences 持久化）：学期开始日期 + 总周数。
 */
class SettingsStore(context: Context) {

    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** 学期开始日期，自动对齐到所在周的周一 */
    var semesterStart: LocalDate
        get() = sp.getString(KEY_START, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: defaultMonday()
        set(value) {
            sp.edit().putString(KEY_START, value.with(DayOfWeek.MONDAY).toString()).apply()
        }

    var totalWeeks: Int
        get() = sp.getInt(KEY_WEEKS, 20)
        set(value) {
            sp.edit().putInt(KEY_WEEKS, value.coerceIn(1, WeekSpec.MAX_WEEK)).apply()
        }

    /** 今天所在的教学周（超出范围时收敛到 1..totalWeeks） */
    fun currentWeek(): Int {
        val days = ChronoUnit.DAYS.between(semesterStart, LocalDate.now())
        return (days / 7 + 1).toInt().coerceIn(1, totalWeeks)
    }

    companion object {
        private const val KEY_START = "semester_start"
        private const val KEY_WEEKS = "total_weeks"

        fun defaultMonday(): LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    }
}
