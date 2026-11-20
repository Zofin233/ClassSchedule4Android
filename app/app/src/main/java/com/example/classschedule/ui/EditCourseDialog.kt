package com.example.classschedule.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.classschedule.data.CourseEntity
import com.example.classschedule.data.WeekSpec

/** 编辑课程对话框：修改名称 / 地点 / 老师 / 时间 / 周次 / 颜色，支持删除 */
@Composable
fun EditCourseDialog(
    course: CourseEntity,
    onDismiss: () -> Unit,
    onSave: (CourseEntity) -> Unit,
    onDelete: () -> Unit
) {
    val state = rememberCourseFormState(course)
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑课程") },
        text = { CourseFormFields(state) },
        confirmButton = {
            Row {
                TextButton(onClick = { confirmDelete = true }) {
                    Text("删除", color = Color(0xFFD32F2F))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    val err = state.validate()
                    if (err != null) {
                        state.error = err
                    } else {
                        onSave(
                            course.copy(
                                name = state.name.trim(),
                                location = state.location.trim(),
                                teacher = state.teacher.trim(),
                                dayOfWeek = state.day,
                                startSection = state.start,
                                endSection = state.end,
                                weeks = WeekSpec.normalize(state.weeks),
                                colorIndex = state.colorIndex
                            )
                        )
                    }
                }) { Text("保存") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除课程") },
            text = { Text("确定删除「${course.name}」的这个时段吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("删除", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            }
        )
    }
}
