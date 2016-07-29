package net.nonylene.photolinkviewer.core.view

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Checkable
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.bindView
import com.bumptech.glide.Glide
import net.nonylene.photolinkviewer.core.R

class SaveDialogItemView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs), Checkable {
    private var mChecked: Boolean = true
        set(value) {
            field = value
            checkedChangeListener?.invoke(value)
            // request focus to remove focus on other view
            requestFocus()
            fileNameEditText.isEnabled = value
            thumbImageView.imageAlpha = if (value) 0xFF else 0x66
            ViewCompat.animate(thumbImageView)
                    .scaleX(if (value) 0.85f else 1.0f)
                    .scaleY(if (value) 0.85f else 1.0f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(250)
                    .start()
            thumbCheckImageView.visibility = if (value) VISIBLE else GONE
        }

    private val thumbImageView: ImageView by bindView(R.id.path_image_view)
    private val thumbCheckImageView: ImageView by bindView(R.id.path_check_image_view)
    private val fileNameEditText: EditText by bindView(R.id.path_edit_text)

    var checkedChangeListener: ((checked: Boolean) -> Unit)? = null

    init {
        setOnClickListener { toggle() }
    }

    // pass event or not
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!mChecked) {
            // if not checked, intercept event on editText
            onTouchEvent(ev)
        }
        return !mChecked
    }

    override fun toggle() {
        mChecked = !mChecked
    }

    override fun isChecked(): Boolean {
        return mChecked
    }

    override fun setChecked(checked: Boolean) {
        mChecked = checked
    }

    fun getFileName(): String {
        return fileNameEditText.text.toString()
    }

    fun setFileName(path: CharSequence) {
        fileNameEditText.setText(path)
    }

    fun setThumbnailUrl(url: String) {
        Glide.with(context).load(url).into(thumbImageView)
    }
}
