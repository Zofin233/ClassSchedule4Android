package com.example.classschedule.data

/**
 * 周次表达式解析，支持："1-16"、"1-8,10-16"、"2,4,6"、"1-16周"（容忍中文逗号、"~"、结尾"周"字）。
 */
object WeekSpec {

    const val MAX_WEEK = 30

    fun parse(spec: String): Set<Int> {
        val out = sortedSetOf<Int>()
        val s = spec.trim().replace('，', ',').removeSuffix("周").trim()
        if (s.isEmpty()) throw IllegalArgumentException("周次为空")
        for (token in s.split(',')) {
            val t = token.trim()
            if (t.isEmpty()) continue
            if (t.contains('-') || t.contains('~')) {
                val parts = t.split('-', '~').map { it.trim() }
                if (parts.size != 2) throw IllegalArgumentException("周次格式错误：$t")
                val a = parts[0].toIntOrNull() ?: throw IllegalArgumentException("周次不是数字：$t")
                val b = parts[1].toIntOrNull() ?: throw IllegalArgumentException("周次不是数字：$t")
                if (a < 1 || b > MAX_WEEK || a > b) throw IllegalArgumentException("周次范围无效：$t（应在 1-$MAX_WEEK 内）")
                out.addAll(a..b)
            } else {
                val n = t.toIntOrNull() ?: throw IllegalArgumentException("周次不是数字：$t")
                if (n < 1 || n > MAX_WEEK) throw IllegalArgumentException("周次超出范围：$t")
                out.add(n)
            }
        }
        if (out.isEmpty()) throw IllegalArgumentException("周次为空")
        return out
    }

    fun isValid(spec: String): Boolean = try {
        parse(spec); true
    } catch (e: IllegalArgumentException) {
        false
    }

    fun contains(spec: String, week: Int): Boolean = try {
        parse(spec).contains(week)
    } catch (e: IllegalArgumentException) {
        false
    }

    /** 归一化为 "1-8,10-16" 形式 */
    fun normalize(spec: String): String {
        val set = parse(spec).sorted()
        val parts = mutableListOf<String>()
        var i = 0
        while (i < set.size) {
            var j = i
            while (j + 1 < set.size && set[j + 1] == set[j] + 1) j++
            parts.add(if (i == j) "${set[i]}" else "${set[i]}-${set[j]}")
            i = j + 1
        }
        return parts.joinToString(",")
    }
}
