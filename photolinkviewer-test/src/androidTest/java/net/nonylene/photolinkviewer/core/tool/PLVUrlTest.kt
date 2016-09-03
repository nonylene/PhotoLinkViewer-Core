package net.nonylene.photolinkviewer.core.tool

import android.os.Parcel
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import kotlin.reflect.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PLVUrlTest {

    private fun createMockPLVUrl(): PLVUrl {
        val plvUrl = PLVUrl("url", "sitename", "filename", "quality")
        plvUrl.thumbUrl = "thumburl"
        plvUrl.displayUrl = "displayurl"
        plvUrl.biggestUrl = "biggesturl"
        plvUrl.type = "type"
        plvUrl.height = 100
        plvUrl.width = 200
        plvUrl.isVideo = true
        return plvUrl
    }

    @Test
    fun parcelableTest() {
        val parcel = Parcel.obtain()
        val plvUrl = createMockPLVUrl()
        plvUrl.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val createdPLVUrl = PLVUrl.CREATOR.createFromParcel(parcel)

        for (field in PLVUrl::class.declaredMemberProperties) {
            field.isAccessible = true
            assertEquals(field.get(createdPLVUrl), field.get(plvUrl), field.name + " test")
        }
    }

    @Test
    fun nullWithParcelTest() {
        val parcel = Parcel.obtain()
        val plvUrl = createMockPLVUrl()
        plvUrl.thumbUrl = null
        plvUrl.type = null
        plvUrl.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val createdPLVUrl = PLVUrl.CREATOR.createFromParcel(parcel)
        assertNull(createdPLVUrl.type)
        assertEquals(createdPLVUrl.thumbUrl, plvUrl.displayUrl)
    }
}
