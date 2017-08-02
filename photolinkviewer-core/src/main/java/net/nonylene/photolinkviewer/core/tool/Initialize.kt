package net.nonylene.photolinkviewer.core.tool

import android.content.Context
import android.content.SharedPreferences
import android.net.http.HttpResponseCache
import android.preference.PreferenceManager

object Initialize {

    private val sites = arrayOf("flickr", "twitter", "twipple", "imgly", "instagram", "nicoseiga", "tumblr")

    fun initialize39(context: Context) {
        // ver 39 (clear old cache)
        HttpResponseCache.getInstalled()?.delete()
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean("initialized39", true)
            .apply()
    }

    fun initialize47(context: Context) {
        // ver 47 (move preference keys)
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val zoomSpeed = pref.getString("zoom_speed", "1.4")
        val isVideoPlay = pref.getBoolean("video_play", true)
        val isAdjustZoom = pref.getBoolean("adjust_zoom", false)
        val downloadDir = pref.getString("download_dir", "PLViewer/")
        val downloadDirType = pref.getString("download_file", "mkdir-username")
        val isSkipDialog = pref.getBoolean("skip_dialog", false)
        val isLeaveNotify = pref.getBoolean("leave_notify", true)
        val isDoubleZoomDisabled= pref.getBoolean("double_zoom", false)
        val imageViewMaxSize = pref.getInt("imageview_max_size", 2)
        val isOriginalEnabledWifi = pref.getBoolean("original_switch_wifi", false)
        val isOriginalEnabled3g = pref.getBoolean("original_switch_3g", false)
        val isWifiEnabled = pref.getBoolean("wifi_switch", false)
        val sitesWifi = sites.associateBy({it}, { pref.getQualityOld(it, true) })
        val sites3g = sites.associateBy({it}, { pref.getQualityOld(it, false) })

        pref.edit().putZoomSpeed(zoomSpeed.toFloat())
                .putIsVideoAutoPlay(isVideoPlay)
                .putIsAdjustZoom(isAdjustZoom)
                .putDownloadDir(downloadDir)
                .putDownloadDirType(downloadDirType)
                .putIsSkipDialog(isSkipDialog)
                .putIsLeaveNotify(isLeaveNotify)
                .putIsDoubleZoomDisabled(isDoubleZoomDisabled)
                .putImageViewMaxSize(imageViewMaxSize)
                .putIsOriginalEnabled(isOriginalEnabledWifi, true)
                .putIsOriginalEnabled(isOriginalEnabled3g, false)
                .putIsWifiEnabled(isWifiEnabled)
        .apply()

        val prefEdit = pref.edit()
        sitesWifi.forEach { prefEdit.putQuality(it.value, it.key, true) }
        sites3g.forEach { prefEdit.putQuality(it.value, it.key, false) }
        prefEdit.putIsInitialized47(true)
        prefEdit.apply()
    }

    private fun SharedPreferences.getQualityOld(siteName: String, isWifi: Boolean, defaultValue: String = "large"): String {
        return getString("${siteName}_quality_" + if (isWifi) "wifi" else "3g", defaultValue)
    }
}
