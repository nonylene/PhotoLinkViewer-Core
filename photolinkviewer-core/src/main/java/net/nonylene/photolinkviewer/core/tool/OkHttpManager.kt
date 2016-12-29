package net.nonylene.photolinkviewer.core.tool

import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import okhttp3.OkHttpClient

object OkHttpManager {
    @Suppress("HasPlatformType")
    val okHttpClient by lazy {
        OkHttpClient.Builder()
                .cache(PhotoLinkViewer.cache)
                .addNetworkInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request());
                    originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=180")
                            .build()
                }
                .build()
    }
}
