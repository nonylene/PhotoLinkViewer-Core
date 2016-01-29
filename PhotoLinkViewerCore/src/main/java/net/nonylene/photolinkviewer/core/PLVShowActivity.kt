package net.nonylene.photolinkviewer.core

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import net.nonylene.photolinkviewer.core.fragment.OptionFragment
import net.nonylene.photolinkviewer.core.fragment.ShowFragment
import net.nonylene.photolinkviewer.core.fragment.VideoShowFragment
import net.nonylene.photolinkviewer.core.tool.PLVUrl
import net.nonylene.photolinkviewer.core.tool.PLVUrlService
import net.nonylene.photolinkviewer.core.tool.ProgressBarListener
import net.nonylene.photolinkviewer.core.view.TilePhotoView

class PLVShowActivity : Activity(), PLVUrlService.PLVUrlListener, ProgressBarListener, TilePhotoView.TilePhotoViewListener {

    private var isSingle : Boolean = true
    private var scrollView : ScrollView? = null
    private var tileView : TilePhotoView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)

        scrollView = findViewById(R.id.show_activity_scroll) as ScrollView
        tileView = findViewById(R.id.show_activity_tile) as TilePhotoView

        //receive intent
        if (Intent.ACTION_VIEW != intent.action) {
            Toast.makeText(this, "Intent Error!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val url = intent.data.toString()

        val fragmentTransaction = fragmentManager.beginTransaction()
        val optionFragment = OptionFragment().apply {
            arguments = OptionFragment.createArguments(url)
        }
        fragmentTransaction.add(R.id.root_layout, optionFragment)
        fragmentTransaction.commit()

        PLVUrlService(this, this).requestGetPLVUrl(url)
    }

    override fun onGetPLVUrlFinished(plvUrls: Array<PLVUrl>) {
        val bundle = Bundle()
        bundle.putBoolean("single_frag", true)
        bundle.putParcelable("plvurl", plvUrls[0])

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
    }

    override fun onGetPLVUrlFailed(text: String) {
        Toast.makeText(this@PLVShowActivity, text, Toast.LENGTH_LONG).show()
    }

    override fun onURLAccepted() {

    }

    override fun hideProgressBar() {
        findViewById(R.id.show_progress).visibility = View.GONE
    }

    override fun onShowFragmentRequired(plvUrl: PLVUrl) {
        onFragmentRequired(ShowFragment(), plvUrl)
    }

    override fun onVideoShowFragmentRequired(plvUrl: PLVUrl) {
        onFragmentRequired(VideoShowFragment(), plvUrl)
    }

    private fun onFragmentRequired(fragment: Fragment, plvUrl: PLVUrl) {
        try {
            // go to show fragment
            val bundle = Bundle()
            bundle.putParcelable("plvurl", plvUrl)
            bundle.putBoolean("single_frag", isSingle)
            fragment.arguments = bundle

            val fragmentTransaction = fragmentManager.beginTransaction()
            // back to this screen when back pressed
            if (!isSingle) fragmentTransaction.addToBackStack(null)
            fragmentTransaction.replace(R.id.show_frag_replace, fragment)
            fragmentTransaction.commit()

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }
}
