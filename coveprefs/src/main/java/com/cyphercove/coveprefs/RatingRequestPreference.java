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

import androidx.preference.TwoStatePreference;

import com.cyphercove.coveprefs.utils.CovePrefsUtils;

/**
 * A TwoStatePreference that does not show a widget, but rather is automatically made invisible when
 * it is set to true. It becomes true and invisible when clicked. It is intended for prompting the
 * user to rate the application, so a store link Intent should be set.
 */
@SuppressWarnings("WeakerAccess")
public class RatingRequestPreference extends TwoStatePreference {

    public RatingRequestPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RatingRequestPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public RatingRequestPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    protected void init (Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_BannerLinkPreference);
        String uri = a.getString(R.styleable.CovePrefs_RatingRequestPreference_coveprefs_uri);
        String backupUri = a.getString(R.styleable.CovePrefs_RatingRequestPreference_coveprefs_backupUri);
        String uriFormatArg = a.getString(R.styleable.CovePrefs_RatingRequestPreference_coveprefs_uriFormatArg);
        a.recycle();

        Intent intent = CovePrefsUtils.resolveIntent(context, uriFormatArg, uri, backupUri);
        if (intent != null){
            setIntent(intent);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        setVisible(!checked);
    }
}
