package net.nonylene.photolinkviewer.core.viewmodel

import android.databinding.ObservableField
import android.view.View
import net.nonylene.photolinkviewer.core.model.OptionButton

class OptionButtonFragmentViewModel {

    constructor(button: OptionButton, listener: ((OptionButton) -> Unit)?) {
        this.button.set(button)
        this.listener = listener
    }

    val button = ObservableField<OptionButton>()
    var listener: ((OptionButton) -> Unit)? = null

    fun onBaseViewClick(view: View) {
        listener?.invoke(button.get())
    }
}
