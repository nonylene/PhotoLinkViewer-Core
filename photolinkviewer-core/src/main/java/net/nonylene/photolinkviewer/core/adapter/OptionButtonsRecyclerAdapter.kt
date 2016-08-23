package net.nonylene.photolinkviewer.core.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.databinding.PlvCoreOptionButtonItemBinding

import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.viewmodel.OptionButtonViewModel

class OptionButtonsRecyclerAdapter(val listener: ((OptionButton) -> Unit)?) : RecyclerView.Adapter<OptionButtonsRecyclerAdapter.ViewHolder>() {

    val buttonList = arrayListOf<OptionButton>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(buttonList[position], listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plv_core_option_button_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long = buttonList[position].hashCode().toLong()

    override fun getItemCount(): Int = buttonList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: PlvCoreOptionButtonItemBinding

        init {
            binding = DataBindingUtil.bind(itemView)
        }

        // https://youtrack.jetbrains.com/issue/KT-12402#u=1463619483291
        @Suppress("MISSING_DEPENDENCY_CLASS")
        fun bind(button: OptionButton, listener: ((OptionButton) -> Unit)?) {
            binding.setModel(OptionButtonViewModel(button, listener))
        }
    }
}