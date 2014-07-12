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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.provider.Settings;

import java.io.IOException;

public class TouchkeyTimeout extends ListPreference implements OnPreferenceChangeListener {

    public TouchkeyTimeout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnPreferenceChangeListener(this);
    }

    private static final String FILE_TOUCHKEY_TIMEOUT = "/sys/class/sec/sec_touchkey/timeout";

    public static boolean isSupported() {
        return Utils.fileExists(FILE_TOUCHKEY_TIMEOUT);
    }

    private static void setTimeoutValue(Context context, String value)
    {
        ContentResolver resolver = context.getContentResolver();
        int curValue = Settings.System.getIntForUser(resolver,
            Settings.System.BUTTON_BACKLIGHT_TIMEOUT, -1, UserHandle.USER_CURRENT);
        int newValue = Integer.parseInt(value) * 1000;
        if (curValue != newValue) {
            Settings.System.putIntForUser(resolver,
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT,
                newValue, UserHandle.USER_CURRENT);
        }
        Utils.writeValue(FILE_TOUCHKEY_TIMEOUT, value);
    }

    /**
     * Restore touchscreen sensitivity setting from SharedPreferences. (Write to kernel.)
     * @param context       The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        setTimeoutValue(context, sharedPrefs.getString(DeviceSettings.KEY_TOUCHKEY_TIMEOUT, "3"));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setTimeoutValue(getContext(), (String) newValue);
        return true;
    }

}
