package net.nonylene.photolinkviewer.core.model

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import net.nonylene.photolinkviewer.core.R

private val BLUE_COLOR = Color.parseColor("#24a8ed")
private val RED_COLOR = Color.parseColor("#f4554f")
private val GREEN_COLOR = Color.parseColor("#46d249")

enum class OptionButton(val id: Int, @DrawableRes val icon: Int, @ColorInt val color: Int, val text: String?) {
    ADD_BUTTON(-1, R.drawable.plv_core_ic_add_24dp, GREEN_COLOR, null),
    TWEET_LIKE(0, R.drawable.plv_core_ic_favorite_white, RED_COLOR, "Like tweet"),
    TWEET_RETWEET(1, R.drawable.plv_core_retweet_white, RED_COLOR, "Retweet"),
    DOWNLOAD(2, R.drawable.plv_core_ic_file_download_white, BLUE_COLOR, "Download"),
    PREFERENCE(3, R.drawable.plv_core_ic_settings_white, BLUE_COLOR, "Preference"),
    OPEN_OTHER_APP(4, R.drawable.plv_core_ic_open_in_browser_white, BLUE_COLOR, "Open browser"),
    COPY_URL(5, R.drawable.plv_core_content_copy_black_24dp, BLUE_COLOR, "Copy URL"),
    SHARE(6, R.drawable.plv_core_share_black_24dp, BLUE_COLOR, "Share URL"),
}

fun getOptionButtonFromId(id: Int): OptionButton {
    return OptionButton.values().first { it.id == id }
}
