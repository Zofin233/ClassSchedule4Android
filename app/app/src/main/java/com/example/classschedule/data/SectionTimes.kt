package com.example.classschedule.data

/**
 * 默认节次时间表（12 节）。
 */
object SectionTimes {

    const val COUNT = 12

    /** (开始时间, 结束时间)，下标 0 对应第 1 节 */
    val list: List<Pair<String, String>> = listOf(
        "08:30" to "09:15",
        "09:20" to "10:05",
        "10:20" to "11:05",
        "11:10" to "11:55",
        "14:30" to "15:15",
        "15:20" to "16:05",
        "16:20" to "17:05",
        "17:10" to "17:55",
        "19:00" to "19:45",
        "19:50" to "20:35",
        "20:40" to "21:25",
        "21:30" to "22:15"
    )

    fun of(section: Int): Pair<String, String> = list[(section - 1).coerceIn(0, COUNT - 1)]
}
