package net.nonylene.photolinkviewer.core.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Matrix
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.app.DialogFragment
import android.content.res.Configuration
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import net.nonylene.photolinkviewer.core.PLVMaxSizePreferenceActivity
import net.nonylene.photolinkviewer.core.R


import net.nonylene.photolinkviewer.core.async.AsyncHttpBitmap
import net.nonylene.photolinkviewer.core.event.DownloadButtonEvent
import net.nonylene.photolinkviewer.core.event.RotateEvent
import net.nonylene.photolinkviewer.core.event.BaseShowFragmentEvent
import net.nonylene.photolinkviewer.core.event.SnackbarEvent
import net.nonylene.photolinkviewer.core.tool.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ShowFragment : BaseShowFragment() {

    private val imageView: ImageView by bindView(R.id.imgview)
    private val showFrameLayout: FrameLayout by bindView(R.id.showframe)
    private val progressBar: ProgressBar by bindView(R.id.showprogress)

    private val preferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity) }
    private var firstScale = 1f
    private var quickScale: MyQuickScale? = null
    private var applicationContext : Context? = null

    companion object {

        private val IS_SINGLE_FRAGMENT_KEY = "is_single"
        private val PLV_URL_KEY = "plvurl"

        /**
         * @param isSingleFragment if true, background color and progressbar become transparent in this fragment.
         */
        fun createArguments(plvUrl: PLVUrl, isSingleFragment: Boolean): Bundle {
            return Bundle().apply {
                putParcelable(PLV_URL_KEY, plvUrl)
                putBoolean(IS_SINGLE_FRAGMENT_KEY, isSingleFragment)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        applicationContext = activity.applicationContext
        return inflater.inflate(R.layout.plv_core_show_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scaleGestureDetector = ScaleGestureDetector(activity, simpleOnScaleGestureListener())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            scaleGestureDetector.isQuickScaleEnabled = false
        }

        val gestureDetector = GestureDetector(activity, simpleOnGestureListener())

        imageView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (!scaleGestureDetector.isInProgress) {
                gestureDetector.onTouchEvent(event)
            }
            when (event.action) {
            // image_view double_tap quick scale
                MotionEvent.ACTION_MOVE -> quickScale?.onMove(event)
                MotionEvent.ACTION_UP   -> quickScale = null
            }
            true
        }

        if (!preferences.getBoolean("initialized39", false)) {
            Initialize.initialize39(activity)
        }

        if (!preferences.isInitialized47()) {
            Initialize.initialize47(activity)
        }

        if (arguments.getBoolean(IS_SINGLE_FRAGMENT_KEY)) {
            showFrameLayout.setBackgroundResource(R.color.plv_core_transparent)
            progressBar.visibility = View.GONE
        }

        EventBus.getDefault().postSticky(DownloadButtonEvent(listOf(arguments.getParcelable(PLV_URL_KEY)), false))
        AsyncExecute(arguments.getParcelable(PLV_URL_KEY)).Start()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    internal inner class simpleOnGestureListener : GestureDetector.SimpleOnGestureListener() {
        var isDoubleZoomDisabled: Boolean = false

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // drag photo
            val matrix = Matrix()
            matrix.set(imageView.imageMatrix)
            val values = FloatArray(9)
            matrix.getValues(values)
            // move photo
            values[Matrix.MTRANS_X] = values[Matrix.MTRANS_X] - distanceX
            values[Matrix.MTRANS_Y] = values[Matrix.MTRANS_Y] - distanceY
            matrix.setValues(values)
            imageView.imageMatrix = matrix
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            isDoubleZoomDisabled = preferences.isDoubleZoomDisabled()
            quickScale = MyQuickScale(e, !isDoubleZoomDisabled)
            if (!isDoubleZoomDisabled) doubleZoom(e)
            return super.onDoubleTap(e)
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_UP) {
                quickScale?.let {
                    if (isDoubleZoomDisabled && !it.moved) doubleZoom(e)
                    quickScale = null
                }
            }
            return false
        }

        private fun doubleZoom(e: MotionEvent) {
            val touchX = e.x
            val touchY = e.y
            imageView.startAnimation(ScaleAnimation(1f, 2f, 1f, 2f, touchX, touchY).apply {
                duration = 200
                isFillEnabled = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        val matrix = Matrix()
                        matrix.set(imageView.imageMatrix)
                        matrix.postScale(2f, 2f, touchX, touchY)
                        imageView.imageMatrix = matrix
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }
                })
            })
        }
    }

    private inner class MyQuickScale(e: MotionEvent, double_frag: Boolean) {
        // quick scale zoom
        private val initialY: Float
        private val initialX: Float
        private var basezoom: Float = 0f
        private var old_zoom: Float = 0f
        var moved = false

        init {
            initialY = e.y
            initialX = e.x
            //get current status
            val values = FloatArray(9)
            val matrix = Matrix()
            matrix.set(imageView.imageMatrix)
            matrix.getValues(values)
            //set base zoom param
            basezoom = values[Matrix.MSCALE_X]
            if (basezoom == 0f) basezoom = Math.abs(values[Matrix.MSKEW_X])
            // double tap
            if (double_frag) basezoom *= 2
            old_zoom = 1f
        }

        fun onMove(e: MotionEvent) {
            moved = true
            val touchY = e.y
            val matrix = Matrix()
            matrix.set(imageView.imageMatrix)
            // adjust zoom speed
            // If using preference_fragment, value is saved to DefaultSharedPref.
            val zoomSpeed = preferences.getZoomSpeed()
            val new_zoom = Math.pow((touchY / initialY).toDouble(), (zoomSpeed * 2).toDouble()).toFloat()
            // photo's zoom scale (is relative to old zoom value.)
            val scale = new_zoom / old_zoom
            if (new_zoom > firstScale / basezoom * 0.8) {
                old_zoom = new_zoom
                matrix.postScale(scale, scale, initialX, initialY)
                imageView.imageMatrix = matrix
            }
        }
    }

    internal inner class simpleOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var touchX: Float = 0f
        private var touchY: Float = 0f
        private var basezoom: Float = 0f
        private var old_zoom: Float = 0f

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            //define zoom-base point
            touchX = detector.focusX
            touchY = detector.focusY
            //get current status
            val values = FloatArray(9)
            val matrix = Matrix()
            matrix.set(imageView.imageMatrix)
            matrix.getValues(values)
            //set base zoom param
            basezoom = values[Matrix.MSCALE_X]
            if (basezoom == 0f) basezoom = Math.abs(values[Matrix.MSKEW_X])
            old_zoom = 1f
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val matrix = Matrix()
            matrix.set(imageView.imageMatrix)
            // adjust zoom speed
            // If using preference_fragment, value is saved to DefaultSharedPref.
            val zoomSpeed = preferences.getZoomSpeed()
            val new_zoom = Math.pow(detector.scaleFactor.toDouble(), zoomSpeed.toDouble()).toFloat()
            // photo's zoom scale (is relative to old zoom value.)
            val scale = new_zoom / old_zoom
            if (new_zoom > firstScale / basezoom * 0.8) {
                old_zoom = new_zoom
                matrix.postScale(scale, scale, touchX, touchY)
                imageView.imageMatrix = matrix
            }
            return super.onScale(detector)
        }
    }

    inner class AsyncExecute(private val plvUrl: PLVUrl) : LoaderManager.LoaderCallbacks<AsyncHttpBitmap.Result> {

        fun Start() {
            //there are some loaders, so restart(all has finished)
            val bundle = Bundle().apply {
                putParcelable("plvurl", plvUrl)
            }
            loaderManager.restartLoader(0, bundle, this)
        }

        override fun onCreateLoader(id: Int, bundle: Bundle): Loader<AsyncHttpBitmap.Result> {
            val max_size = preferences.getImageViewMaxSize() * 1024
            return AsyncHttpBitmap(activity.applicationContext, bundle.getParcelable<PLVUrl>("plvurl"), max_size)
        }

        override fun onLoadFinished(loader: Loader<AsyncHttpBitmap.Result>, result: AsyncHttpBitmap.Result) {
            plvUrl.type = result.type
            plvUrl.height = result.originalHeight
            plvUrl.width = result.originalWidth

            val bitmap = result.bitmap

            if (bitmap == null) {
                Toast.makeText(applicationContext, getString(R.string.plv_core_show_bitamap_error) +
                        result.errorMessage?.let { "\n" + it }, Toast.LENGTH_LONG).show()
                return
            }

            EventBus.getDefault().postSticky(DownloadButtonEvent(listOf(plvUrl), false))

            val display = activity.windowManager.defaultDisplay
            val displaySize = Point()
            display.getSize(displaySize)

            // get picture scale
            val widthScale = displaySize.x / bitmap.width.toFloat()
            val heightScale = displaySize.y / bitmap.height.toFloat()
            val minScale = Math.min(widthScale, heightScale)

            if (preferences.isAdjustZoom() || minScale < 1) {
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            firstScale = Math.min(minScale, 1f)

            if (result.isResized) {
                // avoid crash after fragment closed
                activity.let { activity ->
                    EventBus.getDefault().post(
                            SnackbarEvent(getString(R.string.plv_core_resize_message) + result.originalWidth + "x" + result.originalHeight,
                                    getString(R.string.plv_core_resize_action_message), {
                                MaxSizeDialogFragment().show(activity.fragmentManager, "about")
                            }))
                }
            }

            if ("gif" == result.type) {
                Glide.with(this@ShowFragment)
                        .load(plvUrl.displayUrl)
                        .dontAnimate()
                        .into(object : GlideDrawableImageViewTarget(imageView) {
                            override fun onResourceReady(resource: GlideDrawable, animation: GlideAnimation<in GlideDrawable>) {
                                super.onResourceReady(resource, animation)
                                afterImageSet()
                            }
                        })
                return
            } else {
                // set image
                imageView.setImageBitmap(bitmap)
                afterImageSet()
            }
        }

        fun afterImageSet() {
            removeProgressBar()
            // change scale to MATRIX
            imageView.scaleType = ImageView.ScaleType.MATRIX
            EventBus.getDefault().post(BaseShowFragmentEvent(this@ShowFragment, true))
        }

        override fun onLoaderReset(loader: Loader<AsyncHttpBitmap.Result>) {

        }
    }


    @Suppress("unused")
    @Subscribe
    fun onEvent(rotateEvent: RotateEvent) {
        rotateImg(rotateEvent.isRightRotate)
    }

    private fun rotateImg(right: Boolean) {
        //get display size
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        imageView.imageMatrix = Matrix().apply {
            set(imageView.imageMatrix)
            postRotate(if (right) 90f else -90f, (size.x / 2).toFloat(), (size.y / 2).toFloat())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageView.setImageBitmap(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().post(BaseShowFragmentEvent(this, false))
    }

    private fun removeProgressBar() {
        showFrameLayout.removeView(progressBar)
        (activity as? ProgressBarListener)?.hideProgressBar()
    }

    class MaxSizeDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
            return AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.plv_core_image_resized_dialog_title))
                    .setMessage(getString(R.string.plv_core_image_resized_dialog_text))
                    .setPositiveButton(getString(android.R.string.ok), null)
                    .setNeutralButton(getString(R.string.plv_core_image_resized_dialog_neutral), { dialogInterface, i ->
                        startActivity(Intent(activity, PLVMaxSizePreferenceActivity::class.java))
                    })
                    .create()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_PORTRAIT -> {
                val matrix = Matrix().apply {
                    set(imageView.imageMatrix)
                }
                val values = FloatArray(9)
                matrix.getValues(values)
                // swap XY
                values[Matrix.MTRANS_X].let {
                    values[Matrix.MTRANS_X] = values[Matrix.MTRANS_Y]
                    values[Matrix.MTRANS_Y] = it
                }
                matrix.setValues(values)
                imageView.imageMatrix = matrix
            }
        }
    }
}