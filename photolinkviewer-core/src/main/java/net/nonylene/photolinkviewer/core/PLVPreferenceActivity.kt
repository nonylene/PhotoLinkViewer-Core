package net.nonylene.photolinkviewer.core

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nonylene.photolinkviewer.core.fragment.PreferenceSummaryFragment
import net.nonylene.photolinkviewer.core.tool.Initialize
import net.nonylene.photolinkviewer.core.tool.isInitialized47

class PLVPreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!preferences.getBoolean("initialized39", false)) {
            Initialize.initialize39(this)
        }
        if (!preferences.isInitialized47()) {
            Initialize.initialize47(this)
        }
        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceSummaryFragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            addPreferencesFromResource(R.xml.preference)

            val maxPreference = findPreference("max_quality_preference")
            maxPreference.setOnPreferenceClickListener {
                startActivity(Intent(activity, PLVMaxSizePreferenceActivity::class.java))
                false
            }

            val qualityPreference = findPreference("quality_preference")
            qualityPreference.setOnPreferenceClickListener {
                startActivity(Intent(activity, PLVQualityPreferenceActivity::class.java))
                false
            }

            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }
}