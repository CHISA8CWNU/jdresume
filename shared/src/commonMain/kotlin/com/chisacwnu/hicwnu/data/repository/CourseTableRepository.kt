/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import androidx.room3.Transaction
import com.chisacwnu.hicwnu.data.db.main.Course
import com.chisacwnu.hicwnu.data.db.main.CourseDao
import com.chisacwnu.hicwnu.data.db.main.CourseTable
import com.chisacwnu.hicwnu.data.db.main.CourseTableConfig
import com.chisacwnu.hicwnu.data.db.main.CourseTableDao
import com.chisacwnu.hicwnu.data.db.main.CourseWeek
import com.chisacwnu.hicwnu.data.db.main.CourseWeekDao
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Single
class CourseTableRepository(
    private val courseTableDao: CourseTableDao,
    private val courseDao: CourseDao,
    private val courseWeekDao: CourseWeekDao,
    private val timeSlotRepository: TimeSlotRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
                repositoryScope.launch {
            seedDefaultData()
        }
    }

    
    private suspend fun seedDefaultData() {
        if (courseTableDao.getAllCourseTables().first().isNotEmpty()) {
            return
        }

        val tableId = Uuid.random().toString()
        val defaultCourseTable = CourseTable(
            id = tableId,
            name = "我的课表",
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
        courseTableDao.insert(defaultCourseTable)

        val defaultConfig = CourseTableConfig(
            courseTableId = tableId,
            showWeekends = true,
            semesterTotalWeeks = 20,
            defaultClassDuration = 45,
            defaultBreakDuration = 10,
            firstDayOfWeek = 1
        )
        appSettingsRepository.insertOrUpdateCourseConfig(defaultConfig)

        val defaultTimeSlotsForNewTable = defaultTimeSlots.map {
            it.copy(courseTableId = tableId)
        }
        timeSlotRepository.insertAll(defaultTimeSlotsForNewTable)

        println("数据库初始化数据已完成写入")
    }

    
    fun getAllCourseTables(): Flow<List<CourseTable>> {
        return courseTableDao.getAllCourseTables()
    }

    
    fun getCoursesWithWeeksByTableId(tableId: String): Flow<List<CourseWithWeeks>> {
        return courseDao.getCoursesWithWeeksByTableId(tableId)
    }

    
    @Transaction
    suspend fun createNewCourseTable(name: String) {
        val newTable = CourseTable(
            id = Uuid.random().toString(),
            name = name,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
        courseTableDao.insert(newTable)

                val defaultTimeSlotsForNewTable = defaultTimeSlots.map {
            it.copy(courseTableId = newTable.id)
        }
                timeSlotRepository.insertAll(defaultTimeSlotsForNewTable)

                val newConfig = CourseTableConfig(courseTableId = newTable.id)
        appSettingsRepository.insertOrUpdateCourseConfig(newConfig)
    }

    
    suspend fun updateCourseTable(courseTable: CourseTable) {
        courseTableDao.update(courseTable)
    }

    
    suspend fun deleteCourseTable(courseTable: CourseTable): Boolean {
        val allTables = courseTableDao.getAllCourseTables().first()
        if (allTables.size <= 1) {
            return false
        }
        courseTableDao.delete(courseTable)
        return true
    }

    
    suspend fun updateCourseColor(courseId: String, newColorInt: Int) {
        courseDao.updateCourseColorById(courseId, newColorInt)
    }

    
    @Transaction
    suspend fun upsertCourse(course: Course, weekNumbers: List<Int>) {
                val safeRemark = course.remark?.take(300)?.ifBlank { null }

                val processedCourse = course.copy(remark = safeRemark)

                if (courseDao.exists(processedCourse.id)) {
                        courseDao.update(processedCourse)
        } else {
                        courseDao.insertAll(listOf(processedCourse))
        }

                val courseWeeks = weekNumbers.map { week ->
            CourseWeek(courseId = processedCourse.id, weekNumber = week)
        }
        courseWeekDao.updateCourseWeeks(processedCourse.id, courseWeeks)
    }

    
    suspend fun deleteCourse(course: Course) {
        courseDao.delete(course)
    }

    
    suspend fun deleteCoursesByIds(courseIds: List<String>) {
        if (courseIds.isEmpty()) return
        courseDao.deleteCoursesByIds(courseIds)
    }

    
    suspend fun deleteCoursesByNames(tableId: String, courseNames: List<String>) {
        if (courseNames.isEmpty() || tableId.isBlank()) return
        courseDao.deleteCoursesByNames(tableId, courseNames)
    }

        enum class TweakMode {
        MERGE,              OVERWRITE,          EXCHANGE        }

    
    @Transaction
    suspend fun tweakCoursesOnDate(
        mode: TweakMode,
        courseTableId: String,
        fromWeek: Int,
        fromDay: Int,
        toWeek: Int,
        toDay: Int
    ) {
                val sourceList = courseDao.getCoursesWithWeeksByDayAndWeek(courseTableId, fromDay, fromWeek).first()
        val targetList = courseDao.getCoursesWithWeeksByDayAndWeek(courseTableId, toDay, toWeek).first()

        when (mode) {
            TweakMode.MERGE -> {
                                executeMoveInternal(sourceList, toWeek, toDay, fromWeek)
            }

            TweakMode.OVERWRITE -> {
                                if (targetList.isNotEmpty()) {
                    val targetIds = targetList.map { it.course.id }
                    courseWeekDao.deleteCourseWeeksForCourseAndWeek(targetIds, toWeek)
                }
                executeMoveInternal(sourceList, toWeek, toDay, fromWeek)
            }

            TweakMode.EXCHANGE -> {
                                if (sourceList.isNotEmpty()) {
                    courseWeekDao.deleteCourseWeeksForCourseAndWeek(sourceList.map { it.course.id }, fromWeek)
                }
                if (targetList.isNotEmpty()) {
                    courseWeekDao.deleteCourseWeeksForCourseAndWeek(targetList.map { it.course.id }, toWeek)
                }
                                executeMoveInternal(sourceList, toWeek, toDay, -1)
                executeMoveInternal(targetList, fromWeek, fromDay, -1)
            }
        }
    }

    
    private suspend fun executeMoveInternal(
        items: List<CourseWithWeeks>,
        targetWeek: Int,
        targetDay: Int,
        originalWeek: Int
    ) {
        if (items.isEmpty()) return

        val newCourses = mutableListOf<Course>()
        val newWeeks = mutableListOf<CourseWeek>()
        val oldIdsToRemove = mutableListOf<String>()

        for (item in items) {
            val course = item.course
                        val isSingleWeek = item.weeks.size <= 1

            if (isSingleWeek) {
                                courseDao.update(course.copy(day = targetDay))
                if (originalWeek != -1) {
                    courseWeekDao.deleteCourseWeeksForCourseAndWeek(listOf(course.id), originalWeek)
                }
                courseWeekDao.insertAll(listOf(CourseWeek(courseId = course.id, weekNumber = targetWeek)))
            } else {
                                val newId = Uuid.random().toString()
                newCourses.add(course.copy(id = newId, day = targetDay))
                newWeeks.add(CourseWeek(courseId = newId, weekNumber = targetWeek))
                if (originalWeek != -1) oldIdsToRemove.add(course.id)
            }
        }

                if (oldIdsToRemove.isNotEmpty()) {
            courseWeekDao.deleteCourseWeeksForCourseAndWeek(oldIdsToRemove, originalWeek)
        }
        if (newCourses.isNotEmpty()) {
            courseDao.insertAll(newCourses)
            courseWeekDao.insertAll(newWeeks)
        }
    }

    
    @Transaction
    suspend fun deleteCoursesOnDates(
        tableId: String,
        weekDayPairs: List<Pair<Int, Int>>
    ) {
        weekDayPairs.forEach { (week, day) ->
                        val coursesToDelete = getCoursesForDay(tableId, week, day).first()

            if (coursesToDelete.isNotEmpty()) {
                val ids = coursesToDelete.map { it.course.id }

                                courseWeekDao.deleteCourseWeeksForCourseAndWeek(ids, week)
            }
        }
    }

    
    fun getCoursesForDay(
        courseTableId: String,
        weekNumber: Int,
        day: Int
    ): Flow<List<CourseWithWeeks>> {
                return courseDao.getCoursesWithWeeksByDayAndWeek(
            courseTableId = courseTableId,
            day = day,
            weekNumber = weekNumber
        )
    }

    
    fun getCoursesWithWeeksByDate(
        courseTableId: String,
        targetDate: LocalDate,
        config: CourseTableConfig
    ): Flow<List<CourseWithWeeks>> {
        val weekNumber = appSettingsRepository.getWeekIndexAtDate(
            targetDate = targetDate,
            startDateStr = config.semesterStartDate,
            firstDayOfWeekInt = config.firstDayOfWeek
        )

                if (weekNumber == null) {
            return flowOf(emptyList())
        }

                return courseDao.getCoursesWithWeeksByTableAndWeek(courseTableId, weekNumber)
    }
}

private val defaultTimeSlots = listOf(
    TimeSlot(number = 1, startTime = "08:00", endTime = "08:45", courseTableId = "placeholder"),
    TimeSlot(number = 2, startTime = "08:50", endTime = "09:35", courseTableId = "placeholder"),
    TimeSlot(number = 3, startTime = "09:50", endTime = "10:35", courseTableId = "placeholder"),
    TimeSlot(number = 4, startTime = "10:40", endTime = "11:25", courseTableId = "placeholder"),
    TimeSlot(number = 5, startTime = "11:30", endTime = "12:15", courseTableId = "placeholder"),
    TimeSlot(number = 6, startTime = "14:00", endTime = "14:45", courseTableId = "placeholder"),
    TimeSlot(number = 7, startTime = "14:50", endTime = "15:35", courseTableId = "placeholder"),
    TimeSlot(number = 8, startTime = "15:45", endTime = "16:30", courseTableId = "placeholder"),
    TimeSlot(number = 9, startTime = "16:35", endTime = "17:20", courseTableId = "placeholder"),
    TimeSlot(number = 10, startTime = "18:30", endTime = "19:15", courseTableId = "placeholder"),
    TimeSlot(number = 11, startTime = "19:20", endTime = "20:05", courseTableId = "placeholder"),
    TimeSlot(number = 12, startTime = "20:10", endTime = "20:55", courseTableId = "placeholder"),
    TimeSlot(number = 13, startTime = "21:10", endTime = "21:55", courseTableId = "placeholder")
)