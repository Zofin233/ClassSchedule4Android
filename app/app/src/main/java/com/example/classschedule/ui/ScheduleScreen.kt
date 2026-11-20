package com.example.classschedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.classschedule.ScheduleViewModel
import com.example.classschedule.data.CourseEntity
import com.example.classschedule.data.SectionTimes
import com.example.classschedule.data.WeekSpec
import com.example.classschedule.ui.theme.CourseColors
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

private val SectionHeight = 64.dp
private val TimeColumnWidth = 52.dp
private val Hairline = Color(0xFFF3F3F3)

/** 拖拽选中的节次范围（用于空白处滑动添加课程） */
private data class DragSelection(val day: Int, val start: Int, val end: Int)

/** 周视图课程表主界面 */
@Composable
fun ScheduleScreen(vm: ScheduleViewModel) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val week by vm.week.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    var addPrefill by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
    var detailCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var editCourse by remember { mutableStateOf<CourseEntity?>(null) }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        TopBar(
            week = week,
            totalWeeks = vm.totalWeeks,
            semesterLabel = vm.semesterLabel(),
            onWeekChange = vm::setWeek,
            onAdd = { showAdd = true },
            onImport = { showImport = true }
        )
        WeekHeader(monday = vm.mondayOf(week))
        HorizontalDivider(color = Color(0xFFEEEEEE))
        ScheduleGrid(
            courses = courses,
            week = week,
            onSwipeWeek = { delta -> vm.setWeek(week + delta) },
            onCourseClick = { detailCourse = it },
            onEmptyDragSelect = { day, s, e ->
                addPrefill = Triple(day, s, e)
                showAdd = true
            }
        )
    }

    if (showAdd) {
        val p = addPrefill
        AddCourseDialog(
            prefillDay = p?.first,
            prefillStart = p?.second,
            prefillEnd = p?.third,
            onDismiss = {
                showAdd = false
                addPrefill = null
            },
            onSave = { name, loc, teacher, day, s, e, weeks, color ->
                vm.addCourse(name, loc, teacher, day, s, e, weeks, color)
                showAdd = false
                addPrefill = null
            }
        )
    }
    if (showImport) {
        ImportDialog(
            onDismiss = { showImport = false },
            onImport = { records ->
                vm.import(records)
                showImport = false
            }
        )
    }
    detailCourse?.let { c ->
        CourseDetailDialog(
            course = c,
            onDismiss = { detailCourse = null },
            onEdit = {
                editCourse = c
                detailCourse = null
            }
        )
    }
    editCourse?.let { c ->
        EditCourseDialog(
            course = c,
            onDismiss = { editCourse = null },
            onSave = { updated ->
                vm.updateCourse(updated)
                editCourse = null
            },
            onDelete = {
                vm.deleteCourse(c.id)
                editCourse = null
            }
        )
    }
}

/** 顶部栏：周次选择器 + 学期信息 + 右侧「+」菜单（导入课程 / 添加课程） */
@Composable
private fun TopBar(
    week: Int,
    totalWeeks: Int,
    semesterLabel: String,
    onWeekChange: (Int) -> Unit,
    onAdd: () -> Unit,
    onImport: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 周次下拉
        Box {
            var weekMenu by remember { mutableStateOf(false) }
            Row(
                Modifier.clickable { weekMenu = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("第${week}周", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "切换周次",
                            tint = Color(0xFF111111)
                        )
                    }
                    Text(semesterLabel, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
            DropdownMenu(expanded = weekMenu, onDismissRequest = { weekMenu = false }) {
                (1..totalWeeks).forEach { w ->
                    DropdownMenuItem(
                        text = { Text("第${w}周") },
                        onClick = {
                            onWeekChange(w)
                            weekMenu = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // 「+」按钮，点击展开菜单
        Box {
            var addMenu by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF111111))
                    .clickable { addMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加", tint = Color.White)
            }
            DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                DropdownMenuItem(
                    text = { Text("导入课程") },
                    onClick = {
                        addMenu = false
                        onImport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("添加课程") },
                    onClick = {
                        addMenu = false
                        onAdd()
                    }
                )
            }
        }
    }
}

/** 周日期表头：左端月份 + 七列日期，当天反白高亮 */
@Composable
private fun WeekHeader(monday: LocalDate) {
    val today = LocalDate.now()
    val dayChars = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.width(TimeColumnWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${monday.monthValue}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
            Text("月", fontSize = 11.sp, color = Color(0xFF666666))
        }
        dayChars.forEachIndexed { i, c ->
            val date = monday.plusDays(i.toLong())
            val isToday = date == today
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(c, fontSize = 12.sp, color = Color(0xFF999999))
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isToday) Color(0xFF111111) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${date.dayOfMonth}",
                        fontSize = 14.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

/**
 * 课表网格：左侧时间轴 + 7 天课程列。
 * - 左右滑动：切换周
 * - 空白处上下滑动：选中节次范围并打开添加课程
 * - 点击课程卡片：查看详情
 */
@Composable
private fun ScheduleGrid(
    courses: List<CourseEntity>,
    week: Int,
    onSwipeWeek: (Int) -> Unit,
    onCourseClick: (CourseEntity) -> Unit,
    onEmptyDragSelect: (Int, Int, Int) -> Unit
) {
    var selection by remember { mutableStateOf<DragSelection?>(null) }

    Row(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(week) {
                // 左右滑动切换周
                var total = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (total <= -160f) onSwipeWeek(1)
                        else if (total >= 160f) onSwipeWeek(-1)
                        total = 0f
                    },
                    onDragCancel = { total = 0f },
                    onHorizontalDrag = { _, dragAmount -> total += dragAmount }
                )
            }
    ) {
        // 时间轴
        Column(Modifier.width(TimeColumnWidth)) {
            repeat(SectionTimes.COUNT) { i ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(SectionHeight)
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                    val (start, end) = SectionTimes.of(i + 1)
                    Text(start, fontSize = 8.sp, color = Color(0xFFAAAAAA), lineHeight = 10.sp)
                    Text(end, fontSize = 8.sp, color = Color(0xFFAAAAAA), lineHeight = 10.sp)
                }
            }
        }

        // 每天一列
        for (day in 1..7) {
            val dayCourses = courses.filter { it.dayOfWeek == day }
            Box(
                Modifier
                    .weight(1f)
                    .height(SectionHeight * SectionTimes.COUNT)
                    .pointerInput(day) {
                        // 空白处上下滑动选择节次范围
                        val cellPx = SectionHeight.toPx()
                        var selStart = -1
                        var selEnd = -1
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                selStart = ((offset.y / cellPx).toInt() + 1)
                                    .coerceIn(1, SectionTimes.COUNT)
                                selEnd = selStart
                                selection = DragSelection(day, selStart, selEnd)
                            },
                            onVerticalDrag = { change, _ ->
                                selEnd = ((change.position.y / cellPx).toInt() + 1)
                                    .coerceIn(1, SectionTimes.COUNT)
                                selection = DragSelection(day, min(selStart, selEnd), max(selStart, selEnd))
                                change.consume()
                            },
                            onDragEnd = {
                                if (selStart > 0) {
                                    onEmptyDragSelect(day, min(selStart, selEnd), max(selStart, selEnd))
                                }
                                selection = null
                            },
                            onDragCancel = { selection = null }
                        )
                    }
            ) {
                // 背景网格线
                Column(Modifier.fillMaxSize()) {
                    repeat(SectionTimes.COUNT) {
                        Box(Modifier.fillMaxWidth().height(SectionHeight)) {
                            HorizontalDivider(
                                Modifier.align(Alignment.BottomCenter),
                                thickness = 0.5.dp,
                                color = Hairline
                            )
                        }
                    }
                }
                // 拖拽选区高亮
                selection?.takeIf { it.day == day }?.let { sel ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .offset(y = SectionHeight * (sel.start - 1))
                            .height(SectionHeight * (sel.end - sel.start + 1))
                            .padding(horizontal = 1.5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1A111111))
                            .border(1.dp, Color(0xFF111111), RoundedCornerShape(8.dp))
                    )
                }
                // 课程卡片：非本周的先画，本周的在上层
                val sorted = dayCourses.sortedBy { WeekSpec.contains(it.weeks, week) }
                sorted.forEach { course ->
                    val active = WeekSpec.contains(course.weeks, week)
                    val overlaps = overlapCount(course, dayCourses, week)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .offset(y = SectionHeight * (course.startSection - 1))
                            .height(SectionHeight * (course.endSection - course.startSection + 1))
                            .padding(horizontal = 1.5.dp, vertical = 1.dp)
                            .clickable { onCourseClick(course) }
                    ) {
                        CourseCard(
                            course = course,
                            activeThisWeek = active,
                            badge = if (active && overlaps > 1) overlaps else null
                        )
                    }
                }
            }
        }
    }
}

/** 同一时段本周重叠的课程数（含自身），>1 时显示角标 */
private fun overlapCount(target: CourseEntity, dayCourses: List<CourseEntity>, week: Int): Int {
    var n = 1
    for (c in dayCourses) {
        if (c.id == target.id) continue
        if (!WeekSpec.contains(c.weeks, week)) continue
        if (c.startSection <= target.endSection && target.startSection <= c.endSection) n++
    }
    return n
}

/** 课程卡片：顶部色条 + 节次范围 + 课程名@地点，非本周灰化 */
@Composable
private fun CourseCard(course: CourseEntity, activeThisWeek: Boolean, badge: Int?) {
    val color = if (activeThisWeek) CourseColors.of(course.colorIndex) else CourseColors.inactive
    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(color.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 顶部色条（非本周显示「非本周」标签条）
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (activeThisWeek) 5.dp else 12.dp)
                    .background(if (activeThisWeek) color.main else Color(0xFFCFCFCF)),
                contentAlignment = Alignment.Center
            ) {
                if (!activeThisWeek) {
                    Text("非本周", fontSize = 7.sp, color = Color(0xFF888888), lineHeight = 8.sp)
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    course.sectionText(),
                    fontSize = 8.sp,
                    color = color.main,
                    lineHeight = 10.sp
                )
                Text(
                    course.displayText(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.main,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 8,
                    lineHeight = 12.sp
                )
            }
        }
        // 时段多课角标
        if (badge != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4E9BD8)),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
