package net.nonylene.photolinkviewer.core.model

import android.support.test.runner.AndroidJUnit4
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OptionButtonTest {

    @Test
    fun testFromId() {
        OptionButton.values().forEach {
            assertEquals(it, getOptionButtonFromId(it.id))
        }
    }

    @Test
    fun testUniqueId() {
        val values = OptionButton.values()
        assertEquals(values.size, values.distinctBy { it.id }.size)
    }
}