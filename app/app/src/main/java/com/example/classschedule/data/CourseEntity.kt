package com.example.classschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一门课的一个上课时段。
 * 同一门课的多个时段存多条记录（课程名相同视为同一门课）。
 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,       // 可为空字符串
    val teacher: String = "",   // 任课老师，可为空字符串
    val dayOfWeek: Int,         // 1=周一 … 7=周日
    val startSection: Int,      // 起始节次（连续节次已合并为一段）
    val endSection: Int,        // 结束节次
    val weeks: String,          // 周次表达式，如 "1-8,10-16"
    val colorIndex: Int         // 课程配色索引
) {
    /** 卡片显示文本：课程名@地点 */
    fun displayText(): String =
        if (location.isBlank()) name else "$name@$location"

    /** 节次范围文本：1-2节 */
    fun sectionText(): String =
        if (startSection == endSection) "${startSection}节" else "$startSection-$endSection 节"
}
