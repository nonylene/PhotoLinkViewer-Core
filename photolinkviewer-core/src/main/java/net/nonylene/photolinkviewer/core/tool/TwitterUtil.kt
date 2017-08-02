package net.nonylene.photolinkviewer.core.tool

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import twitter4j.Status

fun twitterSmallUrl(url: String): String {
    return url + ":small"
}

fun twitterBiggestUrl(url: String): String {
    return url + ":orig"
}

fun twitterDisplayUrl(url: String, quality: String): String {
    return when (quality) {
        "original" -> twitterBiggestUrl(url)
        "large"    -> url + ":large"
        "medium"   -> url
        "small"    -> twitterSmallUrl(url)
        else       -> url
    }
}
private fun getId(url: String): String {
    val lastPath = Uri.parse(url).lastPathSegment
    val index = lastPath.lastIndexOf('.')
    return lastPath.substring(0, if (index > 0) index else lastPath.length)
}

fun createTwitterPLVUrls(status: Status, context: Context): List<PLVUrl> {
    return status.mediaEntities.map { mediaEntity ->
        val url = mediaEntity.mediaURLHttps

        if (mediaEntity.type in arrayOf("animated_gif", "video")) {
            val displayUrl = mediaEntity.videoVariants.filter {
                ("video/mp4") == it.contentType
            }.maxBy { it.bitrate }!!.url
            val plvUrl = PLVUrl(url, "twitter", getId(displayUrl), null, status.user.screenName)
            plvUrl.type = "mp4"
            plvUrl.displayUrl = displayUrl
            plvUrl.thumbUrl = mediaEntity.mediaURLHttps
            plvUrl.isVideo = true
            plvUrl
        } else {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val quality = sharedPreferences.getQuality("twitter", useWifiPreference(context))

            val plvUrl =  PLVUrl(url, "twitter", getId(url), quality, status.user.screenName)
            plvUrl.type = getFileTypeFromUrl(url)
            plvUrl.biggestUrl = twitterBiggestUrl(url)
            plvUrl.thumbUrl = twitterSmallUrl(url)
            plvUrl.displayUrl = twitterDisplayUrl(url, quality)
            plvUrl
        }
    }

}
