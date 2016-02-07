package net.nonylene.photolinkviewer.core

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import net.nonylene.photolinkviewer.core.fragment.PLVPreferenceFragment

import net.nonylene.photolinkviewer.core.tool.Initialize

class PLVPreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!preferences.getBoolean("initialized39", false)) {
            Initialize.initialize39(this)
        }
        fragmentManager.beginTransaction().replace(android.R.id.content, PLVPreferenceFragment()).commit()
    }
}