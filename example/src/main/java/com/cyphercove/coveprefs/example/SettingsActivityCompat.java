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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.cyphercove.coveprefs.support.CovePreferenceFragmentCompat;

/** An example of using CovePrefs with AppCompat Preferences. The preference fragment must extend
 * CovePreferenceFragmentCompat for the CovePref Preferences to show dialogs.
 * <p>
 */
public class SettingsActivityCompat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ThemeSwapper.isCustomTheme(this))
            setTheme(R.style.CustomTheme);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){ // don't replace existing or screen rotations will kill dialogs
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
            case R.id.action_swap_compat:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragmentCompat extends CovePreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_overview_compat);
        }
    }
}