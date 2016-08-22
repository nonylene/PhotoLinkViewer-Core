package net.nonylene.photolinkviewer.core.adapter

import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.bindView
import net.nonylene.photolinkviewer.core.R

import net.nonylene.photolinkviewer.core.model.OptionButton

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

        val actionButton: FloatingActionButton by bindView(R.id.option_button)

        fun bind(button: OptionButton, listener: ((OptionButton) -> Unit)?) {
            with(actionButton) {
                backgroundTintList = ColorStateList.valueOf(button.color)
                setImageResource(button.icon)
                setOnClickListener {
                    listener?.invoke(button)
                }
            }
        }
    }
}