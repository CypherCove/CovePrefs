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
package com.cyphercove.coveprefs;

import androidx.preference.*;

/**
 * Extend this class to support CovePrefs with dialogs. If a different Fragment superclass is needed,
 * see {@link CovePrefs#onPreferenceDisplayDialog(PreferenceFragmentCompat, Preference)} .
 */
public abstract class CovePreferenceFragmentCompat extends PreferenceFragmentCompat {
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (CovePrefs.onPreferenceDisplayDialog(this, preference))
            return;
        super.onDisplayPreferenceDialog(preference);
    }
}
