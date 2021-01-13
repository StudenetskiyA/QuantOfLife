package com.skyfolk.quantoflife.settings

import android.content.SharedPreferences
import com.skyfolk.quantoflife.entity.QuantCategory

class SettingsInteractor(private val preferences: SharedPreferences) {
    companion object {
        const val SELECTED_RADIO_IN_STATISTIC = "selected_radio_in_statistic_2"
        const val CATEGORY_NAME_ = "category_name_"
        const val ONBOARDING_COMPLETE = "onboarding_complete"
        const val START_DAY_TIME = "start_day_time"
    }

    fun writeStatisticTimeIntervalSelectedElement(element: String) {
        preferences.edit()
            .putString(SELECTED_RADIO_IN_STATISTIC, element)
            .apply()
    }

    fun getStatisticTimeIntervalSelectedElement() : String {
        return preferences.getString(SELECTED_RADIO_IN_STATISTIC, "All") ?: "All"
    }

    fun getCategoryName(category: QuantCategory) : String {
        return preferences.getString(CATEGORY_NAME_ + category.name, category.name) ?: category.name
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