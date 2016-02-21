package net.nonylene.photolinkviewer.core.tool

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import net.nonylene.photolinkviewer.core.PLVPreferenceActivity
import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import net.nonylene.photolinkviewer.core.test.BuildConfig
import net.nonylene.photolinkviewer.core.test.TestUrls
import org.junit.*

import org.junit.runner.RunWith
import java.util.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PLVUrlServiceTest {

    private val mContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun getServiceWithSuccessListener(operation: (Array<PLVUrl>) -> Unit): PLVUrlService {
        return PLVUrlService(mContext, object : PLVUrlService.PLVUrlListener {

            override fun onGetPLVUrlFinished(plvUrls: Array<PLVUrl>) {
                operation(plvUrls)
            }

            override fun onGetPLVUrlFailed(text: String) {
                throw IllegalStateException("plv url failed!: $text")
            }

            override fun onURLAccepted() {
            }
        })
    }

    init {
        PhotoLinkViewer.with(PhotoLinkViewer.TwitterKeys("", ""), BuildConfig.FLICKR_KEY, BuildConfig.TUMBLR_KEY, PLVPreferenceActivity::class.java)
    }

    companion object {
        private val defaultPrefStringMap = HashMap<String, String>()
        private val defaultPrefBooleanMap = HashMap<String, Boolean>()

        @JvmStatic
        @BeforeClass
        fun start() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(
                    InstrumentationRegistry.getInstrumentation().targetContext)
            initPreference("flickr", preferences)
            initPreference("nicoseiga", preferences)
            initPreference("tumblr", preferences)
            initPreference("twitter", preferences)
            initPreference("twipple", preferences)
            initPreference("imgly", preferences)
            initPreference("instagram", preferences)
        }

        @JvmStatic
        @AfterClass
        fun finalize() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(
                    InstrumentationRegistry.getInstrumentation().targetContext)
            restorePreference("flickr", preferences)
            restorePreference("nicoseiga", preferences)
            restorePreference("tumblr", preferences)
            restorePreference("twitter", preferences)
            restorePreference("twipple", preferences)
            restorePreference("imgly", preferences)
            restorePreference("instagram", preferences)
        }

        private fun initPreference(siteName: String, sharedPreferences: SharedPreferences) {
            defaultPrefStringMap.putQuality(sharedPreferences.getQuality(siteName, false), siteName, false)
            defaultPrefStringMap.putQuality(sharedPreferences.getQuality(siteName, true), siteName, true)

            sharedPreferences.edit()
                    .putQuality("large", siteName, false)
                    .putQuality("large", siteName, true)
                    .apply()
        }

        private fun restorePreference(siteName: String, sharedPreferences: SharedPreferences) {
            sharedPreferences.edit()
                    .putQuality(defaultPrefStringMap.getQuality(siteName, false), siteName, false)
                    .putQuality(defaultPrefStringMap.getQuality(siteName, true), siteName, true)
                    .apply()
        }
    }

    @Test
    fun requestFlickrUrlTest() {
        val countDownLatch = CountDownLatch(3)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://farm6.staticflickr.com/5714/22247064413_6f0f765301_o.jpg")
                assertEquals(thumbUrl, "https://farm6.staticflickr.com/5714/22247064413_3740db4e3c_z.jpg")
                assertEquals(fileName, "22247064413")
                assertEquals(siteName, "flickr")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.FLICKR_SHORTEN_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://farm6.staticflickr.com/5714/22247064413_6f0f765301_o.jpg")
                assertEquals(thumbUrl, "https://farm6.staticflickr.com/5714/22247064413_3740db4e3c_z.jpg")
                assertEquals(fileName, "22247064413")
                assertEquals(siteName, "flickr")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.FLICKR_NORMAL_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://farm6.staticflickr.com/5714/22247064413_6f0f765301_o.jpg")
                assertEquals(thumbUrl, "https://farm6.staticflickr.com/5714/22247064413_3740db4e3c_z.jpg")
                assertEquals(fileName, "22247064413")
                assertEquals(siteName, "flickr")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.FLICKR_MOBILE_URL)

        countDownLatch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun requestNicoUrlTest() {
        val countDownLatch = CountDownLatch(2)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://lohas.nicoseiga.jp/img/5323294l")
                assertEquals(thumbUrl, "http://lohas.nicoseiga.jp/img/5323294m")
                assertEquals(fileName, "5323294")
                assertEquals(siteName, "nico")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.NICO_NORMAL_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://lohas.nicoseiga.jp/img/5323294l")
                assertEquals(thumbUrl, "http://lohas.nicoseiga.jp/img/5323294m")
                assertEquals(fileName, "5323294")
                assertEquals(siteName, "nico")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.NICO_SHORTEN_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestTwitterUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://pbs.twimg.com/media/CTSJJGvVEAAjr7J.jpg:orig")
                assertEquals(thumbUrl, "http://pbs.twimg.com/media/CTSJJGvVEAAjr7J.jpg:small")
                assertEquals(fileName, "CTSJJGvVEAAjr7J")
                assertEquals(siteName, "twitter")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.PBS_TWITTER_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestTwippleUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://p.twipple.jp/show/orig/XLyY4")
                assertEquals(thumbUrl, "http://p.twipple.jp/show/large/XLyY4")
                assertEquals(fileName, "XLyY4")
                assertEquals(siteName, "twipple")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.TWIPPLE_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestImglyUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://img.ly/show/full/CSbm")
                assertEquals(thumbUrl, "http://img.ly/show/medium/CSbm")
                assertEquals(fileName, "CSbm")
                assertEquals(siteName, "imgly")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.IMGLY_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestInstagramNoApiUrlTest() {
        val countDownLatch = CountDownLatch(2)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://instagram.com/p/90kD_WzfqP/media/?size=l")
                assertEquals(thumbUrl, "https://instagram.com/p/90kD_WzfqP/media/?size=m")
                assertEquals(fileName, "90kD_WzfqP")
                assertEquals(siteName, "instagram")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.INSTAGRAM_NORMAL_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://instagram.com/p/90kD_WzfqP/media/?size=l")
                assertEquals(thumbUrl, "https://instagram.com/p/90kD_WzfqP/media/?size=m")
                assertEquals(fileName, "90kD_WzfqP")
                assertEquals(siteName, "instagram")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.INSTAGRAM_SHORTEN_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestGyazoUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://gyazo.com/953acc58e3a4fbac2723c190f83c1a90/raw")
                assertEquals(thumbUrl, "https://gyazo.com/953acc58e3a4fbac2723c190f83c1a90/raw")
                assertEquals(fileName, "953acc58e3a4fbac2723c190f83c1a90")
                assertEquals(siteName, "gyazo")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.GYAZO_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestImgurUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://i.imgur.com/rfuJf9e.jpg")
                assertEquals(thumbUrl, "http://i.imgur.com/rfuJf9e.jpg")
                assertEquals(fileName, "rfuJf9e")
                assertEquals(siteName, "imgur")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.IMGUR_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestVineUrlTest() {
        val countDownLatch = CountDownLatch(1)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "http://mtc.cdn.vine.co/r/videos_h264high/161490F5CE1275115288903467008_SW_WEBM_1446985382905bb3eec67c1.mp4?versionId=lC9IMKIxRhcgNaR2YfaljLKsh1x7UEvj")
                assertEquals(thumbUrl, "http://v.cdn.vine.co/r/videos/161490F5CE1275115288903467008_SW_WEBM_1446985382905bb3eec67c1.webm.jpg?versionId=RbCfXIo1nbI6LCToiXc6TqjL6n4ti.7R")
                assertEquals(fileName, "elzuDTFiYDT")
                assertEquals(siteName, "vine")
                assertTrue(isVideo)
                assertEquals(type, "mp4")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.VINE_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun requestTumblrUrlTest() {
        val countDownLatch = CountDownLatch(2)

        getServiceWithSuccessListener({
            assertEquals(it.size, 5)
            it[0].apply {
                assertEquals(biggestUrl, "https://40.media.tumblr.com/ae6cc3e23d6ea2a6d59e0f261866e556/tumblr_nxhxd2u2TV1tevt9yo1_1280.jpg")
                assertEquals(thumbUrl, "https://40.media.tumblr.com/ae6cc3e23d6ea2a6d59e0f261866e556/tumblr_nxhxd2u2TV1tevt9yo1_250.jpg")
                assertEquals(fileName, "132793441942-0")
                assertEquals(siteName, "tumblr")
                assertEquals(type, "jpg")
            }
            it[4].apply {
                assertEquals(biggestUrl, "https://41.media.tumblr.com/96be3485fbf827c5472930a1a9c3fd69/tumblr_nxhxd2u2TV1tevt9yo5_1280.jpg")
                assertEquals(thumbUrl, "https://40.media.tumblr.com/96be3485fbf827c5472930a1a9c3fd69/tumblr_nxhxd2u2TV1tevt9yo5_250.jpg")
                assertEquals(fileName, "132793441942-4")
                assertEquals(siteName, "tumblr")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.TUMBLR_NORMAL_URL)

        getServiceWithSuccessListener({
            assertEquals(it.size, 5)
            it[0].apply {
                assertEquals(biggestUrl, "https://40.media.tumblr.com/ae6cc3e23d6ea2a6d59e0f261866e556/tumblr_nxhxd2u2TV1tevt9yo1_1280.jpg")
                assertEquals(thumbUrl, "https://40.media.tumblr.com/ae6cc3e23d6ea2a6d59e0f261866e556/tumblr_nxhxd2u2TV1tevt9yo1_250.jpg")
                assertEquals(fileName, "132793441942-0")
                assertEquals(siteName, "tumblr")
                assertEquals(type, "jpg")
            }
            it[4].apply {
                assertEquals(biggestUrl, "https://41.media.tumblr.com/96be3485fbf827c5472930a1a9c3fd69/tumblr_nxhxd2u2TV1tevt9yo5_1280.jpg")
                assertEquals(thumbUrl, "https://40.media.tumblr.com/96be3485fbf827c5472930a1a9c3fd69/tumblr_nxhxd2u2TV1tevt9yo5_250.jpg")
                assertEquals(fileName, "132793441942-4")
                assertEquals(siteName, "tumblr")
                assertEquals(type, "jpg")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.TUMBLR_SHORTEN_URL)

        countDownLatch.await(8, TimeUnit.SECONDS)
    }

    @Test
    fun requestOtherUrlTest() {
        val countDownLatch = CountDownLatch(3)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png")
                assertEquals(thumbUrl, "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png")
                assertEquals(fileName, "googlelogo_color_272x92dp")
                assertEquals(siteName, "other")
                assertEquals(type, "png")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.OTHER_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_.272x92dp.png?jfldasjfkdaslfjasfa...#jfkldajflka/j")
                assertEquals(thumbUrl, "https://www.google.co.jp/images/branding/googlelogo/2x/googlelogo_color_.272x92dp.png?jfldasjfkdaslfjasfa...#jfkldajflka/j")
                assertEquals(fileName, "googlelogo_color_.272x92dp")
                assertEquals(siteName, "other")
                assertEquals(type, "png")
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.OTHER_WITH_NOISE_URL)

        getServiceWithSuccessListener({
            it[0].apply {
                assertEquals(biggestUrl, "https://www.google.co.jp/favicon.ico")
                assertEquals(thumbUrl, "https://www.google.co.jp/favicon.ico")
                assertEquals(fileName, "favicon.ico")
                assertEquals(siteName, "other")
                assertNull(type)
            }
            countDownLatch.countDown()
        }).requestGetPLVUrl(TestUrls.OTHER_INVALID_TYPE_URL)

        countDownLatch.await(5, TimeUnit.SECONDS)
    }
}

private fun HashMap<String, String>.putQuality(quality: String, siteName: String, isWifi: Boolean) {
    put(siteName + if (isWifi) "wifi" else "3g", quality)
}

private fun HashMap<String, String>.getQuality(siteName: String, isWifi: Boolean, defaultValue: String = "large"): String {
    return get(siteName + if (isWifi) "wifi" else "3g") ?: defaultValue
}
