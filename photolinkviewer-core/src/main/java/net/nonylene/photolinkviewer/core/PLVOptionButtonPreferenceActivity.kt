package net.nonylene.photolinkviewer.core

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import net.nonylene.photolinkviewer.core.adapter.OptionButtonsRecyclerAdapter
import net.nonylene.photolinkviewer.core.model.OptionButton


class PLVOptionButtonPreferenceActivity : AppCompatActivity() {

    val adapter = OptionButtonsRecyclerAdapter(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plv_core_activity_option_preference)

        val recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.buttonList.addAll(listOf(OptionButton.DOWNLOAD, OptionButton.OPEN_OTHER_APP, OptionButton.PREFERENCE))
        adapter.notifyDataSetChanged()
    }

}
