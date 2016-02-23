package top.itmp.newhosts;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by hz on 2016/2/23.
 */
public class SettingFragment extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
