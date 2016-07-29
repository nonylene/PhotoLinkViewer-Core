package net.nonylene.photolinkviewer.core.tool

import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import net.nonylene.photolinkviewer.core.PhotoLinkViewer

object OkHttpManager {
    val okHttpClient by lazy {
        OkHttpClient().apply {
            PhotoLinkViewer.cache?.let { cache ->
                //enable cache
                setCache(cache)
                networkInterceptors().add(Interceptor { chain ->
                    val originalResponse = chain.proceed(chain.request());
                    originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=180")
                            .build()
                })
            }
        }
    }
}
