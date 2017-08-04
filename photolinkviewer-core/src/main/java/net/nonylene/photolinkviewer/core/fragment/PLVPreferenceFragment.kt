package net.nonylene.photolinkviewer.core.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
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

        // on notes
        findPreference("filename_note").setOnPreferenceClickListener{
            NoteDialogFragment().apply {
                show(this@PLVPreferenceFragment.fragmentManager, "batch")
            }
            false
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    class NoteDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(activity)
                .setTitle(getString(R.string.plv_core_notes_filename_dialog_title))
                .setMessage(getString(R.string.plv_core_notes_about_filename))
                .setPositiveButton(getString(android.R.string.ok), null)
                .create()
    }
}
