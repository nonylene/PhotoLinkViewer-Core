package net.nonylene.photolinkviewer.core

import java.util.*

object PhotoLinkViewer {
    private var twitterKeys: TwitterKeys? = null
    private var flickrKey: String? = null
    private var tumblrKey: String? = null
    val twitterTokenMap = LinkedHashMap<String, TwitterToken>()
    private var instagramToken: String? = null
    set(value) { field = value }

    // call this function when application start
    fun with(twitterKeys: TwitterKeys, flickrKey: String, tumblrKey: String) {
        this.twitterKeys = twitterKeys
        this.flickrKey = flickrKey
        this.tumblrKey = tumblrKey
    }

    fun getTwitterKeys(): TwitterKeys {
        if (twitterKeys != null) return twitterKeys!!
        else throw IllegalStateException("You must set twitter keys (call PhotoLinkViewer.with()?).")
    }

    fun getFlickrKey(): String {
        if (flickrKey != null) return flickrKey!!
        else throw IllegalStateException("You must set flickr key (call PhotoLinkViewer.with()?).")
    }

    fun getTumblrKey(): String {
        if (tumblrKey != null) return tumblrKey!!
        else throw IllegalStateException("You must set tumblr key (call PhotoLinkViewer.with()?).")
    }

    fun getInstagramToken(): String {
        if (instagramToken != null) return instagramToken!!
        else throw IllegalStateException("You must set instagram token (set PhotoLinkViewer.instagramKey?).")
    }

    class TwitterKeys(val consumerKey: String, val consumerSecret: String)
    class TwitterToken(val accessToken: String, val accessTokenSecret: String)
}
