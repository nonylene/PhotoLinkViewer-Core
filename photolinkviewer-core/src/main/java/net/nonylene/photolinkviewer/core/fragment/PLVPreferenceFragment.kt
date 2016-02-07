package net.nonylene.photolinkviewer.core.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.nonylene.photolinkviewer.core.PLVMaxSizePreferenceActivity
import net.nonylene.photolinkviewer.core.PLVQualityPreferenceActivity
import net.nonylene.photolinkviewer.core.R

class PLVPreferenceFragment: PreferenceSummaryFragment() {

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
