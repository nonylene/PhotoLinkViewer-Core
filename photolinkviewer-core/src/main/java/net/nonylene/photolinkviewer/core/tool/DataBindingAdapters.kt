package net.nonylene.photolinkviewer.core.tool

import android.content.res.ColorStateList
import android.databinding.BindingAdapter
import android.support.annotation.ColorInt
import android.support.design.widget.FloatingActionButton
import android.widget.ImageView

object DataBindingAdapters {

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageViewResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }

    @BindingAdapter("app:backgroundTintColor")
    @JvmStatic
    fun setBackgroundTint(floatingActionButton: FloatingActionButton, @ColorInt color: Int) {
        floatingActionButton.backgroundTintList = ColorStateList.valueOf(color)
    }
}
