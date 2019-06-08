/*
 * Copyright (C) 2019 Cypher Cove, LLC
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
package com.cyphercove.coveprefs;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public final class CovePrefs {

    /**
     * Starts the dialog fragments for CovePrefs' included preferences. The activity (not the fragment) hosting the
     * preferences must implement
     * {@link androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback} and
     * call this method.
     * @param caller Passed in from {@link androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback#onPreferenceDisplayDialog(PreferenceFragmentCompat, Preference)}.
     * @param pref Passed in from {@link androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback#onPreferenceDisplayDialog(PreferenceFragmentCompat, Preference)}.
     * @return
     */
    public static boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, Preference pref){
        if (!(pref instanceof BaseDialogPreference))
            return false;

        final DialogFragment f = BaseDialogPreference.DialogFragment.newInstance(pref.getKey());
        f.setTargetFragment(caller, 0);
        f.show(caller.getFragmentManager(), ((BaseDialogPreference)pref).getDialogFragmentTag());
        return true;
    }
}
