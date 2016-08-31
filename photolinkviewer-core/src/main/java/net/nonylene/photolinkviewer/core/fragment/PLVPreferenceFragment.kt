package net.nonylene.photolinkviewer.core.fragment

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.nonylene.photolinkviewer.core.PLVMaxSizePreferenceActivity
import net.nonylene.photolinkviewer.core.PLVOptionButtonPreferenceActivity
import net.nonylene.photolinkviewer.core.PLVQualityPreferenceActivity
import net.nonylene.photolinkviewer.core.R
import net.nonylene.photolinkviewer.core.tool.Initialize
import net.nonylene.photolinkviewer.core.tool.isInitialized47

class PLVPreferenceFragment: PreferenceSummaryFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PreferenceManager.getDefaultSharedPreferences(activity).isInitialized47()) {
            Initialize.initialize47(activity)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        addPreferencesFromResource(R.xml.plv_core_preference)

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

        val buttonPreference = findPreference("option_button_preference")
        buttonPreference.setOnPreferenceClickListener {
            startActivity(Intent(activity, PLVOptionButtonPreferenceActivity::class.java))
            false
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
