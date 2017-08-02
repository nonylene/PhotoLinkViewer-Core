package net.nonylene.photolinkviewer.core.tool

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.preference.PreferenceManager

fun useWifiPreference(context: Context): Boolean {
    //check wifi connecting and wifi setting enabled or not
    // note: if no default network is available, activeNetWorInfo returns null
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    return sharedPreferences.isWifiEnabled() &&
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.type ==
                    ConnectivityManager.TYPE_WIFI
}

fun getFileTypeFromUrl(url: String): String? {
    return Uri.parse(url).lastPathSegment?.let {
        it.substring(it.lastIndexOf(".") + 1)
    }
}
