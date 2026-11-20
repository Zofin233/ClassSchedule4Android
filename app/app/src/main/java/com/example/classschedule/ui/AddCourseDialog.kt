package com.example.classschedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classschedule.data.CourseEntity
import com.example.classschedule.data.SectionTimes
import com.example.classschedule.data.WeekSpec
import com.example.classschedule.ui.theme.CourseColors

/** 课程表单状态，添加与编辑共用 */
class CourseFormState(
    name: String = "",
    location: String = "",
    teacher: String = "",
    day: Int = 1,
    start: Int = 1,
    end: Int = 2,
    weeks: String = "1-16",
    colorIndex: Int = 0
) {
    var name by mutableStateOf(name)
    var location by mutableStateOf(location)
    var teacher by mutableStateOf(teacher)
    var day by mutableIntStateOf(day)
    var start by mutableIntStateOf(start)
    var end by mutableIntStateOf(end)
    var weeks by mutableStateOf(weeks)
    var colorIndex by mutableIntStateOf(colorIndex)
    var error by mutableStateOf<String?>(null)

    /** 校验并生成错误信息；null 表示通过 */
    fun validate(): String? = when {
        name.isBlank() -> "课程名不能为空"
        !WeekSpec.isValid(weeks) -> "周次格式无效，应为如 1-16 或 1-8,10-16"
        else -> null
    }
}

@Composable
fun rememberCourseFormState(
    name: String = "",
    location: String = "",
    teacher: String = "",
    day: Int = 1,
    start: Int = 1,
    end: Int = 2,
    weeks: String = "1-16",
    colorIndex: Int = 0
): CourseFormState = remember {
    CourseFormState(name, location, teacher, day, start, end, weeks, colorIndex)
}

@Composable
fun rememberCourseFormState(course: CourseEntity): CourseFormState = remember(course.id) {
    CourseFormState(
        name = course.name,
        location = course.location,
        teacher = course.teacher,
        day = course.dayOfWeek,
        start = course.startSection,
        end = course.endSection,
        weeks = course.weeks,
        colorIndex = course.colorIndex
    )
}

/** 课程表单字段：名称/地点/老师/星期/节次/周次/颜色 */
@Composable
fun CourseFormFields(state: CourseFormState) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = { state.name = it },
            label = { Text("课程名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.location,
            onValueChange = { state.location = it },
            label = { Text("地点（可空）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.teacher,
            onValueChange = { state.teacher = it },
            label = { Text("老师（可空）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Selector(
                label = "星期",
                options = (1..7).map { "周${listOf("一", "二", "三", "四", "五", "六", "日")[it - 1]}" },
                selected = state.day - 1,
                onSelect = { state.day = it + 1 },
                modifier = Modifier.weight(1f)
            )
            Selector(
                label = "开始节",
                options = (1..SectionTimes.COUNT).map { "第${it}节" },
                selected = state.start - 1,
                onSelect = {
                    state.start = it + 1
                    if (state.end < state.start) state.end = state.start
                },
                modifier = Modifier.weight(1f)
            )
            Selector(
                label = "结束节",
                options = (1..SectionTimes.COUNT).map { "第${it}节" },
                selected = state.end - 1,
                onSelect = {
                    state.end = it + 1
                    if (state.start > state.end) state.start = state.end
                },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = state.weeks,
            onValueChange = { state.weeks = it },
            label = { Text("周次（如 1-16 或 1-8,10-16）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // 颜色选择
        Text("颜色", fontSize = 13.sp, color = Color(0xFF666666))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CourseColors.palette.forEachIndexed { i, c ->
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(c.main)
                        .border(
                            width = if (i == state.colorIndex) 3.dp else 0.dp,
                            color = if (i == state.colorIndex) Color(0xFF111111) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { state.colorIndex = i }
                )
            }
        }
        state.error?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
    }
}

/** 添加单门课程对话框；prefill* 用于从课表空白拖拽选区预填 */
@Composable
fun AddCourseDialog(
    prefillDay: Int? = null,
    prefillStart: Int? = null,
    prefillEnd: Int? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, location: String, teacher: String, day: Int, start: Int, end: Int, weeks: String, colorIndex: Int) -> Unit
) {
    val state = rememberCourseFormState(
        day = prefillDay ?: 1,
        start = prefillStart ?: 1,
        end = prefillEnd ?: (prefillStart ?: 1)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加课程") },
        text = { CourseFormFields(state) },
        confirmButton = {
            TextButton(onClick = {
                val err = state.validate()
                if (err != null) {
                    state.error = err
                } else {
                    onSave(
                        state.name.trim(), state.location.trim(), state.teacher.trim(),
                        state.day, state.start, state.end, state.weeks.trim(), state.colorIndex
                    )
                }
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 通用下拉选择器（Box + DropdownMenu，避免实验性 API） */
@Composable
fun Selector(
    label: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(label, fontSize = 10.sp, color = Color(0xFF999999))
            Text(options.getOrElse(selected) { "" }, fontSize = 14.sp, color = Color(0xFF111111))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { i, opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(i)
                        expanded = false
                    }
                )
            }
        }
    }
}
