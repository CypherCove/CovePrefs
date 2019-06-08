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
package com.cyphercove.coveprefs.example;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.cyphercove.coveprefs.CovePreferenceFragmentCompat;
import com.cyphercove.coveprefs.CovePrefs;

/** An example of using CovePrefs with AppCompat Preferences. The preference fragment must extend
 * CovePreferenceFragmentCompat for the CovePref Preferences to show dialogs.
 * <p>
 */
public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {


    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        return CovePrefs.onPreferenceDisplayDialog(caller, pref);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ThemeSwapper.isCustomTheme(this))
            setTheme(R.style.CustomTheme);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragmentCompat())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_swap_theme:
                ThemeSwapper.swapTheme(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragmentCompat extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_overview);
        }
    }
}