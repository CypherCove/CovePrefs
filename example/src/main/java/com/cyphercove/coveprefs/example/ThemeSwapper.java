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

import android.app.Activity;
import android.content.Intent;

import androidx.preference.PreferenceManager;

/**
 */
public class ThemeSwapper {

    private static final String THEME_KEY = "com.cyphercove.coveprefs.example.THEME";

    public static boolean isCustomTheme (Activity activity){
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(THEME_KEY, false);
    }

    public static void swapTheme (Activity activity){
        boolean isCustomTheme = isCustomTheme(activity);
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(THEME_KEY, !isCustomTheme).apply();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.finish();
    }

}
