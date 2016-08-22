package net.nonylene.photolinkviewer.core.model

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import net.nonylene.photolinkviewer.core.R

private val BLUE_COLOR = Color.parseColor("#24a8ed")
private val RED_COLOR = Color.parseColor("#f4554f")

enum class OptionButton(val id: Int, @DrawableRes val icon: Int, @ColorInt val color: Int) {

    TWEET_LIKE(0, R.drawable.plv_core_ic_favorite_white, RED_COLOR),
    TWEET_RETWEET(1, R.drawable.plv_core_retweet_white, RED_COLOR),
    DOWNLOAD(2, R.drawable.plv_core_ic_file_download_white, BLUE_COLOR),
    PREFERENCE(3, R.drawable.plv_core_ic_settings_white, BLUE_COLOR),
    OPEN_OTHER_APP(4, R.drawable.plv_core_ic_open_in_browser_white, BLUE_COLOR);

}

fun getOptionButtonFromId(id: Int): OptionButton {
    return OptionButton.values().first { it.id == id }
}
