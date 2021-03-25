package com.skyfolk.quantoflife

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenNavigationTest {
    @Test
    fun testAppNavigation() {
        //SETUP
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.navigation_feeds)).perform(ViewActions.click())
        onView(withId(R.id.feeds_fragment_root)).check(matches(isDisplayed()))

        pressBack()
        onView(withId(R.id.now_fragment_root)).check(matches(isDisplayed()))

        onView(withId(R.id.navigation_statistic)).perform(ViewActions.click())
        onView(withId(R.id.statistic_fragment_root)).check(matches(isDisplayed()))

        pressBack()
        onView(withId(R.id.now_fragment_root)).check(matches(isDisplayed()))

        onView(withId(R.id.navigation_settings)).perform(ViewActions.click())
        onView(withId(R.id.settings_fragment_root)).check(matches(isDisplayed()))

        pressBack()
        onView(withId(R.id.now_fragment_root)).check(matches(isDisplayed()))
    }
}
