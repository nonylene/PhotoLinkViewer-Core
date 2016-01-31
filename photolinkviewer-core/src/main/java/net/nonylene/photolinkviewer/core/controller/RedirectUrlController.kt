package net.nonylene.photolinkviewer.core.controller

import com.squareup.okhttp.Request
import net.nonylene.photolinkviewer.core.tool.OkHttpManager

internal class RedirectUrlController(private val callback: com.squareup.okhttp.Callback) {
    fun getRedirect(url : String) {
        OkHttpManager.okHttpClient.newCall(Request.Builder().url(url).get().build()).enqueue(callback)
    }
}
