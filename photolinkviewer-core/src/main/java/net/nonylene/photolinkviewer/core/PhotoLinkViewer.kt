package net.nonylene.photolinkviewer.core

import android.app.Activity
import com.squareup.okhttp.Cache
import java.util.*

/**
 * This is object (singleton in java) used in PhotoLinkViewer-Core.
 * Before app uses this module, [PhotoLinkViewer.with] must be called.
 */
object PhotoLinkViewer {
    private var twitterKeys: TwitterKeys? = null
    private var flickrKey: String? = null
    private var tumblrKey: String? = null
    // launch activity when preference button pressed
    private var preferenceActivityClass: Class<out Activity>? = null

    val twitterTokenMap = LinkedHashMap<String, TwitterToken>()
    // to use instagram video preview, set token.
    var instagramToken: String? = null
    // to use cache in okhttp, set cache.
    var cache : Cache? = null

    // call this function when application start
    fun with(twitterKeys: TwitterKeys, flickrKey: String, tumblrKey: String, preferenceActivityClass: Class<out Activity>) {
        this.twitterKeys = twitterKeys
        this.flickrKey = flickrKey
        this.tumblrKey = tumblrKey
        this.preferenceActivityClass = preferenceActivityClass
    }

    // cause exception if required keys not exists
    fun getTwitterKeys(): TwitterKeys {
        if (twitterKeys != null) return twitterKeys!!
        else throw IllegalStateException("You must set twitter keys (called PhotoLinkViewer.with()?).")
    }

    fun getFlickrKey(): String {
        if (flickrKey != null) return flickrKey!!
        else throw IllegalStateException("You must set flickr key (called PhotoLinkViewer.with()?).")
    }

    fun getTumblrKey(): String {
        if (tumblrKey != null) return tumblrKey!!
        else throw IllegalStateException("You must set tumblr key (called PhotoLinkViewer.with()?).")
    }

    fun getPreferenceActivityClass(): Class<out Activity> {
        if (preferenceActivityClass != null) return preferenceActivityClass!!
        else throw IllegalStateException("You must set preference Activity class (called PhotoLinkViewer.with()?).")
    }
    class TwitterKeys(val consumerKey: String, val consumerSecret: String)
    class TwitterToken(val accessToken: String, val accessTokenSecret: String)
}
