package com.example.studentcompanion

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.ScheduleAdapter
import com.example.studentcompanion.database.StudentDatabase
import com.example.studentcompanion.model.Course
import com.example.studentcompanion.model.Schedule
import com.example.studentcompanion.model.toCourse
import com.example.studentcompanion.model.toSchedule
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * CLASS: ScheduleActivity
 * INHERITANCE: Inherits from AppCompatActivity.
 * Manages the weekly class schedule.
 */
class ScheduleActivity : AppCompatActivity() {

    // UI Components
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddSchedule: FloatingActionButton
    private lateinit var tvCurrentDay: TextView
    private lateinit var tvCurrentDate: TextView

    // Day chips for navigation
    private lateinit var chipMonday: Chip
    private lateinit var chipTuesday: Chip
    private lateinit var chipWednesday: Chip
    private lateinit var chipThursday: Chip
    private lateinit var chipFriday: Chip
    private lateinit var chipSaturday: Chip
    private lateinit var chipSunday: Chip

    // Database and Lists
    private lateinit var database: StudentDatabase
    private val schedulesList = mutableListOf<Schedule>()
    private var coursesList = mutableListOf<Course>()
    private var currentDay = "Monday"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_schedule)

        // Initialize views
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.scheduleRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddSchedule = findViewById(R.id.fabAddSchedule)
        tvCurrentDay = findViewById(R.id.tvCurrentDay)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)

        chipMonday = findViewById(R.id.chipMonday)
        chipTuesday = findViewById(R.id.chipTuesday)
        chipWednesday = findViewById(R.id.chipWednesday)
        chipThursday = findViewById(R.id.chipThursday)
        chipFriday = findViewById(R.id.chipFriday)
        chipSaturday = findViewById(R.id.chipSaturday)
        chipSunday = findViewById(R.id.chipSunday)

        database = StudentDatabase.getDatabase(this)

        // LAMBDA: Toolbar navigation listener
        toolbar.setNavigationOnClickListener {
            finish()
        }

        updateCurrentDate()

        // LAMBDA: Initializing adapter with click and long-press callback lambdas
        adapter = ScheduleAdapter(
            getFilteredSchedules(),
            onScheduleClick = { schedule ->
                showScheduleDialog(schedule)
            },
            onScheduleLongClick = { schedule ->
                showDeleteConfirmation(schedule)
                true // Returns Boolean as required by long-press listener
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // LAMBDA: FAB click listener
        fabAddSchedule.setOnClickListener {
            showScheduleDialog(null)
        }

        setupDayChips()

        loadCourses()
        loadSchedules()
    }

    /**
     * FUNCTION: setupDayChips
     */
    private fun setupDayChips() {
        val chips = listOf(chipMonday, chipTuesday, chipWednesday, chipThursday, chipFriday, chipSaturday, chipSunday)
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        // LAMBDA: .forEachIndexed { ... } using a lambda to iterate through views and set listeners
        chips.forEachIndexed { index, chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentDay = dayNames[index]
                    updateSchedulesList()
                }
            }
        }
    }

    private fun updateCurrentDate() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        currentDay = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }

        tvCurrentDay.text = currentDay
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(calendar.time)

        when (currentDay) {
            "Monday" -> chipMonday.isChecked = true
            "Tuesday" -> chipTuesday.isChecked = true
            "Wednesday" -> chipWednesday.isChecked = true
            "Thursday" -> chipThursday.isChecked = true
            "Friday" -> chipFriday.isChecked = true
            "Saturday" -> chipSaturday.isChecked = true
            "Sunday" -> chipSunday.isChecked = true
        }
    }

    private fun loadCourses() {
        // LAMBDA: Coroutine launch
        lifecycleScope.launch {
            val entities = database.courseDao().getAllCoursesSync()
            coursesList.clear()
            // LAMBDA: .map { ... } transformation lambda
            coursesList.addAll(entities.map { it.toCourse() })
        }
    }

    private fun loadSchedules() {
        // LAMBDA: Coroutine launch
        lifecycleScope.launch {
            val entities = database.scheduleDao().getAllSchedulesSync()
            schedulesList.clear()
            // LAMBDA: .map { ... } transformation lambda
            schedulesList.addAll(entities.map { it.toSchedule() })
            updateSchedulesList()
        }
    }

    /**
     * FUNCTION: showScheduleDialog
     */
    private fun showScheduleDialog(schedule: Schedule?) {
        if (coursesList.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Courses")
                .setMessage("Please add courses first before creating a schedule.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val courseDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.courseDropdown)
        val dayChipGroup = dialogView.findViewById<ChipGroup>(R.id.dayChipGroup)
        val etStartTime = dialogView.findViewById<TextInputEditText>(R.id.etStartTime)
        val etEndTime = dialogView.findViewById<TextInputEditText>(R.id.etEndTime)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // LAMBDA: .map { ... } for dropdown items
        val courseNames = coursesList.map { "${it.courseCode} - ${it.courseName}" }
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courseNames)
        courseDropdown.setAdapter(courseAdapter)

        var selectedCourse: Course? = null
        var startTime = ""
        var endTime = ""

        if (schedule != null) {
            dialogTitle.text = "Edit Schedule"
            // LAMBDA: .find { ... } uses a predicate lambda to locate an item
            selectedCourse = coursesList.find { it.id == schedule.courseId }
            courseDropdown.setText("${schedule.courseCode} - ${schedule.courseName}", false)

            when (schedule.dayOfWeek) {
                "Monday" -> dayChipGroup.check(R.id.chipMon)
                "Tuesday" -> dayChipGroup.check(R.id.chipTue)
                "Wednesday" -> dayChipGroup.check(R.id.chipWed)
                "Thursday" -> dayChipGroup.check(R.id.chipThu)
                "Friday" -> dayChipGroup.check(R.id.chipFri)
                "Saturday" -> dayChipGroup.check(R.id.chipSat)
                "Sunday" -> dayChipGroup.check(R.id.chipSun)
            }

            etStartTime.setText(schedule.startTime)
            etEndTime.setText(schedule.endTime)
            startTime = schedule.startTime
            endTime = schedule.endTime
            btnSave.text = "Update Schedule"
        }

        // LAMBDA: Dropdown item click listener
        courseDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedCourse = coursesList[position]
        }

        // LAMBDA: Time pickers with custom result lambdas
        etStartTime.setOnClickListener {
            showTimePicker { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                etStartTime.setText(startTime)
            }
        }

        etEndTime.setOnClickListener {
            showTimePicker { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                etEndTime.setText(endTime)
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            if (selectedCourse == null) {
                courseDropdown.error = "Please select a course"
                return@setOnClickListener
            }
            if (startTime.isEmpty() || endTime.isEmpty()) {
                return@setOnClickListener
            }

            val selectedDay = when (dayChipGroup.checkedChipId) {
                R.id.chipMon -> "Monday"
                R.id.chipTue -> "Tuesday"
                R.id.chipWed -> "Wednesday"
                R.id.chipThu -> "Thursday"
                R.id.chipFri -> "Friday"
                R.id.chipSat -> "Saturday"
                R.id.chipSun -> "Sunday"
                else -> "Monday"
            }

            val course = selectedCourse!!

            if (schedule == null) {
                val newSchedule = Schedule(0, course.id, course.courseCode, course.courseName, course.instructor, course.room, selectedDay, startTime, endTime, course.color)
                lifecycleScope.launch {
                    database.scheduleDao().insert(newSchedule.toEntity())
                    loadSchedules()
                }
            } else {
                val updatedSchedule = schedule.copy(courseId = course.id, courseCode = course.courseCode, courseName = course.courseName, instructor = course.instructor, room = course.room, dayOfWeek = selectedDay, startTime = startTime, endTime = endTime, color = course.color)
                lifecycleScope.launch {
                    database.scheduleDao().update(updatedSchedule.toEntity())
                    loadSchedules()
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * FUNCTION: showTimePicker
     * USES LAMBDA: Accepts a lambda 'onTimeSet' to handle the selected time result
     */
    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        // LAMBDA: TimePickerDialog listener using a lambda
        TimePickerDialog(this, { _, hour, minute -> onTimeSet(hour, minute) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun showDeleteConfirmation(schedule: Schedule) {
        AlertDialog.Builder(this)
            .setTitle("Delete Schedule")
            .setMessage("Remove ${schedule.courseCode} from ${schedule.dayOfWeek}?")
            // LAMBDA: Dialog positive button listener
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.scheduleDao().delete(schedule.toEntity())
                    loadSchedules()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * FUNCTION: getFilteredSchedules
     * USES LAMBDA: .filter { ... } and .sortedBy { ... }
     */
    private fun getFilteredSchedules(): List<Schedule> {
        return schedulesList
            .filter { it.dayOfWeek == currentDay }
            .sortedBy { it.startTime }
    }

    private fun updateSchedulesList() {
        adapter.updateSchedules(getFilteredSchedules())
        updateUI()
    }

    private fun updateUI() {
        val filteredList = getFilteredSchedules()
        if (filteredList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}