package net.nonylene.photolinkviewer.tool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.android.volley.toolbox.ImageLoader

import net.nonylene.photolinkviewer.R
import net.nonylene.photolinkviewer.view.UserTweetLoadingView
import net.nonylene.photolinkviewer.view.UserTweetView

import java.util.ArrayList

import twitter4j.Status

class TwitterStatusAdapter(private val imageLoader: ImageLoader) : BaseAdapter(), UserTweetView.TwitterViewListener, UserTweetLoadingView.LoadingViewListener {

    private val statusList = ArrayList<Status?>()

    var twitterAdapterListener: TwitterAdapterListener? = null

    private var isRequesting: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val lastStatus: Status?
        get() {
            return statusList.last()?.let { it } ?: statusList.get(statusList.size() - 2)
        }

    override fun onShowFragmentRequired(plvUrl: PLVUrl) {
        twitterAdapterListener?.onShowFragmentRequired(plvUrl)
    }

    override fun onVideoShowFragmentRequired(plvUrl: PLVUrl) {
        twitterAdapterListener?.onVideoShowFragmentRequired(plvUrl)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewTypeEnum(position).id
    }

    private fun getItemViewTypeEnum(position: Int): ItemType {
        return statusList.get(position)?.let { ItemType.STATUS } ?: ItemType.LOADING
    }

    // not selectable base view
    override public fun isEnabled(position : Int) : Boolean{
        return getItemViewTypeEnum(position) != ItemType.STATUS;
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val inflater = LayoutInflater.from(parent!!.context)
        if (getItemViewTypeEnum(position) == ItemType.LOADING) {
            val loadView = (convertView?.let { it } ?: inflater.inflate(R.layout.loading_layout, parent, false)) as UserTweetLoadingView
            loadView.loadingViewListener = this
            loadView.setIsRequesting(isRequesting)
            return loadView
        } else {
            val tweetView = (convertView?.let { it } ?: inflater.inflate(R.layout.twitter_status_list, parent, false)) as UserTweetView
            tweetView.imageLoader = imageLoader
            tweetView.twitterViewListener = this
            tweetView.setEntry(getItem(position)!!)
            return tweetView
        }
    }

    override fun getItem(position: Int): Status? {
        return statusList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return statusList.size()
    }

    override fun getViewTypeCount(): Int {
        return ItemType.values().size()
    }


    public fun addItem(status: Status) {
        // init -> add loading footer
        if (statusList.isEmpty()) statusList.addAll(arrayOf(status, null))
        else statusList.add(statusList.size() - 1, status)

        if (status.inReplyToScreenName == null) statusList.remove(statusList.size() - 1)

        notifyDataSetChanged()

        if (status.inReplyToScreenName != null){
            // auto pager
            if (statusList.size() % 4 != 2) twitterAdapterListener?.onReadMoreClicked()
            else isRequesting = false
        }
    }

    override fun onReadMoreClicked() {
        twitterAdapterListener!!.onReadMoreClicked()
        isRequesting = true
    }

    private enum class ItemType internal constructor(val id: Int) {
        STATUS(0), LOADING(1)
    }

    interface TwitterAdapterListener {
        fun onShowFragmentRequired(plvUrl: PLVUrl)
        fun onVideoShowFragmentRequired(plvUrl: PLVUrl)
        fun onReadMoreClicked()
    }
}