package org.wikipedia.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.wikipedia.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class SmokeTests {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        onView(allOf(withId(R.id.fragment_onboarding_forward_button), isDisplayed()))
                .perform(click())
    }
}
