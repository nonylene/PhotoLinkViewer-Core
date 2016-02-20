package net.nonylene.photolinkviewer.core.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.bindView
import de.greenrobot.event.EventBus
import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import net.nonylene.photolinkviewer.core.R

import net.nonylene.photolinkviewer.core.dialog.SaveDialogFragment
import net.nonylene.photolinkviewer.core.event.DownloadButtonEvent
import net.nonylene.photolinkviewer.core.event.RotateEvent
import net.nonylene.photolinkviewer.core.event.ShowFragmentEvent
import net.nonylene.photolinkviewer.core.event.SnackbarEvent
import net.nonylene.photolinkviewer.core.tool.*
import twitter4j.*
import twitter4j.auth.AccessToken
import java.io.File
import java.util.*

/**
 * @see OptionFragment.createArguments
 */
class OptionFragment : Fragment() {

    private val baseView: CoordinatorLayout by bindView(R.id.option_base_view)
    private val baseButton: FloatingActionButton by bindView(R.id.basebutton)
    private val settingButton: FloatingActionButton by bindView(R.id.setbutton)
    private val webButton: FloatingActionButton by bindView(R.id.webbutton)
    private val downLoadButton: FloatingActionButton by bindView(R.id.dlbutton)
    private val rotateLeftButton: FloatingActionButton by bindView(R.id.rotate_leftbutton)
    private val rotateRightButton: FloatingActionButton by bindView(R.id.rotate_rightbutton)
    private val retweetButton: FloatingActionButton by bindView(R.id.retweet_button)
    private val likeButton: FloatingActionButton by bindView(R.id.like_button)

    private var lastSaveInfos: List<SaveDialogFragment.Info>? = null
    private var lastSaveDir: String? = null

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity) }
    private var applicationContext : Context? = null
    private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

    private var isDownloadEnabled = false
    private var isOpen = false

    // by default, animation does not run if not isLaidOut().
    private fun FloatingActionButton.showWithAnimation() {
        alpha = 0f
        scaleY = 0f
        scaleX = 0f
        animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(fastOutSlowInInterpolator)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        visibility = View.VISIBLE
                    }
                })
    }

    companion object {
        private val LIKE_CODE = 1
        private val RETWEET_CODE = 2
        private val STORAGE_PERMISSION_REQUEST = 3
        private val SAVE_DIALOG_CODE = 4

        /**
         * arguments for normal sites
         */
        fun createArguments(url: String): Bundle {
            return Bundle().apply {
                setUrl(url)
            }
        }

        /**
         * arguments for twitter sites. Twitter options will be available by using this.
         * @param tweetId Tweet id used in retweet and favorite feature.
         * @param twitterDefaultScreenName Twitter screen name used as default account.
         * @see OptionFragment.TwitterDialogFragment
         */
        fun createArguments(url: String, tweetId: Long, twitterDefaultScreenName: String): Bundle {
            return Bundle().apply {
                setUrl(url)
                setTwitterEnabled(true)
                setTweetId(tweetId)
                setTwitterDefaultScreenName(twitterDefaultScreenName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext = activity.applicationContext
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plv_core_option_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseButton.setOnClickListener{ baseButton ->
            settingButton.animate().cancel()
            webButton.animate().cancel()
            retweetButton.animate().cancel()
            likeButton.animate().cancel()
            downLoadButton.animate().cancel()
            settingButton.animate().cancel()

            if (isOpen) {
                ViewCompat.setRotation(baseButton, 180f)
                ViewCompat.animate(baseButton).rotationBy(180f).duration = 150
                settingButton.hide()
                webButton.hide()
                if (arguments.isTwitterEnabled()) {
                    retweetButton.hide()
                    likeButton.hide()
                }
                if (isDownloadEnabled) downLoadButton.hide()
            } else {
                ViewCompat.setRotation(baseButton, 0f)
                ViewCompat.animate(baseButton).rotationBy(180f).duration = 150
                settingButton.showWithAnimation()
                webButton.showWithAnimation()
                if (arguments.isTwitterEnabled()) {
                    retweetButton.showWithAnimation()
                    likeButton.showWithAnimation()
                }
                if (isDownloadEnabled) downLoadButton.showWithAnimation()
            }
            isOpen = !isOpen
        }

        settingButton.setOnClickListener{
            startActivity(Intent(activity, PhotoLinkViewer.getPreferenceActivityClass()))
        }

        webButton.setOnClickListener{
            // get uri from bundle
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(arguments.getUrl()))
            // open intent chooser
            startActivity(Intent.createChooser(intent, getString(R.string.plv_core_intent_title)))
        }

        rotateRightButton.setOnClickListener {
            EventBus.getDefault().post(RotateEvent(true))
        }

        rotateLeftButton.setOnClickListener {
            EventBus.getDefault().post(RotateEvent(false))
        }

        val bundle = arguments

        if (bundle.isTwitterEnabled()) {
            retweetButton.setOnClickListener{
                TwitterDialogFragment().apply {
                    arguments = bundle
                    setTargetFragment(this@OptionFragment, RETWEET_CODE)
                    show(this@OptionFragment.fragmentManager, "retweet")
                }
            }

            likeButton.setOnClickListener{
                TwitterDialogFragment().apply {
                    arguments = bundle
                    setTargetFragment(this@OptionFragment, LIKE_CODE)
                    show(this@OptionFragment.fragmentManager, "like")
                }
            }
        }
    }

    class TwitterDialogFragment : DialogFragment() {
        private var requestCode: Int = 0

        companion object {
            val SCREEN_NAME_KEY = "screen_name"
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // get request code
            requestCode = targetRequestCode
            // get twitter id
            val id_long = arguments.getTweetId()
            val screenName = arguments.getTwitterDefaultScreenName()

            // get account_list
            val screen_list = PhotoLinkViewer.twitterTokenMap.keys.toList()
            // array_list to adapter
            val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, screen_list).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // get view
            val view = View.inflate(activity, R.layout.plv_core_spinner_dialog, null)
            val textView = view.findViewById(R.id.spinner_text) as TextView
            val spinner = (view.findViewById(R.id.accounts_spinner) as Spinner).apply {
                setAdapter(adapter)
                setSelection(screen_list.indexOf(screenName))
            }

            val builder = AlertDialog.Builder(activity)
            // change behave for request code
            when (requestCode) {
                LIKE_CODE -> {
                    textView.text = getString(R.string.plv_core_like_dialog_message)
                    builder.setTitle(getString(R.string.plv_core_like_dialog_title))
                }
                RETWEET_CODE  -> {
                    textView.text = getString(R.string.plv_core_retweet_dialog_message)
                    builder.setTitle(getString(R.string.plv_core_retweet_dialog_title))
                }
            }

            return builder
                    .setView(view)
                    .setPositiveButton(getString(android.R.string.ok), { dialog, which ->
                        val screen_name = screen_list[spinner.selectedItemPosition]
                        targetFragment.onActivityResult(requestCode, Activity.RESULT_OK,
                                Intent().putExtra(SCREEN_NAME_KEY, screen_name).putExtra(TWITTER_ID_KEY, id_long))
                    })
                    .setNegativeButton(getString(android.R.string.cancel), null)
                    .create()
        }
    }

    // eventBus catch event
    @Suppress("unused")
    fun onEvent(downloadButtonEvent: DownloadButtonEvent) {
        setDlButton(downloadButtonEvent.plvUrls)
    }

    // eventBus catch event
    @Suppress("unused")
    fun onEvent(showFragmentEvent: ShowFragmentEvent) {
        rotateLeftButton.animate().cancel()
        rotateRightButton.animate().cancel()
        if (showFragmentEvent.isToBeShown) {
            rotateRightButton.showWithAnimation()
            rotateLeftButton.showWithAnimation()
        } else {
            isDownloadEnabled = false
            removeDLButton()
            rotateRightButton.hide()
            rotateLeftButton.hide()
        }
    }

    // eventBus catch event
    @Suppress("unused")
    fun onEvent(snackbarEvent: SnackbarEvent) {
        Snackbar.make(baseView, snackbarEvent.message, Snackbar.LENGTH_LONG).apply {
            snackbarEvent.actionMessage?.let {
                setAction(it, snackbarEvent.actionListener)
            }
        }.show()
    }

    private fun setDlButton(plvUrls: List<PLVUrl>) {
        // dl button visibility and click
        downLoadButton.setOnClickListener {
            // download direct
            val infoPair = getSaveFragmentInfos(plvUrls)
            if (preferences.isSkipDialog()) {
                saveOrRequestPermission(infoPair.first, infoPair.second)
            } else {
                // open dialog
                SaveDialogFragment().apply {
                    arguments = SaveDialogFragment.createArguments(infoPair.first, infoPair.second.toCollection(ArrayList()))
                    setTargetFragment(this@OptionFragment, SAVE_DIALOG_CODE)
                    show(this@OptionFragment.fragmentManager, "Save")
                }
            }
        }
        downLoadButton.animate().cancel()
        if (isOpen && !isDownloadEnabled) downLoadButton.showWithAnimation()
        isDownloadEnabled = true
    }

    private fun removeDLButton() {
        // dl button visibility and click
        downLoadButton.setOnClickListener(null)
        downLoadButton.animate().cancel()
        if (isOpen) downLoadButton.hide()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                    save(lastSaveDir!!, lastSaveInfos!!)
                } else {
                    Toast.makeText(applicationContext!!, applicationContext!!.getString(R.string.plv_core_permission_denied), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveOrRequestPermission(dirName: String, infoList: List<SaveDialogFragment.Info>) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            lastSaveInfos = infoList
            lastSaveDir = dirName
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST)
        } else {
            save(dirName, infoList)
        }
    }

    private fun save(dirName: String, infoList: List<SaveDialogFragment.Info>) {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val stringBuilder = StringBuilder().apply {
            append(applicationContext!!.getString(R.string.plv_core_download_photo_title))
        }
        infoList.forEach { info ->
            val uri = Uri.parse(info.downloadUrl)
            val filename = info.fileName
            val path = File(dirName, filename)
            // save file
            val request = DownloadManager.Request(uri)
                    .setDestinationUri(Uri.fromFile(path))
                    .setTitle("PhotoLinkViewer")
                    .setDescription(filename)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            // notify
            if (preferences.isLeaveNotify()) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            downloadManager.enqueue(request)
            stringBuilder.append("\n${path.toString()},")
        }
        Toast.makeText(applicationContext!!, stringBuilder.toString(), Toast.LENGTH_LONG).show()
    }

    /**
     * @param plvUrls same sites
     */
    private fun getSaveFragmentInfos(plvUrls: List<PLVUrl>): Pair<String, List<SaveDialogFragment.Info>> {
        val plvUrl = plvUrls[0]
        // set download directory
        val directory = preferences.getDownloadDir()
        val root = Environment.getExternalStorageDirectory()

        val dir: File
        var filename: String
        if (preferences.getDownloadDirType() == "mkdir") {
            // make directory
            dir = File(root, directory + "/" + plvUrl.siteName)
            filename = plvUrl.fileName
        } else {
            // not make directory
            dir = File(root, directory)
            filename = plvUrl.siteName + "-" + plvUrl.fileName
        }
        plvUrl.type?.let { filename += "." + it }
        dir.mkdirs()

        //check wifi connecting and setting or not
        var isWifi = false
        if (preferences.isWifiEnabled()) {
            // get wifi status
            val manager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            isWifi = manager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }

        val original = preferences.isOriginalEnabled(isWifi)

        return Pair(dir.toString(), plvUrls.map {
            SaveDialogFragment.Info(filename, if (original) plvUrl.biggestUrl!! else plvUrl.displayUrl!!, plvUrl.thumbUrl!!)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SAVE_DIALOG_CODE) {
            saveOrRequestPermission(data!!.getStringExtra(SaveDialogFragment.DIR_KEY), data.getParcelableArrayListExtra(SaveDialogFragment.INFO_KEY))
        } else {
            val applicationContext = activity.applicationContext
            // request code > like or retweet
            // result code > row_id
            // intent > id_long
            val id_long = data!!.getLongExtra(TWITTER_ID_KEY, -1)
            val screenName = data.getStringExtra(TwitterDialogFragment.SCREEN_NAME_KEY)
            val twitter = AsyncTwitterFactory().instance
            with(PhotoLinkViewer.getTwitterKeys()) {
                twitter.setOAuthConsumer(consumerKey, consumerSecret)
            }
            with(PhotoLinkViewer.twitterTokenMap[screenName]!!) {
                twitter.oAuthAccessToken = AccessToken(accessToken, accessTokenSecret)
            }
            twitter.addListener(object : TwitterAdapter() {
                override fun onException(e: TwitterException?, twitterMethod: TwitterMethod?) {
                    val message = "${getString(R.string.plv_core_twitter_error_toast)}: ${e!!.statusCode}\n(${e.errorMessage})"

                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun createdFavorite(status: Status?) {
                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.plv_core_twitter_like_toast), Toast.LENGTH_LONG).show()
                    }
                }

                override fun retweetedStatus(status: Status?) {
                    Handler(Looper.getMainLooper()).post{
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.plv_core_twitter_retweet_toast), Toast.LENGTH_LONG).show()
                    }
                }
            })
            when (requestCode) {
                LIKE_CODE -> twitter.createFavorite(id_long)
                RETWEET_CODE  -> twitter.retweetStatus(id_long)
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}

private val TWITTER_ENABLED_KEY = "is_twitter_enabled"
private val TWITTER_ID_KEY = "twitter_id_long"
private val TWITTER_DEFAULT_SCREEN_KEY = "twitter_default_screen"
private val URL_KEY = "url"

fun Bundle.setTwitterEnabled(isTwitterEnabled: Boolean) {
    putBoolean(TWITTER_ENABLED_KEY, isTwitterEnabled)
}

fun Bundle.isTwitterEnabled() : Boolean {
    return getBoolean(TWITTER_ENABLED_KEY, false)
}

fun Bundle.setTweetId(tweetId: Long) {
    putLong(TWITTER_ID_KEY, tweetId)
}

fun Bundle.getTweetId() : Long {
    return getLong(TWITTER_ID_KEY)
}

fun Bundle.setUrl(url: String) {
    putString(URL_KEY, url)
}

fun Bundle.getUrl() : String {
    return getString(URL_KEY)
}

fun Bundle.setTwitterDefaultScreenName(screenName: String) {
    putString(TWITTER_DEFAULT_SCREEN_KEY, screenName)
}

fun Bundle.getTwitterDefaultScreenName() : String {
    return getString(TWITTER_DEFAULT_SCREEN_KEY)
}
