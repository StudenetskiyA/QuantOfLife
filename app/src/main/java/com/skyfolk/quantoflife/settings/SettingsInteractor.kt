package com.skyfolk.quantoflife.settings

import android.content.Context
import android.content.SharedPreferences
import com.skyfolk.quantoflife.GraphSelectedYear
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.meansure.Measure
import com.skyfolk.quantoflife.meansure.QuantFilter
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsInteractor(private val context: Context) {
    companion object {
        const val SELECTED_RADIO_IN_STATISTIC = "selected_radio_in_statistic_2"
        const val SELECTED_TIME_START = "selected_time_start"
        const val SELECTED_TIME_END = "selected_time_end"
        const val CATEGORY_NAME_ = "category_name_"
        const val ONBOARDING_COMPLETE = "onboarding_complete"
        const val START_DAY_TIME = "start_day_time"
        const val SELECTED_EVENT_FILTER = "selected_event_filter"
        const val SELECTED_GRAPH_PERIOD = "selected_graph_period"
        const val SELECTED_GRAPH_MEASURE = "selected_graph_measure"
        const val SELECTED_GRAPH_FIRST_QUANT = "selected_graph_first_quant"
        const val SELECTED_GRAPH_SECOND_QUANT = "selected_graph_second_quant"
        const val LAST_SELECTED_CALENDAR = "last_selected_calendar"

        const val PERIOD_WHILE_SELECTED_CALENDAR_MATTERS = 60 * 60 * 1000
    }

    private val preferences = context.getSharedPreferences("qol_preferences", Context.MODE_PRIVATE)

    var lastSelectedCalendar: Calendar
    get() {
        if (Calendar.getInstance().timeInMillis - lastSelectedCalendarWhenSelected.timeInMillis
            > PERIOD_WHILE_SELECTED_CALENDAR_MATTERS) {
            return Calendar.getInstance()
        } else {
            return lastSelectedCalendarWhatSelected
        }
    }
    set(value) {
        lastSelectedCalendarWhatSelected = value
        lastSelectedCalendarWhenSelected = Calendar.getInstance()
    }

    private var lastSelectedCalendarWhenSelected by preferences.calendar()
    private var lastSelectedCalendarWhatSelected by preferences.calendar()

    var selectedTimeInterval by preferences.timeInterval (
        key = { SELECTED_GRAPH_PERIOD },
        defaultValue = TimeInterval.Week
    )

    var selectedGraphQuantFirst by preferences.quantFilter (
        key = { SELECTED_GRAPH_FIRST_QUANT },
        defaultValue = QuantFilter.All
    )

    var selectedGraphQuantSecond by preferences.quantFilter (
        key = { SELECTED_GRAPH_SECOND_QUANT },
        defaultValue = QuantFilter.Nothing
    )

    var selectedGraphMeasure by preferences.measure (
        key = { SELECTED_GRAPH_MEASURE },
        defaultValue = Measure.TotalCount
    )

    var selectedYearFilter by preferences.graphSelectedYear ()

    var statisticTimeIntervalSelectedElement by preferences.string(
        key = { SELECTED_RADIO_IN_STATISTIC },
        defaultValue = "All"
    )

    var statisticTimeStart by preferences.long(
        key = { SELECTED_TIME_START }
    )

    var statisticTimeEnd by preferences.long(
        key = { SELECTED_TIME_END }
    )

    var selectedEventFiler by preferences.stringOrNull (
        key = { SELECTED_EVENT_FILTER },
        defaultValue = null
    )

    var categoryNames = mutableMapOf(
        QuantCategory.Physical to getCategoryName(QuantCategory.Physical),
        QuantCategory.Emotion to getCategoryName(QuantCategory.Emotion),
        QuantCategory.Evolution to getCategoryName(QuantCategory.Evolution),
        QuantCategory.All to getCategoryName(QuantCategory.All),
        QuantCategory.Other to getCategoryName(QuantCategory.Other),
        QuantCategory.None to getCategoryName(QuantCategory.None)
    )
    set(value) {
        field = value
        for (v in field) {
            setCategoryName(v.key, v.value)
        }
    }

    private fun getCategoryName(category: QuantCategory): String {
        val default = context.resources.getStringArray(R.array.category_name)[category.ordinal]
        return preferences.getString(CATEGORY_NAME_ + category.name, default) ?: default
    }

    fun getCategoryNames(): ArrayList<Pair<QuantCategory, String>> {
        val result = arrayListOf<Pair<QuantCategory, String>>()

        enumValues<QuantCategory>().forEach {
            val default = context.resources.getStringArray(R.array.category_name)[it.ordinal]
            result.add(
                Pair(
                    it,
                    preferences.getString(CATEGORY_NAME_ + it.name, default) ?: default
                )
            )
        }
        return result
    }

    private fun setCategoryName(category: QuantCategory, name: String) {
        preferences.edit()
            .putString(CATEGORY_NAME_ + category.name, name)
            .apply()
    }

    fun isOnBoardingCompleted(): Boolean {
        return true//preferences.getBoolean(ONBOARDING_COMPLETE , false)
    }

    fun setOnBoardingComplete(complete: Boolean) {
        preferences.edit()
            .putBoolean(ONBOARDING_COMPLETE, complete)
            .apply()
    }

    var startDayTime by preferences.long(
        key = { START_DAY_TIME }
    )

    fun SharedPreferences.string(
        defaultValue: String = "",
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, String> =
        object : ReadWriteProperty<Any, String> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): String = getString(key(property), defaultValue) ?: defaultValue

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: String
            ) = edit().putString(key(property), value).apply()
        }

    fun SharedPreferences.stringOrNull(
        defaultValue: String? = null,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, String?> =
        object : ReadWriteProperty<Any, String?> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): String? = getString(key(property), defaultValue) ?: defaultValue

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: String?
            ) = edit().putString(key(property), value).apply()
        }

    fun SharedPreferences.long(
        defaultValue: Long = 0,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, Long> =
        object : ReadWriteProperty<Any, Long> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): Long = getLong(key(property), defaultValue)

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: Long
            ) = edit().putLong(key(property), value).apply()
        }

    fun SharedPreferences.calendar(
        defaultValue: Calendar = Calendar.getInstance(),
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, Calendar> =
        object : ReadWriteProperty<Any, Calendar> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): Calendar {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = getLong(key(property), defaultValue.timeInMillis)
                return calendar
            }

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: Calendar
            ) = edit().putLong(key(property), value.timeInMillis).apply()
        }

    fun SharedPreferences.measure(
        defaultValue: Measure = Measure.TotalCount,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, Measure> =
        object : ReadWriteProperty<Any, Measure> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): Measure {
                QLog.d("skyfolk-settings","read measure = ${getString(key(property), defaultValue.toString())
                    ?: defaultValue.toString()}, for example = ${Measure.TotalPhysical.name}")
                return when (getString(key(property), defaultValue.toString())
                    ?: defaultValue.toString()) {
                    Measure.TotalCount.name -> Measure.TotalCount
                    Measure.Quantity.name -> Measure.Quantity
                    Measure.TotalPhysical.name -> Measure.TotalPhysical
                    Measure.TotalEmotional.name -> Measure.TotalEmotional
                    Measure.TotalEvolution.name -> Measure.TotalEvolution
                    Measure.AverageRating.name -> Measure.AverageRating
                    else -> Measure.TotalCount
                }
            }

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: Measure
            ) {
                edit().putString(key(property), value.name).apply()
            }
        }

    private fun SharedPreferences.graphSelectedYear(
        defaultValue: GraphSelectedYear = GraphSelectedYear.All,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, GraphSelectedYear> =
        object : ReadWriteProperty<Any, GraphSelectedYear> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): GraphSelectedYear {
                return when (val value = getString(key(property), defaultValue.toString())
                    ?: defaultValue.toString()) {
                    GraphSelectedYear.All.toString() -> GraphSelectedYear.All
                    else -> {
                        if (value.toIntOrNull() != null) {
                            GraphSelectedYear.OnlyYear(value.toInt())
                            } else {
                            GraphSelectedYear.All
                        }

                    }
                }
            }

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: GraphSelectedYear
            ) {
                when (value) {
                    GraphSelectedYear.All -> edit().putString(key(property), GraphSelectedYear.All::class.java.canonicalName).apply()
                    is GraphSelectedYear.OnlyYear -> edit().putString(key(property), value.year.toString()).apply()
                }
            }
        }

    fun SharedPreferences.timeInterval(
        defaultValue: TimeInterval = TimeInterval.All,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, TimeInterval> =
        object : ReadWriteProperty<Any, TimeInterval> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): TimeInterval {
                return when (getString(key(property), defaultValue.toString())
                    ?: defaultValue.toString()) {
                    TimeInterval.Today.toString() -> TimeInterval.Today
                    TimeInterval.Year.toString() -> TimeInterval.Year
                    TimeInterval.Week.toString() -> TimeInterval.Week
                    else -> TimeInterval.Month
                }
            }

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: TimeInterval
            ) = edit().putString(key(property), value.toString()).apply()
        }

    fun SharedPreferences.quantFilter(
        defaultValue: QuantFilter = QuantFilter.All,
        key: (KProperty<*>) -> String = KProperty<*>::name
    ): ReadWriteProperty<Any, QuantFilter> =
        object : ReadWriteProperty<Any, QuantFilter> {
            override fun getValue(
                thisRef: Any,
                property: KProperty<*>
            ): QuantFilter {
                return when (val quant = getString(key(property), defaultValue.toString())
                    ?: defaultValue.toString()) {
                    "Ничего" -> QuantFilter.Nothing
                    "Все события" -> QuantFilter.All
                    else -> QuantFilter.OnlySelected(quant)
                }
            }

            override fun setValue(
                thisRef: Any,
                property: KProperty<*>,
                value: QuantFilter
            ) = edit().putString(key(property), value.toString()).apply()
        }
}