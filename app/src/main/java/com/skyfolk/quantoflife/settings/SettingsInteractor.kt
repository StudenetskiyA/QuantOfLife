package com.skyfolk.quantoflife.settings

import android.content.Context
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.meansure.Measure
import com.skyfolk.quantoflife.meansure.QuantFilter
import com.skyfolk.quantoflife.timeInterval.TimeInterval

open class SettingsInteractor(private val context: Context) {
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
    }

    private val preferences = context.getSharedPreferences("qol_preferences", Context.MODE_PRIVATE)

    fun getSelectedTimeInterval(): TimeInterval {
        return when (preferences.getString(SELECTED_GRAPH_PERIOD, TimeInterval.Week.toString()) ?:  TimeInterval.Week.toString()) {
            TimeInterval.Today.toString() -> TimeInterval.Today
            TimeInterval.Week.toString() -> TimeInterval.Week
            else -> TimeInterval.Month
        }
    }

    fun getSelectedGraphMeasure(): Measure {
        return when (preferences.getString(SELECTED_GRAPH_MEASURE, Measure.TotalCount.toString()) ?: Measure.TotalCount.toString()) {
            Measure.TotalCount.toString() -> Measure.TotalCount
            Measure.Quantity.toString() -> Measure.Quantity
            Measure.TotalPhysical.toString() -> Measure.TotalPhysical
            Measure.TotalEmotional.toString() -> Measure.TotalEmotional
            Measure.TotalEvolution.toString() -> Measure.TotalEvolution
            Measure.AverageRating.toString() -> Measure.AverageRating
            else -> Measure.TotalCount
        }
    }

    fun getSelectedGraphQuant(n: Int): QuantFilter {
        val quant = when (n) {
            1 -> preferences.getString(SELECTED_GRAPH_FIRST_QUANT, "Все события") ?: "Все события"
            2 -> preferences.getString(SELECTED_GRAPH_SECOND_QUANT, "Ничего") ?: "Ничего"
            else -> "Ничего"
        }

        val filter = when (quant) {
            "Ничего" -> QuantFilter.Nothing
            "Все события" -> QuantFilter.All
            else -> QuantFilter.OnlySelected(quant)
        }
        return filter
    }

    fun writeSelectedTimeInterval(period: TimeInterval) {
        preferences.edit()
            .putString(SELECTED_GRAPH_PERIOD, period.toString())
            .apply()
    }

    fun writeSelectedGraphMeasure(measure: Measure) {
        preferences.edit()
            .putString(SELECTED_GRAPH_MEASURE, measure.toString())
            .apply()
    }

    fun writeSelectedGraphQuant(n: Int, filter: QuantFilter) {
        preferences.edit()
            .putString(if (n == 1) SELECTED_GRAPH_FIRST_QUANT else SELECTED_GRAPH_SECOND_QUANT, filter.toString())
            .apply()
    }

    fun writeStatisticTimeIntervalSelectedElement(element: String) {
        preferences.edit()
            .putString(SELECTED_RADIO_IN_STATISTIC, element)
            .apply()
    }

    fun getStatisticTimeIntervalSelectedElement() : String {
        return preferences.getString(SELECTED_RADIO_IN_STATISTIC, "All") ?: "All"
    }

    fun getStatisticTimeStart() : Long {
        return preferences.getLong(SELECTED_TIME_START, 0)
    }

    fun getStatisticTimeEnd() : Long {
        return preferences.getLong(SELECTED_TIME_END, 0)
    }

    fun setStatisticTimeEnd(time: Long) {
        preferences.edit()
            .putLong(SELECTED_TIME_END, time)
            .apply()
    }

    fun getSelectedEventFiler() : String? {
        return preferences.getString(SELECTED_EVENT_FILTER, null)
    }

    fun setSelectedEventFiler(filter: String?) {
        preferences.edit()
            .putString(SELECTED_EVENT_FILTER, filter)
            .apply()
    }

    fun setStatisticTimeStart(time: Long) {
        preferences.edit()
            .putLong(SELECTED_TIME_START, time)
            .apply()
    }

    fun getCategoryName(category: QuantCategory) : String {
        val default = context.resources.getStringArray(R.array.category_name)[category.ordinal]
        return preferences.getString(CATEGORY_NAME_ + category.name, default) ?: default
    }

    fun getCategoryNames() : ArrayList<Pair<QuantCategory, String>> {
        val result = arrayListOf<Pair<QuantCategory,String>>()

        enumValues<QuantCategory>().forEach {
            val default = context.resources.getStringArray(R.array.category_name)[it.ordinal]
            result.add(Pair(it, preferences.getString(CATEGORY_NAME_ + it.name, default) ?: default))
        }
        return result
    }

    fun setCategoryName(category: QuantCategory, name: String) {
        preferences.edit()
            .putString(CATEGORY_NAME_ + category.name, name)
            .apply()
    }

    fun isOnBoardingCompleted() : Boolean {
        return true//preferences.getBoolean(ONBOARDING_COMPLETE , false)
    }

    fun setOnBoardingComplete(complete: Boolean) {
        preferences.edit()
            .putBoolean(ONBOARDING_COMPLETE, complete)
            .apply()
    }

    fun getStartDayTime() : Long {
        return preferences.getLong(START_DAY_TIME , 0)
    }

    fun setStartDayTime(timeInMillis: Long) {
        preferences.edit()
            .putLong(START_DAY_TIME, timeInMillis)
            .apply()
    }
 }