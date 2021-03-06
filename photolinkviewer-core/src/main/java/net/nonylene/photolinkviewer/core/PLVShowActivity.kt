package net.nonylene.photolinkviewer.core

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import net.nonylene.photolinkviewer.core.event.DownloadButtonEvent
import net.nonylene.photolinkviewer.core.fragment.BaseShowFragment
import net.nonylene.photolinkviewer.core.fragment.OptionFragment
import net.nonylene.photolinkviewer.core.fragment.ShowFragment
import net.nonylene.photolinkviewer.core.fragment.VideoShowFragment
import net.nonylene.photolinkviewer.core.tool.PLVUrl
import net.nonylene.photolinkviewer.core.tool.PLVUrlService
import net.nonylene.photolinkviewer.core.tool.ProgressBarListener
import net.nonylene.photolinkviewer.core.view.TilePhotoView
import org.greenrobot.eventbus.EventBus

/**
 * show photo Activity.
 * This Activity requires uri to preview, in data of intent.
 */
class PLVShowActivity : AppCompatActivity(), PLVUrlService.PLVUrlListener, ProgressBarListener, TilePhotoView.TilePhotoViewListener {

    private var isSingle: Boolean = true
    private var scrollView: ScrollView? = null
    private var tileView: TilePhotoView? = null

    private var isVisibleFront: Boolean = false
    private var fragmentPairAfterOnPause: Pair<Fragment, String?>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().removeAllStickyEvents()
        setContentView(R.layout.plv_core_activity_show)

        scrollView = findViewById(R.id.show_activity_scroll) as ScrollView
        tileView = findViewById(R.id.show_activity_tile) as TilePhotoView

        //receive intent
        if (Intent.ACTION_VIEW != intent.action) {
            Toast.makeText(this, "Intent Error!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val url = intent.data.toString()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val optionFragment = OptionFragment().apply {
            arguments = OptionFragment.createArguments(url)
        }
        fragmentTransaction.add(R.id.root_layout, optionFragment)
        fragmentTransaction.commit()

        PLVUrlService(this).apply {
            plvUrlListener = this@PLVShowActivity
        }.requestGetPLVUrl(url)

    }

    override fun onResume() {
        super.onResume()
        isVisibleFront = true
        fragmentPairAfterOnPause?.let {
            showFragment(it.first, it.second)
            fragmentPairAfterOnPause = null
        }
    }

    override fun onPause() {
        isVisibleFront = false
        super.onPause()
    }

    override fun onGetPLVUrlFinished(plvUrls: Array<PLVUrl>) {
        if (plvUrls.size == 1) {
            if (plvUrls[0].isVideo) onVideoShowFragmentRequired(plvUrls[0])
            else onShowFragmentRequired(plvUrls[0])
        } else {
            isSingle = false
            hideProgressBar()
            scrollView!!.visibility = View.VISIBLE
            tileView!!.tilePhotoViewListener = this
            tileView!!.setPLVUrls(tileView!!.addImageView(), plvUrls)
            tileView!!.notifyChanged()
        }
        EventBus.getDefault().postSticky(DownloadButtonEvent(plvUrls.toList(), plvUrls.size != 1))
    }

    override fun onGetPLVUrlFailed(text: String) {
        Toast.makeText(this@PLVShowActivity, text, Toast.LENGTH_LONG).show()
    }

    override fun onURLAccepted() {

    }

    override fun hideProgressBar() {
        findViewById(R.id.show_progress)?.visibility = View.GONE
    }

    override fun onShowFragmentRequired(plvUrl: PLVUrl) {
        val fragment = ShowFragment().apply {
            arguments = ShowFragment.createArguments(plvUrl, isSingle)
        }
        val tag = BaseShowFragment.SHOW_FRAGMENT_TAG
        if (isVisibleFront) showFragment(fragment, tag) else fragmentPairAfterOnPause = fragment to tag
    }

    override fun onVideoShowFragmentRequired(plvUrl: PLVUrl) {
        val fragment = VideoShowFragment().apply {
            arguments = VideoShowFragment.createArguments(plvUrl, isSingle)
        }
        val tag = BaseShowFragment.SHOW_FRAGMENT_TAG
        if (isVisibleFront) showFragment(fragment, tag) else fragmentPairAfterOnPause = fragment to tag
    }

    private fun showFragment(fragment: Fragment, tag: String?) {
        try {
            // go to show fragment
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            // back to this screen when back pressed
            if (!isSingle) fragmentTransaction.addToBackStack(null)
            fragmentTransaction.replace(R.id.show_frag_replace, fragment, tag)
            fragmentTransaction.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}
