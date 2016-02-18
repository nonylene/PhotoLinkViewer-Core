package net.nonylene.photolinkviewer.core.event

import android.view.View
import net.nonylene.photolinkviewer.core.tool.PLVUrl

class DownloadButtonEvent(val plvUrls: List<PLVUrl>)
class ShowFragmentEvent(val isToBeShown: Boolean)
class SnackbarEvent(val message: String, val actionMessage: String?, val actionListener: (view: View) -> Unit)
class RotateEvent(val isRightRotate: Boolean)
