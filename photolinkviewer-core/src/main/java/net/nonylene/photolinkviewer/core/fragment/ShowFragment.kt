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
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.bindView
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
    private var firstzoom = 1f
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
            if (new_zoom > firstzoom / basezoom * 0.8) {
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
            if (new_zoom > firstzoom / basezoom * 0.8) {
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

            if ("gif" == result.type) {
                addWebView(plvUrl)
                return
            } else {
                removeProgressBar()
            }

            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val dispWidth = size.x
            val dispHeight = size.y

            //get bitmap size
            val origWidth = bitmap.width.toFloat()
            val origHeight = bitmap.height.toFloat()
            //set image
            imageView.setImageBitmap(bitmap)
            //get matrix from imageview
            val matrix = Matrix()
            matrix.set(imageView.matrix)
            //get display size
            val wid = dispWidth / origWidth
            val hei = dispHeight / origHeight
            val zoom = Math.min(wid, hei)
            val initX: Float
            val initY: Float
            if (preferences.isAdjustZoom() || zoom < 1) {
                //zoom
                matrix.setScale(zoom, zoom)
                if (wid < hei) {
                    //adjust width
                    initX = 0f
                    initY = (dispHeight - origHeight * wid) / 2
                } else {
                    //adjust height
                    initX = (dispWidth - origWidth * hei) / 2
                    initY = 0f
                }
                if (zoom < 1) {
                    firstzoom = zoom
                }
            } else {
                //move
                initX = (dispWidth - origWidth) / 2
                initY = (dispHeight - origHeight) / 2
            }

            matrix.postTranslate(initX, initY)
            imageView.imageMatrix = matrix

            EventBus.getDefault().post(BaseShowFragmentEvent(this@ShowFragment, true))

            // avoid crash after fragment closed
            if (result.isResized) {
                activity.let { activity ->
                    EventBus.getDefault().post(
                            SnackbarEvent(getString(R.string.plv_core_resize_message) + result.originalWidth + "x" + result.originalHeight,
                                    getString(R.string.plv_core_resize_action_message), {
                                MaxSizeDialogFragment().show(activity.fragmentManager, "about")
                            }))
                }
            }
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

    private fun addWebView(plvUrl: PLVUrl) {
        val videoWidth = plvUrl.width
        val videoHeight = plvUrl.height

        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val dispWidth = size.x
        val dispHeight = size.y

        // gif view by web view
        val webView = WebView(activity).apply {
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
        }

        val layoutParams: FrameLayout.LayoutParams
        val escaped = TextUtils.htmlEncode(plvUrl.displayUrl)
        val html: String
        if ((videoHeight > dispHeight * 0.9 && videoWidth * dispHeight / dispHeight < dispWidth) || dispWidth * videoHeight / videoWidth > dispHeight) {
            // if height of video > disp_height * 0.9, check whether calculated width > disp_width . if this is true,
            // give priority to width. and, if check whether calculated height > disp_height, give priority to height.
            val width = (dispWidth * 0.9).toInt()
            val height = (dispHeight * 0.9).toInt()
            layoutParams = FrameLayout.LayoutParams(width, height)
            html = "<html><body><img style='display: block; margin: 0 auto' height='100%'src='$escaped'></body></html>"
        } else {
            val width = (dispWidth * 0.9).toInt()
            layoutParams = FrameLayout.LayoutParams(width, width * videoHeight / videoWidth)
            html = "<html><body><img style='display: block; margin: 0 auto' width='100%'src='$escaped'></body></html>"
        }
        layoutParams.gravity = Gravity.CENTER

        webView.apply {
            setLayoutParams(layoutParams)
            // html to centering
            loadData(html, "text/html", "utf-8")
            setBackgroundColor(0)
            settings.builtInZoomControls = true
        }

        removeProgressBar()
        showFrameLayout.addView(webView)
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