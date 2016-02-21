package net.nonylene.photolinkviewer.core.event

import android.view.View
import net.nonylene.photolinkviewer.core.fragment.BaseShowFragment
import net.nonylene.photolinkviewer.core.tool.PLVUrl

// if add to stack this plv url, plvUrls for download button are restored after showFragment destroyed.
class DownloadButtonEvent(val plvUrls: List<PLVUrl>, val addToStack: Boolean)
class BaseShowFragmentEvent(val fragment: BaseShowFragment, val isToBeShown: Boolean)
class SnackbarEvent(val message: String, val actionMessage: String?, val actionListener: (view: View) -> Unit)
class RotateEvent(val isRightRotate: Boolean)
