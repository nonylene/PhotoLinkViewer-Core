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
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.bindView
import net.nonylene.photolinkviewer.core.PhotoLinkViewer
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.adapter.OptionFragmentRecyclerAdapter

import net.nonylene.photolinkviewer.core.dialog.SaveDialogFragment
import net.nonylene.photolinkviewer.core.event.DownloadButtonEvent
import net.nonylene.photolinkviewer.core.event.RotateEvent
import net.nonylene.photolinkviewer.core.event.BaseShowFragmentEvent
import net.nonylene.photolinkviewer.core.event.SnackbarEvent
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.tool.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import twitter4j.*
import twitter4j.auth.AccessToken
import java.io.File

/**
 * @see OptionFragment.createArguments
 */
class OptionFragment : Fragment() {

    private val baseView: View by bindView(R.id.option_base_view)
    private val baseButton: FloatingActionButton by bindView(R.id.basebutton)
    private val rotateLeftButton: FloatingActionButton by bindView(R.id.rotate_leftbutton)
    private val rotateRightButton: FloatingActionButton by bindView(R.id.rotate_rightbutton)

    private var lastSaveInfos: List<SaveDialogFragment.Info>? = null
    private var lastSaveDir: String? = null

    private var plvUrlsForSave: List<PLVUrl>? = null

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity) }
    private var applicationContext: Context? = null
    private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

    private var downloadPLVUrlStack: List<PLVUrl>? = null

    private var isOpen = false
    set(value) {
        field = value
        if (value) {
            ViewCompat.setRotation(baseButton, 0f)
            ViewCompat.animate(baseButton).rotationBy(180f).duration = 150
        } else {
            ViewCompat.setRotation(baseButton, 180f)
            ViewCompat.animate(baseButton).rotationBy(180f).duration = 150
        }
        adapter.adapterModelList = getFilteredOptionButtonsFromPreference(plvUrlsForSave != null)
    }

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

    private val adapter = OptionFragmentRecyclerAdapter { button ->
        onOptionButtonClick(button)
    }

    companion object {
        private val LIKE_CODE = 1
        private val RETWEET_CODE = 2
        private val STORAGE_PERMISSION_REQUEST = 3
        private val SAVE_DIALOG_CODE = 4

        private val TWITTER_ENABLED_KEY = "is_twitter_enabled"
        private val TWITTER_ID_KEY = "twitter_id_long"
        private val TWITTER_DEFAULT_SCREEN_KEY = "twitter_default_screen"
        private val URL_KEY = "url"

        /**
         * arguments for normal sites
         */
        fun createArguments(url: String): Bundle {
            return Bundle().apply {
                putString(URL_KEY, url)
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
                putString(URL_KEY, url)
                putBoolean(TWITTER_ENABLED_KEY, true)
                putLong(TWITTER_ID_KEY, tweetId)
                putString(TWITTER_DEFAULT_SCREEN_KEY, twitterDefaultScreenName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext = activity.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plv_core_option_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        // adapter -> empty list

        baseButton.setOnClickListener {
            // see setter
            isOpen = !isOpen
        }

        rotateRightButton.setOnClickListener {
            EventBus.getDefault().post(RotateEvent(true))
        }

        rotateLeftButton.setOnClickListener {
            EventBus.getDefault().post(RotateEvent(false))
        }
    }

    fun onOptionButtonClick(optionButton: OptionButton) {
        when (optionButton) {
            OptionButton.PREFERENCE -> {
                startActivity(Intent(activity, PhotoLinkViewer.getPreferenceActivityClass()))
            }
            OptionButton.OPEN_OTHER_APP -> {
                // get uri from bundle
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(arguments.getString(URL_KEY)))
                // open intent chooser
                startActivity(Intent.createChooser(intent, getString(R.string.plv_core_intent_title)))
            }
            OptionButton.TWEET_LIKE -> {
                TwitterDialogFragment().apply {
                    arguments = this@OptionFragment.arguments
                    setTargetFragment(this@OptionFragment, LIKE_CODE)
                    show(this@OptionFragment.fragmentManager, "like")
                }
            }
            OptionButton.TWEET_RETWEET -> {
                TwitterDialogFragment().apply {
                    arguments = this@OptionFragment.arguments
                    setTargetFragment(this@OptionFragment, RETWEET_CODE)
                    show(this@OptionFragment.fragmentManager, "retweet")
                }
            }
            OptionButton.DOWNLOAD -> {
                plvUrlsForSave?.let { plvUrls ->
                    // download direct
                    val infoPair = getSaveFragmentInfos(plvUrls)
                    if (preferences.isSkipDialog()) {
                        saveOrRequestPermission(infoPair.first, infoPair.second)
                    } else {
                        // open dialog
                        SaveDialogFragment().apply {
                            arguments = SaveDialogFragment.createArguments(infoPair.first, infoPair.second.toCollection(arrayListOf()))
                            setTargetFragment(this@OptionFragment, SAVE_DIALOG_CODE)
                            show(this@OptionFragment.fragmentManager, "Save")
                        }
                    }
                }
            }
            else -> {
                // nothing
            }
        }
    }

    override fun onResume() {
        EventBus.getDefault().register(this)
        super.onResume()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
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
            val id_long = arguments.getLong(TWITTER_ID_KEY)
            val screenName = arguments.getString(SCREEN_NAME_KEY)

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
                RETWEET_CODE -> {
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
    @Subscribe(sticky = true)
    fun onEvent(downloadButtonEvent: DownloadButtonEvent) {
        EventBus.getDefault().removeStickyEvent(downloadButtonEvent)
        // save to stack for base view download event
        if (downloadButtonEvent.addToStack) downloadPLVUrlStack = downloadButtonEvent.plvUrls
        setDlButton(downloadButtonEvent.plvUrls)
    }

    // eventBus catch event
    @Suppress("unused")
    @Subscribe
    fun onEvent(baseShowFragmentEvent: BaseShowFragmentEvent) {
        if (fragmentManager.findFragmentByTag(BaseShowFragment.SHOW_FRAGMENT_TAG) != baseShowFragmentEvent.fragment) return

        if (baseShowFragmentEvent.isToBeShown) {
            rotateRightButton.showWithAnimation()
            rotateLeftButton.showWithAnimation()
        } else {
            if (downloadPLVUrlStack != null) {
                // restore plvurl stack for base view download event
                setDlButton(downloadPLVUrlStack!!)
            } else {
                removeDLButton()
            }
            rotateRightButton.hide()
            rotateLeftButton.hide()
        }
    }

    // eventBus catch event
    @Suppress("unused")
    @Subscribe
    fun onEvent(snackbarEvent: SnackbarEvent) {
        Snackbar.make(baseView, snackbarEvent.message, Snackbar.LENGTH_LONG).apply {
            snackbarEvent.actionMessage?.let {
                setAction(it, snackbarEvent.actionListener)
            }
        }.show()
    }

    private fun setDlButton(plvUrls: List<PLVUrl>) {
        plvUrlsForSave = plvUrls
        adapter.adapterModelList = getFilteredOptionButtonsFromPreference(true)
    }

    private fun removeDLButton() {
        adapter.adapterModelList = getFilteredOptionButtonsFromPreference(false)
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
        val doMakeDir = preferences.getDownloadDirType() == "mkdir"
        if (doMakeDir) {
            // make directory
            dir = File(root, directory + "/" + plvUrl.siteName)
        } else {
            // not make directory
            dir = File(root, directory)
        }
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
            var fileName = if (doMakeDir) it.fileName else "${it.siteName}-${it.fileName}"
            it.type?.let { fileName += "." + it }
            SaveDialogFragment.Info(fileName, if (original) it.biggestUrl!! else it.displayUrl!!, it.thumbUrl!!)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SAVE_DIALOG_CODE) {
            data?.let {
                saveOrRequestPermission(it.getStringExtra(SaveDialogFragment.DIR_KEY), it.getParcelableArrayListExtra(SaveDialogFragment.INFO_KEY))
            }
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

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun createdFavorite(status: Status?) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.plv_core_twitter_like_toast), Toast.LENGTH_LONG).show()
                    }
                }

                override fun retweetedStatus(status: Status?) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.plv_core_twitter_retweet_toast), Toast.LENGTH_LONG).show()
                    }
                }
            })
            when (requestCode) {
                LIKE_CODE -> twitter.createFavorite(id_long)
                RETWEET_CODE -> twitter.retweetStatus(id_long)
            }
        }
    }

    /**
     * this method see [isOpen]
     */
    private fun getFilteredOptionButtonsFromPreference(includeDownload: Boolean): List<OptionButton> {
        return PreferenceManager.getDefaultSharedPreferences(context).getOptionButtons().filter {
            isOpen && when (it) {
                OptionButton.DOWNLOAD -> includeDownload
                OptionButton.TWEET_LIKE, OptionButton.TWEET_RETWEET ->
                    arguments.getBoolean(TWITTER_ENABLED_KEY)
                else -> true
            }
        }
    }
}
