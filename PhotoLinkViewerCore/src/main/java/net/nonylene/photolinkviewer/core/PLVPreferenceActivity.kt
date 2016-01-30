package net.nonylene.photolinkviewer.core

import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

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
            val preference = findPreference("about_app_preference")
            preference.setOnPreferenceClickListener {
                val dialogFragment = AboutDialogFragment()
                dialogFragment.setTargetFragment(this@SettingsFragment, ABOUT_FRAGMENT)
                dialogFragment.show(fragmentManager, "about")
                false
            }

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

        // license etc
        class AboutDialogFragment : DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val builder = AlertDialog.Builder(activity)
                try {
                    val manager = activity.packageManager
                    val info = manager.getPackageInfo(activity.packageName, 0)
                    val version = info.versionName
                    val view = View.inflate(activity, R.layout.about_app, null)
                    val textView = view.findViewById(R.id.about_version) as TextView
                    textView.append(version)
                    builder.setView(view).setTitle(getString(R.string.about_app_dialogtitle)).setPositiveButton(getString(android.R.string.ok), null)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e("error", e.toString())
                    Toast.makeText(activity, "Error occured", Toast.LENGTH_LONG).show()
                }

                return builder.create()
            }
        }

        companion object {
            private val ABOUT_FRAGMENT = 100
        }
    }

    // todo: add prefix to preference key / and write convert code
}