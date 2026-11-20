package com.example.classschedule.cdl

import com.example.classschedule.data.SectionTimes
import com.example.classschedule.data.WeekSpec

/** 一条解析成功的上课时段记录 */
data class CdlRecord(
    val name: String,
    val location: String,
    val weeks: String,       // 归一化后的周次表达式
    val dayOfWeek: Int,      // 1=周一 … 7=周日
    val startSection: Int,
    val endSection: Int,
    val teacher: String = "" // 任课老师（可选）
)

/** 解析错误：行号 + 原文 + 原因 */
data class CdlError(val line: Int, val content: String, val reason: String)

data class CdlResult(val records: List<CdlRecord>, val errors: List<CdlError>)

/**
 * 课程描述语言（CDL）解析器。
 *
 * 语法：课程名 @ 地点 | 周次 | 星期 | 节次 [| 老师]
 * 示例：大学物理II @ 立人楼B107 | 1-16 | 周二 | 3-4 | 付传技
 *
 * 规则：
 * - 每行一条记录；空行与 # 开头的注释行忽略
 * - 地点可省略（不写 @ 部分）；老师可省略（不写第 5 段）
 * - 周次支持 "1-16"、"1-8,10-16"、"2,4,6"
 * - 节次支持 "5"、"3-4"（连续节次合并为一个大块）
 * - 同一门课多个时段写多行，课程名相同即同一门课
 */
object CdlParser {

    private val DAYS: Map<String, Int> = mapOf(
        "周一" to 1, "星期一" to 1,
        "周二" to 2, "星期二" to 2,
        "周三" to 3, "星期三" to 3,
        "周四" to 4, "星期四" to 4,
        "周五" to 5, "星期五" to 5,
        "周六" to 6, "星期六" to 6,
        "周日" to 7, "星期日" to 7, "周天" to 7
    )

    fun parse(text: String): CdlResult {
        val records = mutableListOf<CdlRecord>()
        val errors = mutableListOf<CdlError>()
        text.lines().forEachIndexed { index, raw ->
            val lineNo = index + 1
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed
            try {
                records.add(parseLine(line))
            } catch (e: IllegalArgumentException) {
                errors.add(CdlError(lineNo, line, e.message ?: "解析失败"))
            }
        }
        return CdlResult(records, errors)
    }

    private fun parseLine(line: String): CdlRecord {
        val parts = line.split('|').map { it.trim() }
        if (parts.size !in 4..5) {
            throw IllegalArgumentException("应为 4~5 段：课程名@地点 | 周次 | 星期 | 节次 [| 老师]（实际 ${parts.size} 段）")
        }

        // 课程名 @ 地点
        val namePart = parts[0]
        val at = namePart.lastIndexOf('@')
        val name: String
        val location: String
        if (at >= 0) {
            name = namePart.substring(0, at).trim()
            location = namePart.substring(at + 1).trim()
        } else {
            name = namePart
            location = ""
        }
        if (name.isEmpty()) throw IllegalArgumentException("课程名为空")

        // 周次（校验并归一化）
        val weeks = try {
            WeekSpec.normalize(parts[1])
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("周次：${e.message}")
        }

        // 星期
        val day = DAYS[parts[2]]
            ?: throw IllegalArgumentException("星期无效：${parts[2]}（应为 周一~周日）")

        // 节次
        val (start, end) = parseSections(parts[3])

        // 老师（可选第 5 段）
        val teacher = parts.getOrNull(4) ?: ""

        return CdlRecord(name, location, weeks, day, start, end, teacher)
    }

    private fun parseSections(raw: String): Pair<Int, Int> {
        val s = raw.trim().removeSuffix("节").trim()
        val nums = s.split('-', '~').map {
            it.trim().toIntOrNull() ?: throw IllegalArgumentException("节次不是数字：$raw")
        }
        val (a, b) = when (nums.size) {
            1 -> nums[0] to nums[0]
            2 -> nums[0] to nums[1]
            else -> throw IllegalArgumentException("节次格式错误：$raw")
        }
        if (a < 1 || b > SectionTimes.COUNT || a > b) {
            throw IllegalArgumentException("节次范围无效：$raw（应在 1-${SectionTimes.COUNT} 内）")
        }
        return a to b
    }
}
