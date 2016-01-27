package net.nonylene.photolinkviewer.core.event

import android.view.View
import net.nonylene.photolinkviewer.core.tool.PLVUrl

public class DownloadButtonEvent(val plvUrl: PLVUrl)
public class ShowFragmentEvent(val isToBeShown: Boolean)
public class SnackbarEvent(val message: String, val actionMessage: String?, val actionListener: (view: View) -> Unit)
public class RotateEvent(val isRightRotate: Boolean)
