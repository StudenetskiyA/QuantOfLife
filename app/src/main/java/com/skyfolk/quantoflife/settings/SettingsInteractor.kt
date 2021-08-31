package com.skyfolk.quantoflife.settings

import android.content.SharedPreferences
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.meansure.Measure
import com.skyfolk.quantoflife.meansure.QuantFilter
import com.skyfolk.quantoflife.timeInterval.TimeInterval

open class SettingsInteractor(private val preferences: SharedPreferences) {
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

    fun getSelectedGraphPeriod(): TimeInterval {
        return when (preferences.getString(SELECTED_GRAPH_PERIOD, "Неделя") ?: "Неделя") {
            "День" -> TimeInterval.Today
            "Неделя" -> TimeInterval.Week
            else -> TimeInterval.Month
        }
    }

    fun getSelectedGraphMeasure(): Measure {
        return when (preferences.getString(SELECTED_GRAPH_MEASURE, "Общая оценка") ?: "Общая оценка") {
            "Общая оценка" -> Measure.TotalCount
            "Количество" -> Measure.TotalCount
            else -> Measure.AverageRating
        }
    }

    fun getSelectedGraphQuant(n: Int): QuantFilter {
        val quant = when (n) {
            1 -> preferences.getString(SELECTED_GRAPH_FIRST_QUANT, "Все события") ?: "Все события"
            2 -> preferences.getString(SELECTED_GRAPH_SECOND_QUANT, "Ничего") ?: "Ничего"
            else -> "Ничего"
        }
        return when (quant) {
            "Ничего" -> QuantFilter.Nothing
            "Все события" -> QuantFilter.All
            else -> QuantFilter.OnlySelected(quant)
        }
    }

    fun writeSelectedGraphPeriod(period: TimeInterval) {
        preferences.edit()
            .putString(SELECTED_GRAPH_PERIOD, period.toGraphString())
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
        if (category == QuantCategory.All) return "Всего"
        if (category == QuantCategory.None) return "Не задана"
        return preferences.getString(CATEGORY_NAME_ + category.name, category.name) ?: category.name
    }

    fun getCategoryNames() : ArrayList<Pair<QuantCategory, String>> {
        var result = arrayListOf<Pair<QuantCategory,String>>()
//        result.add(Pair(QuantCategory.All, "Всего"))
//        result.add(Pair(QuantCategory.None, "Не задана"))

        enumValues<QuantCategory>().forEach {
            result.add(Pair(it, preferences.getString(CATEGORY_NAME_ + it.name, it.name) ?: it.name))
        }
        return result
    }

    fun setCategoryName(category: QuantCategory, name: String) {
        preferences.edit()
            .putString(CATEGORY_NAME_ + category.name, name)
            .apply()
    }

    fun isOnBoardingCompleted() : Boolean {
        return preferences.getBoolean(ONBOARDING_COMPLETE , false)
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