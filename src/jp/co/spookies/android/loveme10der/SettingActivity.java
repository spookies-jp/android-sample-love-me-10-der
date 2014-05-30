package jp.co.spookies.android.loveme10der;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * メニュー　設定画面用Activity
 */
public class SettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
    }
}