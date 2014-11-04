package net.nonylene.photolinkviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            Preference preference = findPreference("update_check");
            preference.setOnPreferenceClickListener(new PreferenceClickListener());
        }

        class PreferenceClickListener implements Preference.OnPreferenceClickListener {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                AsyncJSONExecute hoge = new AsyncJSONExecute();
                hoge.Start(getString(R.string.update_url));
                return true;
            }
        }

        public class AsyncJSONExecute implements LoaderManager.LoaderCallbacks<JSONObject> {
            //get json from url

            public void Start(String url) {
                Bundle bundle = new Bundle();
                bundle.putString("url", url);
                getLoaderManager().restartLoader(0, bundle, this);
            }

            @Override
            public Loader<JSONObject> onCreateLoader(int id, Bundle bundle) {
                try {
                    String c = bundle.getString("url");
                    URL url = new URL(c);
                    return new AsyncJSON(getActivity().getApplicationContext(), url);
                } catch (IOException e) {
                    Log.e("JSONLoaderError", e.toString());
                    return null;
                }
            }

            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject json) {
                try {
                    Log.v("json", json.toString(2));
                    // get current version
                    PackageManager packageManager = getActivity().getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(),0);
                    Integer current_ver = packageInfo.versionCode;
                    // get latest version
                    final Integer version = json.getInt("latest_version");
                    if (version > current_ver) {
                        // show update dialog
                        String description = json.getString("update_description");
                        String file_url = json.getString("url");
                        final Bundle hoge = new Bundle();
                        hoge.putString("latest_version", version.toString());
                        hoge.putString("current_version", current_ver.toString());
                        hoge.putString("description", description);
                        hoge.putString("file_url", file_url);
                        // use handler to modify gui
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                DialogFragment dialogFragment = new UpdateDialogFragment();
                                dialogFragment.setArguments(hoge);
                                dialogFragment.show(getFragmentManager(), "Update");
                            }
                        });
                    }else{
                        // use handler to modify gui
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),getString(R.string.settings_update_ok) + version.toString(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }
            }

            @Override
            public void onLoaderReset(Loader<JSONObject> loader) {

            }
        }

        public static class UpdateDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final Bundle bundle = getArguments();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.settings_update_exist))
                        .setMessage(getString(R.string.settings_current_ver) + bundle.getString("current_version") +
                                getString(R.string.settings_latest_ver) + bundle.getString("latest_version") +
                                getString(R.string.settings_description) + bundle.getString("description"))
                        .setNeutralButton(getString(R.string.settings_update_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // jump to url
                                Uri uri = Uri.parse(bundle.getString("file_url"));
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.settings_update_ng), null);
                return builder.create();
            }
        }

    }
}
