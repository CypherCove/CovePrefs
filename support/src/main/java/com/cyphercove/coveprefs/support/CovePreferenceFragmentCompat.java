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
package com.cyphercove.coveprefs.support;

import android.support.v4.app.DialogFragment;
import android.support.v7.preference.*;
import android.support.v7.preference.internal.AbstractMultiSelectListPreference;

/**
 */
public abstract class CovePreferenceFragmentCompat extends PreferenceFragmentCompat {
    private static final String DIALOG_FRAGMENT_TAG =
            "com.example.coveprefs.support.CovePreferenceFragmentCompat.DIALOG";

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!(preference instanceof BaseDialogPreference)) {
            super.onDisplayPreferenceDialog(preference);
            return;
        }

        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                    .onPreferenceDisplayDialog(this, preference);
        }

        if (handled) {
            return;
        }

        // check if dialog is already showing
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final DialogFragment f = BaseDialogPreference.DialogFragment.newInstance(preference.getKey());
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
}
