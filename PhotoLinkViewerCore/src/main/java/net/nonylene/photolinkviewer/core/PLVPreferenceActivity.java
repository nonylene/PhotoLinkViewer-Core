package net.nonylene.photolinkviewer.core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.nonylene.photolinkviewer.core.fragment.PreferenceSummaryFragment;
import net.nonylene.photolinkviewer.core.tool.Initialize;

public class PLVPreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean("initialized39", false)) {
            Initialize.INSTANCE.initialize39(this);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceSummaryFragment {

        private static final int ABOUT_FRAGMENT = 100;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.settings);
            Preference preference = findPreference("about_app_preference");
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AboutDialogFragment dialogFragment = new AboutDialogFragment();
                    dialogFragment.setTargetFragment(SettingsFragment.this, ABOUT_FRAGMENT);
                    dialogFragment.show(getFragmentManager(), "about");
                    return false;
                }
            });

            Preference maxPreference = findPreference("max_quality_preference");
            maxPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PLVMaxSizePreferenceActivity.class));
                    return false;
                }
            });

            Preference qualityPreference = findPreference("quality_preference");
            qualityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PLVQualityPreferenceActivity.class));
                    return false;
                }
            });

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        // license etc
        public static class AboutDialogFragment extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    PackageManager manager = getActivity().getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                    String version = info.versionName;
                    View view = View.inflate(getActivity(), R.layout.about_app, null);
                    TextView textView = (TextView) view.findViewById(R.id.about_version);
                    textView.append(version);
                    builder.setView(view)
                            .setTitle(getString(R.string.about_app_dialogtitle))
                            .setPositiveButton(getString(android.R.string.ok), null);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("error", e.toString());
                    Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_LONG).show();
                }
                return builder.create();
            }
        }
    }
}