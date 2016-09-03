package net.nonylene.photolinkviewer.core.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.databinding.PlvCoreOptionButtonFragmentItemBinding
import net.nonylene.photolinkviewer.core.model.OptionButton
import net.nonylene.photolinkviewer.core.viewmodel.OptionButtonFragmentViewModel

class OptionFragmentRecyclerAdapter(val listener: (OptionButton) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_BUTTON = 1
    private val VIEW_TYPE_SPACE = 2

    var adapterModelList = listOf<OptionButton>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ButtonViewHolder -> holder.bind(adapterModelList[position], listener)
            else -> {
                // nothing
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when(viewType) {
            VIEW_TYPE_BUTTON -> {
                return ButtonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plv_core_option_button_fragment_item, parent, false))
            }
            else -> {
                return SpaceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plv_core_option_button_common_space_item, parent, false))
            }
        }
    }

    override fun getItemId(position: Int): Long  {
        when(getItemViewType(position)) {
            VIEW_TYPE_BUTTON -> return adapterModelList[position].hashCode().toLong() shl 32
            else -> return 0.toLong()
        }
    }

    override fun getItemCount(): Int = adapterModelList.size + 1

    override fun getItemViewType(position: Int): Int {
        if (adapterModelList.size > position) return VIEW_TYPE_BUTTON
        else return VIEW_TYPE_SPACE
    }

    class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: PlvCoreOptionButtonFragmentItemBinding

        init {
            binding = DataBindingUtil.bind(itemView)
        }

        // https://youtrack.jetbrains.com/issue/KT-12402#u=1463619483291
        @Suppress("MISSING_DEPENDENCY_CLASS")
        fun bind(button: OptionButton, listener: ((OptionButton) -> Unit)?) {
            binding.setModel(OptionButtonFragmentViewModel(button, listener))
        }
    }

    class SpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
