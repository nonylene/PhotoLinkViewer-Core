package net.nonylene.photolinkviewer.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import net.nonylene.photolinkviewer.core.R

import net.nonylene.photolinkviewer.core.tool.PLVUrl
import java.util.*

class TilePhotoView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    // null -> canceled
    // arrayListOf(null) -> empty view
    private val frameLayoutListList = ArrayList<ArrayList<PLVUrl?>?>()
    private val inflater : LayoutInflater

    var tilePhotoViewListener: TilePhotoViewListener? = null

    init {
        inflater = LayoutInflater.from(context)
        orientation = LinearLayout.VERTICAL
    }

    // add empty view
    fun addImageView(): Int {
        // prev is last linear_layout
        frameLayoutListList.add(arrayListOf(null))
        return frameLayoutListList.size - 1
    }

    // remove canceled view
    @Suppress("unused")
    fun removeImageView(position: Int) {
        frameLayoutListList[position] = null
    }

    @Suppress("unused")
    fun setPLVUrl(position: Int, plvUrl: PLVUrl) {
        if (frameLayoutListList.size > position) frameLayoutListList.set(position, arrayListOf(plvUrl))
    }

    fun setPLVUrls(position: Int, plvUrls: Array<PLVUrl>) {
        if (frameLayoutListList.size > position) frameLayoutListList.set(position, plvUrls.toCollection(ArrayList()))
    }

    fun notifyChanged() {
        val frameLayoutCombinedList = frameLayoutListList.fold(ArrayList<PLVUrl?>()) { combined, list ->
            list?.let { combined.addAll(it) }
            combined
        }

        frameLayoutCombinedList.withIndex().forEach { iv ->
            val frameLayout: FrameLayout

            if (childCount > iv.index / 2) {
                frameLayout = (getChildAt((iv.index / 2).toInt()) as LinearLayout).getChildAt(iv.index % 2) as FrameLayout
            } else {
                // new generation
                if (iv.index % 2 == 0) {
                    // make new linear_layout and put below prev
                    val new_layout = inflater.inflate(R.layout.plv_core_tile_photos, this , false) as LinearLayout
                    addView(new_layout)
                    frameLayout = new_layout.getChildAt(0) as FrameLayout
                } else {
                    val prevLayout = getChildAt(childCount - 1) as LinearLayout
                    // put new photo below prev photo (not new linear_layout)
                    frameLayout = prevLayout.getChildAt(1) as FrameLayout
                }
            }

            val imageView = frameLayout.getChildAt(0) as ImageView

            iv.value?.let { plv ->
                if (plv.isVideo) {
                    frameLayout.getChildAt(1).visibility = View.VISIBLE
                    imageView.setOnClickListener {
                        tilePhotoViewListener?.onVideoShowFragmentRequired(plv)
                    }
                } else {
                    imageView.setOnClickListener {
                        tilePhotoViewListener?.onShowFragmentRequired(plv)
                    }
                }
                Glide.with(context.applicationContext).load(plv.thumbUrl).into(imageView)
            }
        }

        // reverse -> not removed
        ((frameLayoutCombinedList.size + 1) / 2..childCount - 1).forEach { removeViewAt(it) }
    }

    @Suppress("unused")
    fun initialize() {
        removeAllViews()
        frameLayoutListList.clear()
    }

    interface TilePhotoViewListener {
        fun onShowFragmentRequired(plvUrl: PLVUrl)
        fun onVideoShowFragmentRequired(plvUrl: PLVUrl)
    }
}
