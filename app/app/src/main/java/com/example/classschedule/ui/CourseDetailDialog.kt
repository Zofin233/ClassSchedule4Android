package com.example.classschedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.classschedule.data.CourseEntity
import com.example.classschedule.data.SectionTimes
import com.example.classschedule.data.WeekSpec

private val DayChars = listOf("一", "二", "三", "四", "五", "六", "日")

/** 课程详情卡：大标题 + 编辑入口 + 周次 / 时间 / 教室 / 老师 */
@Composable
fun CourseDetailDialog(
    course: CourseEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                // 标题行：课程名 + 编辑按钮
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        course.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = onEdit) {
                        Text("编辑", fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(14.dp))

                // 周次行：第1-4周 | 第6-16周
                DetailRow(
                    label = "周次",
                    value = WeekSpec.normalize(course.weeks)
                        .split(",")
                        .joinToString("  |  ") { "第${it}周" }
                )
                // 时间行：周四 | 第5-6节 | 14:30-16:05
                val (startTime, endTime) = SectionTimes.of(course.startSection).first to
                        SectionTimes.of(course.endSection).second
                val sectionText = if (course.startSection == course.endSection)
                    "第${course.startSection}节"
                else
                    "第${course.startSection}-${course.endSection}节"
                DetailRow(
                    label = "时间",
                    value = "周${DayChars[course.dayOfWeek - 1]}  |  $sectionText  |  $startTime-$endTime"
                )
                // 教室行
                if (course.location.isNotBlank()) {
                    DetailRow(label = "教室", value = course.location)
                }
                // 老师行
                if (course.teacher.isNotBlank()) {
                    DetailRow(label = "老师", value = course.teacher)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 5.dp)) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(end = 14.dp)
        )
        Text(value, fontSize = 14.sp, color = Color(0xFF222222))
    }
}
