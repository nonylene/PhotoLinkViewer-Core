package net.nonylene.photolinkviewer.core.dialog

import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.adapter.OptionButtonsRecyclerAdapter
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.tool.getOptionButtons

class AddOptionButtonDialogFragment : DialogFragment() {

    val adapter = OptionButtonsRecyclerAdapter {
        (activity as? Listener)?.onAddingButtonSelected(it)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // set custom view
        val recyclerView = View.inflate(context, R.layout.plv_core_add_option_button_dialog, null) as RecyclerView

        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val buttons = PreferenceManager.getDefaultSharedPreferences(context)
                .getOptionButtons().plus(OptionButton.ADD_BUTTON)
        adapter.buttonList.addAll(OptionButton.values().filterNot {
            buttons.contains(it)
        })
        adapter.notifyDataSetChanged()

        return AlertDialog.Builder(context).setView(recyclerView)
                .setTitle("Select button to add")
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface Listener {
        fun onAddingButtonSelected(button: OptionButton)
    }
}
