package com.example.classschedule.ui.theme

import androidx.compose.ui.graphics.Color

/** 课程卡片配色：main = 文字/标题条颜色，background = 卡片底色（同色低饱和） */
data class CourseColor(val main: Color, val background: Color)

object CourseColors {

    val palette: List<CourseColor> = listOf(
        CourseColor(Color(0xFF6C63D8), Color(0xFFE9E8FB)), // 紫
        CourseColor(Color(0xFFB8860B), Color(0xFFFBF2D3)), // 明黄
        CourseColor(Color(0xFF4E9BD8), Color(0xFFE3F1FB)), // 浅蓝
        CourseColor(Color(0xFFD86C9E), Color(0xFFFAE7F0)), // 粉红
        CourseColor(Color(0xFF35A98E), Color(0xFFE1F4EE)), // 青绿
        CourseColor(Color(0xFFB45CC4), Color(0xFFF3E6FA)), // 紫红
        CourseColor(Color(0xFFD8834E), Color(0xFFFBEDE3)), // 橙
        CourseColor(Color(0xFF6B8BB8), Color(0xFFE8EEF7))  // 灰蓝
    )

    fun of(index: Int): CourseColor = palette[index % palette.size]

    /** 非本周课程的灰化样式 */
    val inactive = CourseColor(Color(0xFF9E9E9E), Color(0xFFF2F2F2))
}
