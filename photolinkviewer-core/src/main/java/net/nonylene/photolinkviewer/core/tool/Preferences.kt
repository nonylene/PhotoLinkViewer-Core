package net.nonylene.photolinkviewer.core.tool

import android.content.SharedPreferences
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.model.getOptionButtonFromId
import org.json.JSONArray

private val WIFI_SWITCH_KEY = "plv_core_is_wifi_enabled"
private val ZOOM_SPEED_KEY = "plv_core_zoom_speed"
private val IS_VIDEO_PLAY_KEY = "plv_core_is_video_play"
private val IS_ADJUST_ZOOM_KEY = "plv_core_is_adjust_zoom"
private val DOWNLOAD_DIR_KEY = "plv_core_download_dir"
private val DOWNLOAD_DIR_TYPE_KEY = "plv_core_download_dir_type"
private val IS_SKIP_DIALOG_KEY = "plv_core_is_skip_dialog"
private val IS_LEAVE_NOTIFY_KEY = "plv_core_is_leave_notify"
private val IS_DOUBLE_ZOOM_KEY = "plv_core_is_double_zoom_disabled"
private val IMAGE_VIEW_MAX_KEY = "plv_core_imageview_max_size"
private val IS_INITIALIZE_47_KEY = "plv_core_is_initialize_47"
private val OPTION_BUTTONS_KEY = "option_buttons"

fun SharedPreferences.getQuality(siteName: String, isWifi: Boolean, defaultValue: String = "large"): String {
    return getString("plv_core_${siteName}_quality_" + if (isWifi) "wifi" else "3g", defaultValue)
}

fun SharedPreferences.Editor.putQuality(value: String, siteName: String, isWifi: Boolean): SharedPreferences.Editor {
    return putString("plv_core_${siteName}_quality_" + if (isWifi) "wifi" else "3g", value)
}

fun SharedPreferences.isOriginalEnabled(isWifi: Boolean, defaultValue: Boolean = false): Boolean{
    return getBoolean("plv_core_is_original_" + if (isWifi) "wifi" else "3g", defaultValue)
}

fun SharedPreferences.Editor.putIsOriginalEnabled(value: Boolean, isWifi: Boolean): SharedPreferences.Editor {
    return putBoolean("plv_core_is_original_" + if (isWifi) "wifi" else "3g", value)
}

fun SharedPreferences.isWifiEnabled(defaultValue: Boolean = false): Boolean {
    return getBoolean(WIFI_SWITCH_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsWifiEnabled(value: Boolean): SharedPreferences.Editor {
    return putBoolean(WIFI_SWITCH_KEY, value)
}

fun SharedPreferences.getZoomSpeed(defaultValue: Float = 1.4f): Float {
    return getString(ZOOM_SPEED_KEY, defaultValue.toString()).toFloat()
}

fun SharedPreferences.Editor.putZoomSpeed(value: Float): SharedPreferences.Editor {
    return putString(ZOOM_SPEED_KEY, value.toString())
}

fun SharedPreferences.isVideoAutoPlay(defaultValue: Boolean = true): Boolean{
    return getBoolean(IS_VIDEO_PLAY_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsVideoAutoPlay(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_VIDEO_PLAY_KEY, value)
}

fun SharedPreferences.isAdjustZoom(defaultValue: Boolean = false): Boolean{
    return getBoolean(IS_ADJUST_ZOOM_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsAdjustZoom(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_ADJUST_ZOOM_KEY, value)
}

fun SharedPreferences.getDownloadDir(defaultValue: String = "PLViewer/"): String{
    return getString(DOWNLOAD_DIR_KEY, defaultValue)
}

fun SharedPreferences.Editor.putDownloadDir(value: String): SharedPreferences.Editor {
    return putString(DOWNLOAD_DIR_KEY, value)
}

fun SharedPreferences.getDownloadDirType(defaultValue: String = "mkdir"): String{
    return getString(DOWNLOAD_DIR_TYPE_KEY, defaultValue)
}

fun SharedPreferences.Editor.putDownloadDirType(value: String): SharedPreferences.Editor {
    return putString(DOWNLOAD_DIR_TYPE_KEY, value)
}

fun SharedPreferences.isSkipDialog(defaultValue: Boolean = false): Boolean{
    return getBoolean(IS_SKIP_DIALOG_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsSkipDialog(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_SKIP_DIALOG_KEY, value)
}

fun SharedPreferences.isLeaveNotify(defaultValue: Boolean = true): Boolean{
    return getBoolean(IS_LEAVE_NOTIFY_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsLeaveNotify(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_LEAVE_NOTIFY_KEY, value)
}

fun SharedPreferences.isDoubleZoomDisabled(defaultValue: Boolean = false): Boolean{
    return getBoolean(IS_DOUBLE_ZOOM_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsDoubleZoomDisabled(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_DOUBLE_ZOOM_KEY, value)
}

fun SharedPreferences.getImageViewMaxSize(defaultValue: Int = 2): Int {
    return getInt(IMAGE_VIEW_MAX_KEY, defaultValue)
}

fun SharedPreferences.Editor.putImageViewMaxSize(value: Int): SharedPreferences.Editor {
    return putInt(IMAGE_VIEW_MAX_KEY, value)
}

fun SharedPreferences.isInitialized47(defaultValue: Boolean = false): Boolean{
    return getBoolean(IS_INITIALIZE_47_KEY, defaultValue)
}

fun SharedPreferences.Editor.putIsInitialized47(value: Boolean): SharedPreferences.Editor {
    return putBoolean(IS_INITIALIZE_47_KEY, value)
}

// bottom button is first, top button is last
fun SharedPreferences.getOptionButtons(): List<OptionButton> {
    val jsonString = getString(OPTION_BUTTONS_KEY, null)
    if (jsonString == null) {
        return listOf(
                OptionButton.TWEET_LIKE,
                OptionButton.TWEET_RETWEET,
                OptionButton.DOWNLOAD,
                OptionButton.PREFERENCE,
                OptionButton.OPEN_OTHER_APP
                )
    } else {
        val jsonArray = JSONArray(jsonString)
        return (0 until jsonArray.length()).map {
            jsonArray.getInt(it)
        }.map {
            getOptionButtonFromId(it)
        }
    }
}

fun SharedPreferences.Editor.putOptionButtons(buttons: List<OptionButton>): SharedPreferences.Editor {
    val saveJson = JSONArray()
    buttons.forEach { saveJson.put(it.id) }
    return putString(OPTION_BUTTONS_KEY, saveJson.toString())
}
