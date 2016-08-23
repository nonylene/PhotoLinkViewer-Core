package net.nonylene.photolinkviewer.core

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import net.nonylene.photolinkviewer.core.adapter.OptionButtonsRecyclerAdapter
import net.nonylene.photolinkviewer.core.databinding.PlvCoreActivityOptionPreferenceBinding
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.viewmodel.OptionButtonViewModel


class PLVOptionButtonPreferenceActivity : AppCompatActivity() {

    val adapter = OptionButtonsRecyclerAdapter(null)

    @Suppress("MISSING_DEPENDENCY_CLASS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: PlvCoreActivityOptionPreferenceBinding
                = DataBindingUtil.setContentView(this, R.layout.plv_core_activity_option_preference)

        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val button = adapter.buttonList.removeAt(viewHolder.adapterPosition)
                adapter.buttonList.add(target.adapterPosition, button)
                adapter.notifyDataSetChanged()
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.buttonList.removeAt(viewHolder.adapterPosition)
                adapter.notifyDataSetChanged()
            }
        })
        helper.attachToRecyclerView(binding.recyclerView)

        adapter.buttonList.addAll(listOf(OptionButton.DOWNLOAD, OptionButton.OPEN_OTHER_APP, OptionButton.PREFERENCE))
        adapter.notifyDataSetChanged()

        binding.addButtonLayout?.setModel(OptionButtonViewModel(OptionButton.ADD_BUTTON, null))
    }
}
