package com.skyfolk.quantoflife.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.SingleLiveEvent

class OnBoardingViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {
    private val _navigationState = SingleLiveEvent<OnBoardingWayEvent>()
    val navigationState: LiveData<OnBoardingWayEvent> get() = _navigationState

    fun goToSecondScreen() {
        _navigationState.value = OnBoardingWayEvent.GoToSecondScreen
    }

    fun submitCategoryName(
        firstCategoryName: String,
        secondCategoryName: String,
        thirdCategoryName: String,
        fourthCategoryName: String
    ) {
        settingsInteractor.setCategoryName(QuantCategory.Physical, firstCategoryName)
        settingsInteractor.setCategoryName(QuantCategory.Emotion, secondCategoryName)
        settingsInteractor.setCategoryName(QuantCategory.Evolution, thirdCategoryName)
        settingsInteractor.setCategoryName(QuantCategory.Other, fourthCategoryName)

        settingsInteractor.setOnBoardingComplete(true)
        _navigationState.value = OnBoardingWayEvent.OnBoardingCompleted
    }
}

sealed class OnBoardingWayEvent {
    object GoToSecondScreen : OnBoardingWayEvent()
    object GoToPhysicalSelection : OnBoardingWayEvent()
    object OnBoardingCompleted : OnBoardingWayEvent()
}