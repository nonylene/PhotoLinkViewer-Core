package net.nonylene.photolinkviewer.core.tool

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View

// coordinator behavior for snackbar.
@Suppress("unused")
class ForSnackbarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            val translationY = Math.min(0f, dependency.translationY - dependency.height)
            child?.translationY = translationY
            return true
        }
        return false
    }
}