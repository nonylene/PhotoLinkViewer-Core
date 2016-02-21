package net.nonylene.photolinkviewer.core.tool

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast

import com.android.volley.Response
import com.squareup.okhttp.Callback
import com.squareup.okhttp.Request
import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.controller.RedirectUrlController


import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import java.util.regex.Pattern

class PLVUrlService(private val context: Context, private val plvUrlListener: PLVUrlService.PLVUrlListener) {

    interface PLVUrlListener {
        fun onGetPLVUrlFinished(plvUrls: Array<PLVUrl>)
        fun onGetPLVUrlFailed(text: String)
        // called on same called thread
        fun onURLAccepted()
    }

    fun requestGetPLVUrl(url: String) {
        val site = when {
            url.contains("flickr.com") || url.contains("flic.kr")
                                             -> FlickrSite(url, context, plvUrlListener)
            url.contains("nico.ms") || url.contains("seiga.nicovideo.jp")
                                             -> NicoSite(url, context, plvUrlListener)
            url.contains("twimg.com/media/") -> TwitterSite(url, context, plvUrlListener)
            url.contains("twipple.jp")       -> TwippleSite(url, context, plvUrlListener)
            url.contains("img.ly")           -> ImglySite(url, context, plvUrlListener)
            url.contains("instagram.com") || url.contains("instagr.am")
                                             -> InstagramSite(url, context, plvUrlListener)
            url.contains("gyazo.com")        -> GyazoSite(url, context, plvUrlListener)
            url.contains("imgur.com")        -> ImgurSite(url, context, plvUrlListener)
            url.contains("vine.co")          -> VineSite(url, context, plvUrlListener)
            url.contains("tmblr.co") || url.contains("tumblr.com")
                                             -> TumblrSite(url, context, plvUrlListener)
            else                             -> OtherSite(url, context, plvUrlListener)
        }
        site.getPLVUrl()
    }

    private abstract inner class Site(protected var url: String, protected var context: Context, protected val listener: PLVUrlListener) {

        protected fun wifiChecker(sharedPreferences: SharedPreferences): Boolean {
            //check wifi connecting and wifi setting enabled or not
            // note: if no default network is available, activeNetWorInfo returns null
            return sharedPreferences.isWifiEnabled() &&
                    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.type ==
                            ConnectivityManager.TYPE_WIFI
        }

        protected fun getQuality(siteName: String): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getQuality(siteName, wifiChecker(sharedPreferences))
        }

        protected fun getId(url: String, regex: String): String? {
            val matcher = Pattern.compile(regex).matcher(url)
            if (!matcher.find()) {
                onParseFailed()
                return null
            }
            listener.onURLAccepted()
            return matcher.group(1)
        }

        protected fun onParseFailed() {
            listener.onGetPLVUrlFailed(context.getString(R.string.plv_core_url_purse_toast))
        }

        protected fun getFileTypeFromUrl(url: String): String? {
            return Uri.parse(url).lastPathSegment?.let {
                it.substring(it.lastIndexOf(".") + 1)
            }
        }

        abstract fun getPLVUrl()
    }

    private inner class TwitterSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://pbs\\.twimg\\.com/media/([^\\.]+)\\.")?.let { id ->
                val plvUrl = PLVUrl(url, "twitter", id)

                plvUrl.type = getFileTypeFromUrl(url)
                plvUrl.biggestUrl = url + ":orig"
                plvUrl.thumbUrl = url + ":small"
                plvUrl.displayUrl = when (super.getQuality("twitter")) {
                    "original" -> plvUrl.biggestUrl
                    "large"    -> url + ":large"
                    "medium"   -> url
                    "small"    -> plvUrl.thumbUrl
                    else       -> null
                }

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            }
        }
    }

    private inner class TwippleSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://p\\.twipple\\.jp/(\\w+)")?.let { id ->
                val plvUrl = PLVUrl(url, "twipple", id)

                plvUrl.biggestUrl = "http://p.twipple.jp/show/orig/" + id
                plvUrl.thumbUrl = "http://p.twipple.jp/show/large/" + id
                plvUrl.displayUrl = when (super.getQuality("twipple")) {
                    "original" -> plvUrl.biggestUrl
                    "large"    -> plvUrl.thumbUrl
                    "thumb"    -> "http://p.twipple.jp/show/thumb/" + id
                    else       -> null
                }

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            }
        }
    }

    private inner class ImglySite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://img\\.ly/(\\w+)")?.let { id ->
                val plvUrl = PLVUrl(url, "imgly", id)

                plvUrl.biggestUrl = "http://img.ly/show/full/" + id
                plvUrl.thumbUrl = "http://img.ly/show/medium/" + id
                plvUrl.displayUrl = when (super.getQuality("imgly")) {
                    "full"   -> plvUrl.biggestUrl
                    "large"  -> plvUrl.thumbUrl
                    "medium" -> "http://img.ly/show/medium/" + id
                    else     -> null
                }

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            }
        }
    }

    private inner class InstagramSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://.*instagr\\.?am[\\.com]*/p/([^/\\?=]+)")?.let { id ->
                val plvUrl = PLVUrl(url, "instagram", id)

                if (PhotoLinkViewer.instagramToken != null) {
                    val apiUrl = "https://api.instagram.com/v1/media/shortcode/${id}?access_token=${PhotoLinkViewer.instagramToken}"

                    VolleyManager.getRequestQueue(context).add(MyJsonObjectRequest(context, apiUrl,
                            Response.Listener { response ->
                                try {
                                    listener.onGetPLVUrlFinished(arrayOf(parseInstagram(response, plvUrl)))
                                } catch (e: JSONException) {
                                    listener.onGetPLVUrlFailed("instagram JSON Parse Error!")
                                    e.printStackTrace()
                                }
                            })
                    )

                } else {
                    plvUrl.biggestUrl = "https://instagram.com/p/${id}/media/?size=l"
                    plvUrl.displayUrl = when (super.getQuality("instagram")) {
                        "large"  -> plvUrl.biggestUrl
                        "medium" -> "https://instagram.com/p/${id}/media/?size=m"
                        else     -> null
                    }
                    plvUrl.thumbUrl = "https://instagram.com/p/${id}/media/?size=m"

                    listener.onGetPLVUrlFinished(arrayOf(plvUrl))
                }
            }
        }

        @Throws(JSONException::class)
        private fun parseInstagram(json: JSONObject, plvUrl: PLVUrl): PLVUrl {
            //for flickr
            val data = JSONObject(json.getString("data"))
            val fileUrls: JSONObject

            if ("video" == data.getString("type")) {
                plvUrl.isVideo = true
                fileUrls = data.getJSONObject("videos")
            } else {
                fileUrls = data.getJSONObject("images")
            }

            val displayUrl = when (super.getQuality("instagram")) {
                "large"  -> fileUrls.getJSONObject("standard_resolution")
                "medium" -> fileUrls.getJSONObject("low_resolution")
                else     -> null
            }!!.getString("url")

            plvUrl.type = getFileTypeFromUrl(displayUrl)
            plvUrl.displayUrl = displayUrl

            val imageUrls = data.getJSONObject("images")
            plvUrl.thumbUrl = imageUrls.getJSONObject("low_resolution").getString("url")
            plvUrl.biggestUrl = imageUrls.getJSONObject("standard_resolution").getString("url")

            return plvUrl
        }
    }

    private inner class GyazoSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://.*gyazo\\.com/(\\w+)")?.let { id ->
                val plvUrl = PLVUrl(url, "gyazo", id)

                plvUrl.displayUrl = "https://gyazo.com/${id}/raw"

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            }
        }
    }

    private inner class ImgurSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://.*imgur\\.com/([\\w^\\.]+)")?.let { id ->
                val plvUrl = PLVUrl(url, "imgur", id)

                val file_url = "http://i.imgur.com/${id}.jpg"
                plvUrl.displayUrl = file_url
                plvUrl.type = "jpg"

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            }
        }
    }

    private inner class OtherSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            val lastPath = Uri.parse(url).lastPathSegment
            if (lastPath != null) {
                val lastDotPosition = lastPath.lastIndexOf(".")
                val type = lastPath.substring(lastDotPosition + 1)

                val plvUrl: PLVUrl
                if (arrayOf("png", "jpg", "jpeg", "gif").contains(type)) {
                    plvUrl = PLVUrl(url, "other", lastPath.substring(0, lastDotPosition))
                    plvUrl.type = type
                } else {
                    plvUrl = PLVUrl(url, "other", lastPath)
                }
                plvUrl.displayUrl = url

                listener.onGetPLVUrlFinished(arrayOf(plvUrl))
            } else {
                onParseFailed()
            }
        }
    }

    private inner class FlickrSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            when {
                url.contains("flickr")  -> super.getId(url, "^https?://[wm]w*\\.flickr\\.com/?#?/photos/[\\w@]+/(\\d+)")
                url.contains("flic.kr") -> super.getId(url, "^https?://flic\\.kr/p/(\\w+)")?.let { Base58.decode(it) }
                else                    -> null
            }?.let { id ->
                val plvUrl = PLVUrl(url, "flickr", id)

                val api_key = PhotoLinkViewer.getFlickrKey()
                val request = "https://api.flickr.com/services/rest/?method=flickr.photos.getInfo&format=json&nojsoncallback=1&api_key=${api_key}&photo_id=${id}"

                VolleyManager.getRequestQueue(context).add(MyJsonObjectRequest(context, request,
                        Response.Listener { response ->
                            try {
                                listener.onGetPLVUrlFinished(arrayOf(parseFlickr(response, plvUrl)))
                            } catch (e: JSONException) {
                                listener.onGetPLVUrlFailed(context.getString(R.string.plv_core_show_flickrjson_toast))
                                e.printStackTrace()
                            }
                        })
                );
            }
        }

        @Throws(JSONException::class)
        private fun parseFlickr(json: JSONObject, plvUrl: PLVUrl): PLVUrl {
            //for flickr
            val photo = JSONObject(json.getString("photo"))
            val farm = photo.getString("farm")
            val server = photo.getString("server")
            val id = photo.getString("id")
            val secret = photo.getString("secret")

            val original_secrets = photo.getString("originalsecret")
            val original_formats = photo.getString("originalformat")

            plvUrl.biggestUrl = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + original_secrets + "_o." + original_formats
            plvUrl.thumbUrl = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_z.jpg"
            val quality = super.getQuality("flickr")
            when (quality) {
                "original" -> {
                    plvUrl.displayUrl = plvUrl.biggestUrl
                    plvUrl.type = original_formats
                }
                "large"    -> {
                    plvUrl.displayUrl = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_b.jpg"
                    plvUrl.type = "jpg"
                }
                "medium"   -> {
                    plvUrl.displayUrl = plvUrl.thumbUrl
                    plvUrl.type = "jpg"
                }
            }
            return plvUrl
        }
    }

    private inner class NicoSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            when {
                url.contains("nico.ms") -> super.getId(url, "^https?://nico\\.ms/im(\\d+)")
                else                    -> super.getId(url, "^https?://seiga.nicovideo.jp/seiga/im(\\d+)")
            }?.let { id ->
                val plvUrl = PLVUrl(url, "nico", id)

                RedirectUrlController(object : Callback {
                    override fun onResponse(response: com.squareup.okhttp.Response) {
                        Handler(Looper.getMainLooper()).post {
                            listener.onGetPLVUrlFinished(arrayOf(parseNico(response.request().urlString(), id, plvUrl)))
                        }
                    }

                    override fun onFailure(request: Request, e: IOException) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            listener.onGetPLVUrlFailed("connection error!")
                        }
                    }
                }).getRedirect("http://seiga.nicovideo.jp/image/source/" + id)
            }
        }

        private fun parseNico(redirect: String, id: String, plvUrl: PLVUrl): PLVUrl {
            var biggest_url = redirect.replace("/o/", "/priv/")

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val original = sharedPreferences.isOriginalEnabled(wifiChecker(sharedPreferences))

            val quality = super.getQuality("nicoseiga")

            if (redirect.contains("account.nicovideo.jp")) {
                // cannot preview original photo
                biggest_url = "http://lohas.nicoseiga.jp/img/" + id + "l"
                if (original || quality == "original") {
                    Toast.makeText(context, context.getString(R.string.plv_core_nico_original_toast), Toast.LENGTH_LONG).show()
                }
            }

            plvUrl.biggestUrl = biggest_url
            plvUrl.thumbUrl = "http://lohas.nicoseiga.jp/img/" + id + "m"
            plvUrl.displayUrl = when (quality) {
                "original" -> biggest_url
                "large"    -> "http://lohas.nicoseiga.jp/img/" + id + "l"
                "medium"   -> plvUrl.thumbUrl
                else       -> null
            }

            return plvUrl
        }
    }

    private inner class VineSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            super.getId(url, "^https?://vine\\.co/v/(\\w+)")?.let { id ->
                val plvUrl = PLVUrl(url, "vine", id)

                plvUrl.isVideo = true
                plvUrl.type = "mp4"

                val request = "https://api.vineapp.com/timelines/posts/s/" + id

                VolleyManager.getRequestQueue(context).add(MyJsonObjectRequest(context, request,
                        Response.Listener { response ->
                            try {
                                listener.onGetPLVUrlFinished(arrayOf(parseVine(response, plvUrl)))
                            } catch (e: JSONException) {
                                listener.onGetPLVUrlFailed(context.getString(R.string.plv_core_show_flickrjson_toast))
                                e.printStackTrace()
                            }
                        })
                );
            }
        }

        @Throws(JSONException::class)
        private fun parseVine(json: JSONObject, plvUrl: PLVUrl): PLVUrl {
            val records = json.getJSONObject("data").getJSONArray("records").getJSONObject(0)
            plvUrl.displayUrl = records.getString("videoUrl")
            plvUrl.thumbUrl = records.getString("thumbnailUrl")
            return plvUrl
        }
    }

    private inner class TumblrSite(url: String, context: Context, listener: PLVUrlListener) : Site(url, context, listener) {

        override fun getPLVUrl() {
            if (!url.contains("tmblr.co")) {
                requestAPI(url)
            } else {
                RedirectUrlController(object : Callback {
                    override fun onResponse(response: com.squareup.okhttp.Response) {
                        Handler(Looper.getMainLooper()).post {
                            requestAPI(response.request().urlString())
                        }
                    }

                    override fun onFailure(request: Request, e: IOException) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            listener.onGetPLVUrlFailed("connection error!")
                        }
                    }
                }).getRedirect(url)
            }

            listener.onURLAccepted()
        }


        private fun requestAPI(regularUrl: String) {
            val matcher = Pattern.compile("^https?://([^/]+)/post/(\\d+)").matcher(regularUrl)
            if (!matcher.find()) {
                super.onParseFailed()
                return
            }

            val host = matcher.group(1)
            val id = matcher.group(2)

            val api_key = PhotoLinkViewer.getTumblrKey()
            val request = "https://api.tumblr.com/v2/blog/${host}/posts?api_key=${api_key}&id=${id}"

            VolleyManager.getRequestQueue(context).add(MyJsonObjectRequest(context, request,
                    Response.Listener { response ->
                        try {
                            listener.onGetPLVUrlFinished(parseTumblr(response, id))
                        } catch (e: JSONException) {
                            listener.onGetPLVUrlFailed("tumblr json parse error!")
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            listener.onGetPLVUrlFailed(e.message!!)
                        }
                    })
            );
        }

        @Throws(JSONException::class, IllegalStateException::class)
        private fun parseTumblr(json: JSONObject, id: String): Array<PLVUrl> {
            val post = json.getJSONObject("response").getJSONArray("posts").getJSONObject(0)
            post.getString("type").let {
                if (!"photo".equals(it)) throw IllegalStateException("Type of this post is ${it}, not photo!")
            }

            val quality = super.getQuality("tumblr")
            val photos = post.getJSONArray("photos")

            return (0..photos.length() - 1).map { i ->
                val plvUrl = PLVUrl(url, "tumblr", "${id}-${i}")

                val photo = photos.getJSONObject(i)
                plvUrl.biggestUrl = photo.getJSONObject("original_size").getString("url")

                val altPhotos = photo.getJSONArray("alt_sizes").let {
                    (0..it.length() - 1).map { i ->
                        Photo(it.getJSONObject(i))
                    }
                }.sortedBy { it.width }

                plvUrl.thumbUrl = altPhotos.getOrElse(2) { altPhotos.last() }.url
                val displayUrl = when (quality) {
                    "original" -> plvUrl.biggestUrl
                    "large"    -> altPhotos.getOrElse(4) { altPhotos.last() }.url
                    "medium"   -> altPhotos.getOrElse(3) { altPhotos.last() }.url
                    "small"    -> plvUrl.thumbUrl
                    else       -> null
                }
                plvUrl.displayUrl = displayUrl
                plvUrl.type = displayUrl?.let { getFileTypeFromUrl(it) }

                plvUrl
            }.toTypedArray()
        }

        private inner class Photo(photo: JSONObject) {
            val url = photo.getString("url")
            val width = photo.getInt("width")
            val height = photo.getInt("height")
        }
    }
}
