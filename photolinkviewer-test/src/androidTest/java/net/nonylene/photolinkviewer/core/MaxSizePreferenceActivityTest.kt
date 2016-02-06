package net.nonylene.photolinkviewer.core

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaxSizePreferenceActivityTest {

    @Rule @JvmField
    var activityTestRule = ActivityTestRule(PLVMaxSizePreferenceActivity::class.java);

    @Test
    fun finishTest() {
        activityTestRule.activity.finish()
    }
}
