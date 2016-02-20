package net.nonylene.photolinkviewer.core.tool

import android.os.Parcel
import android.os.Parcelable

class PLVUrl : Parcelable {
    val url: String
    var thumbUrl: String? = null
        get() {
            return field ?: displayUrl
        }
    var displayUrl: String? = null
    var biggestUrl: String? = null
        get() {
            return field ?: displayUrl
        }
    val siteName: String
    val fileName: String
    var type: String? = null
    var height: Int = 0
    var width: Int = 0
    var isVideo = false

    constructor(url: String, siteName: String, fileName :String) {
        this.url = url
        this.siteName = siteName
        this.fileName = fileName
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeString(displayUrl)
        dest.writeString(thumbUrl)
        dest.writeString(biggestUrl)
        dest.writeString(siteName)
        dest.writeString(fileName)
        dest.writeString(type)
        dest.writeByte((if (isVideo) 1 else 0).toByte())
    }

    constructor(source: Parcel) {
        this.url = source.readString()
        this.displayUrl = source.readString()
        this.thumbUrl = source.readString()
        this.biggestUrl = source.readString()
        this.siteName = source.readString()
        this.fileName = source.readString()
        this.type = source.readString()
        this.isVideo = source.readByte().toInt() != 0
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        val CREATOR: Parcelable.Creator<PLVUrl> = object : Parcelable.Creator<PLVUrl> {
            override fun createFromParcel(source: Parcel): PLVUrl {
                return PLVUrl(source)
            }

            override fun newArray(size: Int): Array<PLVUrl?> {
                return arrayOfNulls(size)
            }
        }
    }
}
