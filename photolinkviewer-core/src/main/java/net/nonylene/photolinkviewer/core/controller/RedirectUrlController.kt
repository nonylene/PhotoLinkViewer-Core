package net.nonylene.photolinkviewer.core.controller

import net.nonylene.photolinkviewer.core.tool.OkHttpManager
import okhttp3.Callback
import okhttp3.Request

internal class RedirectUrlController(private val callback: Callback) {
    fun getRedirect(url : String) {
        OkHttpManager.okHttpClient.newCall(Request.Builder().url(url).get().build()).enqueue(callback)
    }
}
