/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carbon.device;

import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.carbon.device.R;

public class ScreenFragmentActivity extends PreferenceFragment {

    private static final String PREF_ENABLED = "1";
    private static final String TAG = "DeviceSettings_Screen";

    private static final String FILE_TOUCHKEY_DISABLE = "/sys/class/sec/sec_touchkey/force_disable";
    private static final String FILE_TOUCHKEY_BRIGHTNESS = "/sys/class/sec/sec_touchkey/brightness";

    private CABC mCABC;
    private mDNIeScenario mmDNIeScenario;
    private mDNIeMode mmDNIeMode;
    private mDNIeNegative mmDNIeNegative;
    private mDNIeOutdoor mmDNIeOutdoor;
    private PanelGamma mPanelGamma;
    private TouchscreenSensitivity mTouchscreenSensitivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screen_preferences);
        PreferenceScreen prefSet = getPreferenceScreen();

        mCABC = (CABC) findPreference(DeviceSettings.KEY_CABC);
        mCABC.setEnabled(CABC.isSupported());

        mmDNIeScenario = (mDNIeScenario) findPreference(DeviceSettings.KEY_MDNIE_SCENARIO);
        mmDNIeScenario.setEnabled(mDNIeScenario.isSupported());

        mmDNIeMode = (mDNIeMode) findPreference(DeviceSettings.KEY_MDNIE_MODE);
        mmDNIeMode.setEnabled(mDNIeMode.isSupported());

        mmDNIeNegative = (mDNIeNegative) findPreference(DeviceSettings.KEY_MDNIE_NEGATIVE);
        mmDNIeNegative.setEnabled(mDNIeNegative.isSupported());

        mmDNIeOutdoor = (mDNIeOutdoor) findPreference(DeviceSettings.KEY_MDNIE_OUTDOOR);
        mmDNIeOutdoor.setEnabled(mDNIeOutdoor.isSupported());

        mPanelGamma = (PanelGamma) findPreference(DeviceSettings.KEY_PANEL_GAMMA);
        mPanelGamma.setEnabled(mPanelGamma.isSupported());

        mTouchscreenSensitivity = (TouchscreenSensitivity) findPreference(DeviceSettings.KEY_TOUCHSCREEN_SENSITIVITY);
        mTouchscreenSensitivity.setEnabled(mTouchscreenSensitivity.isSupported());

        if (((CheckBoxPreference)prefSet.findPreference(DeviceSettings.KEY_TOUCHKEY_LIGHT)).isChecked()) {
            prefSet.findPreference(DeviceSettings.KEY_TOUCHKEY_TIMEOUT).setEnabled(true);
        } else {
            prefSet.findPreference(DeviceSettings.KEY_TOUCHKEY_TIMEOUT).setEnabled(false);
        }
    }
    
    private static void setButtonBrightnessValue(Context context, String value)
    {
        ContentResolver resolver = context.getContentResolver();
        int curValue = Settings.System.getIntForUser(resolver,
            Settings.System.BUTTON_BRIGHTNESS, -1, UserHandle.USER_CURRENT);
        int newValue = Integer.parseInt(value);
        if (curValue != newValue) {
            Settings.System.putIntForUser(resolver,
                Settings.System.BUTTON_BRIGHTNESS, newValue, UserHandle.USER_CURRENT);
        }
        Utils.writeValue(FILE_TOUCHKEY_BRIGHTNESS, value);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String key = preference.getKey();

        Log.w(TAG, "key: " + key);

        if (key.compareTo(DeviceSettings.KEY_TOUCHKEY_LIGHT) == 0) {
            if (((CheckBoxPreference)preference).isChecked()) {
                Utils.writeValue(FILE_TOUCHKEY_DISABLE, "0");
                setButtonBrightnessValue(getActivity(), "1");
                preferenceScreen.findPreference(DeviceSettings.KEY_TOUCHKEY_TIMEOUT).setEnabled(true);
            } else {
                Utils.writeValue(FILE_TOUCHKEY_DISABLE, "1");
                setButtonBrightnessValue(getActivity(), "2");
                preferenceScreen.findPreference(DeviceSettings.KEY_TOUCHKEY_TIMEOUT).setEnabled(false);
            }
        }

        return true;
    }

    public static boolean isSupported(String FILE) {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean light = sharedPrefs.getBoolean(DeviceSettings.KEY_TOUCHKEY_LIGHT, true);

        Utils.writeValue(FILE_TOUCHKEY_DISABLE, light ? "0" : "1");
        setButtonBrightnessValue(context, light ? "1" : "2");
    }
}
