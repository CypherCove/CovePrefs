/*
 * Copyright (C) 2017 Cypher Cove, LLC
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
package com.cyphercove.coveprefs.utils;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import com.cyphercove.coveprefs.BaseDialogPreference;
import com.cyphercove.coveprefs.ColorPreference;
import com.cyphercove.coveprefs.RotaryPreference;

import java.util.Locale;

/**
 */
public class SummaryUpdater {

    public static void autoUpdateSummary (Preference preference, boolean formatted) {
        autoUpdateSummary(preference, formatted, PreferenceManager.getDefaultSharedPreferences(preference.getContext()));
    }

    /** Sets the given Preference's OnPreferenceChangeListener to automatically update its summary to show its selected
     * value. This only applies to ListPreference.
     * @param preference
     * @param formatted If true and the preference's current summary exists, the current summary will be treated as a
     *                  format String, into which the summary's value will be placed;
     * @param sharedPreferences
     */
    public static void autoUpdateSummary (Preference preference, boolean formatted, SharedPreferences sharedPreferences){
        Preference.OnPreferenceChangeListener listener = null;
        if (formatted){
            CharSequence originalSummary = preference.getSummary();
            if (originalSummary != null && !originalSummary.equals(""))
                listener = new SummaryToFormattedValueListener(originalSummary);
        }
        if (listener == null)
            listener = summaryToValueListener;
        preference.setOnPreferenceChangeListener(listener);
        Object value = "";
        if (preference instanceof ListPreference)
            value = sharedPreferences.getString(preference.getKey(), "");
        if (preference instanceof BaseDialogPreference){
            Class dataType = ((BaseDialogPreference)preference).getDataType();
            if (dataType == String.class)
                value = sharedPreferences.getString(preference.getKey(), "");
            else if (dataType == Integer.class)
                value = sharedPreferences.getInt(preference.getKey(), 0);
            else if (dataType == Float.class)
                value = sharedPreferences.getFloat(preference.getKey(), 0);
        }
        listener.onPreferenceChange(preference, value);
    }

    private static CharSequence getSummary (Preference preference, Object value){
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            return index >= 0 ? listPreference.getEntries()[index] : null;
        } else if (preference instanceof RotaryPreference){
            return ((RotaryPreference) preference).getSummarizedValue();
        } else {
            return stringValue;
        }
    }

    private static Preference.OnPreferenceChangeListener summaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (!value.equals(""))
                preference.setSummary(getSummary(preference, value));
            return true;
        }
    };

    private static class SummaryToFormattedValueListener implements Preference.OnPreferenceChangeListener  {
        String originalSummary;

        public SummaryToFormattedValueListener (CharSequence originalSummary){
            this.originalSummary = originalSummary.toString();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Locale locale = preference.getContext().getResources().getConfiguration().locale;
            preference.setSummary(String.format(locale, originalSummary, getSummary(preference, value)));
            return true;
        }
    };
}
