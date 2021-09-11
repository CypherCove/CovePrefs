/*
 * Copyright (C) 2020 Cypher Cove, LLC
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.Preference;

/**
 * A Boolean-based Preference that does not show a widget, but rather is automatically made invisible
 * when it is set to true. It becomes true and invisible when clicked. It is intended for prompting
 * the user to rate the application, so a store link Intent should be set. The preference value can
 * be manually set back to false to cause this Preference to begin appearing in the layout again.
 * <p>
 * It is a {@link BannerLinkPreference} and {@link LinkPreference}, so it can be decorated and
 * have its Intent set via XML properties.
 */
@SuppressWarnings("WeakerAccess")
public class RatingRequestPreference extends LinkPreference {

    public RatingRequestPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RatingRequestPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RatingRequestPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public RatingRequestPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        super.onClick();
        if (callChangeListener(true)) {
            setClicked();
            Intent intent = getIntent();
            if (intent != null) {
                getContext().startActivity(intent);
            }
        }
    }

    void setClicked() {
        persistBoolean(true);
        setVisible(false);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = false;
        }
        if (getPersistedBoolean((Boolean) defaultValue)) {
            setClicked();
        }
    }
}
