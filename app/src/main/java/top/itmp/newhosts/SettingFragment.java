package top.itmp.newhosts;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import java.io.File;

/**
 * Created by hz on 2016/2/23.
 */
public class SettingFragment extends PreferenceFragment {
    private final int REQUEST_WRITE_STORAGE = 112;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        initSummaries(getPreferenceScreen());
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Need Permission to save file", Toast.LENGTH_SHORT).show();
                    getPreferenceScreen().getEditor().putBoolean("isBackup", false).apply();
                    getPreferenceScreen().findPreference("isBackup").setSummary("false");
                    ((CheckBoxPreference)getPreferenceScreen().findPreference("isBackup")).setChecked(false);
                    return;
                }
            default:
                Toast.makeText(getActivity().getApplicationContext(), "Need Permission to save file", Toast.LENGTH_SHORT).show();
                return;
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            setSummary(pref, true);
        }
    };
    public void initSummaries(PreferenceGroup pg) {
        for (int i = 0; i < pg.getPreferenceCount(); ++i) {
            Preference p = pg.getPreference(i);
            if (p instanceof PreferenceGroup)
                initSummaries((PreferenceGroup) p);
            else
                setSummary(p, false);
            if (p instanceof PreferenceScreen)
                p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return false;
                    }
                });
        }
    }
    public void setSummary(Preference pref, boolean init) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) pref;
            pref.setSummary(editPref.getText());

            if (editPref.getKey().equals("backupFile") && !init) {
               /* requestWritePermissions();
                String backupFile;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    File newhosts = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "NewHosts");
                    if(newhosts.exists() && !newhosts.isDirectory()){
                        newhosts.delete();
                    }
                    newhosts.mkdir();

                    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss");*/
                    String backupFile = Environment.getExternalStorageDirectory().toString() + File.separator + "newhosts" + File.separator + "hosts.txt";
                    editPref.setText(backupFile);
                    pref.setSummary(editPref.getText());

                    //Toast.makeText(getActivity().getApplicationContext(), "SDCard Not Found!", Toast.LENGTH_SHORT).show();

            }
        }

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }

        if (pref instanceof CheckBoxPreference) {
            CheckBoxPreference checkPref = (CheckBoxPreference) pref;
                checkPref.setSummary(checkPref.isChecked() + "");
            if (checkPref.getKey().equals("isBackup") && checkPref.isChecked() && init) {
                requestWritePermissions();
                /*if(!(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                    //PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(checkPref.getKey(), false).apply();
                    checkPref.getEditor().putBoolean(checkPref.getKey(), false).apply();
                    checkPref.setChecked(!checkPref.isChecked());
                }*/
            }
        }
        if(pref instanceof SwitchPreference){
            SwitchPreference switchPreference = (SwitchPreference) pref;
            //switchPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(switchPreference.getKey(), false) + "");
            switchPreference.setSummary(switchPreference.isChecked() + "");
        }

        if(pref.getKey().equals("version")){
            pref.setSummary(versionInfo(getActivity()));
        }
        if(pref.getKey().equals("lastUpdate")){
            try {
                pref.setSummary(RunAsRoot.exec(new String[]{"stat -c \"%z\" /etc/hosts\n"}, true));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void requestWritePermissions() {

        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }
    public String versionInfo(Context c){
        String version = "";
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = pi.versionName + "-" + pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
