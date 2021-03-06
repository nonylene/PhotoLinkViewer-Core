package net.nonylene.photolinkviewer.core

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import net.nonylene.photolinkviewer.core.adapter.OptionButtonsPreferenceRecyclerAdapter
import net.nonylene.photolinkviewer.core.databinding.PlvCoreActivityOptionButtonPreferenceBinding
import net.nonylene.photolinkviewer.core.dialog.AddOptionButtonDialogFragment
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.tool.getOptionButtons
import net.nonylene.photolinkviewer.core.tool.putOptionButtons
import net.nonylene.photolinkviewer.core.viewmodel.OptionButtonPreferenceViewModel


class PLVOptionButtonPreferenceActivity : AppCompatActivity(), AddOptionButtonDialogFragment.Listener {
    val adapter = OptionButtonsPreferenceRecyclerAdapter(null)

    @Suppress("MISSING_DEPENDENCY_CLASS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: PlvCoreActivityOptionButtonPreferenceBinding
                = DataBindingUtil.setContentView(this, R.layout.plv_core_activity_option_button_preference)

        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }

        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val button = adapter.buttonList.removeAt(viewHolder.adapterPosition)
                adapter.buttonList.add(target.adapterPosition, button)
                saveOptionsButtons(adapter.buttonList)
                adapter.notifyDataSetChanged()
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.buttonList.removeAt(viewHolder.adapterPosition)
                saveOptionsButtons(adapter.buttonList)
                adapter.notifyDataSetChanged()
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.ACTION_STATE_SWIPE -> {
                        (viewHolder as? OptionButtonsPreferenceRecyclerAdapter.ViewHolder)?.let {
                            it.binding.itemBaseView.alpha = 0.5f
                        }
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                (viewHolder as OptionButtonsPreferenceRecyclerAdapter.ViewHolder).binding.itemBaseView.alpha = 1.0f
            }
        })
        helper.attachToRecyclerView(binding.recyclerView)

        adapter.buttonList.addAll(PreferenceManager.getDefaultSharedPreferences(this).getOptionButtons())
        adapter.notifyDataSetChanged()

        // add button -> click to open view
        binding.addButtonLayout?.setModel(OptionButtonPreferenceViewModel(OptionButton.ADD_BUTTON, null))
        with(binding.addButtonLayout.itemBaseView) {
            // todo: api 15
            setBackgroundDrawable(null)
            setOnClickListener(null)
        }
        binding.addButtonLayout.optionButton.setOnClickListener {
            onAddButtonClicked()
        }
    }

    fun saveOptionsButtons(buttons: List<OptionButton>) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putOptionButtons(buttons).apply()
    }

    fun onAddButtonClicked() {
        AddOptionButtonDialogFragment().show(supportFragmentManager, "add")
    }

    override fun onAddingButtonSelected(button: OptionButton) {
        adapter.buttonList.add(button)
        adapter.notifyDataSetChanged()
        saveOptionsButtons(adapter.buttonList)
    }
}
