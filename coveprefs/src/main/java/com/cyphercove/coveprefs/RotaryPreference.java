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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.TypedArrayUtils;

import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.widgets.RotaryPicker;
import com.cyphercove.coveprefs.widgets.RotaryPreferenceWidget;

/**
 * A DialogPreference that allows the user to choose an integer angle from 0 to 359.  The default styling is intended
 * for use with no dialog title or icon.
 */
@SuppressWarnings("WeakerAccess")
public class RotaryPreference extends BaseDialogPreference<Integer> implements RotaryPicker.OnAngleChangedListener{
    private RotaryPicker rotaryPicker;
    private RotaryPreferenceWidget rotaryWidget;

    public RotaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_RotaryPreference);
        if (a.getBoolean(R.styleable.CovePrefs_RotaryPreference_coveprefs_useSimpleSummaryProvider, false)){
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }
        a.recycle();

        setDialogLayoutResource(R.layout.coveprefs_rotary_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_rotary_preference_widget);

        hideDialogTitleIfNoneSpecified();
        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    public RotaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RotaryPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public RotaryPreference(Context context) {
        this(context, null);
    }

    @Override
    protected @NonNull
    Integer getBackupDefaultValue() {
        return 0;
    }

    @Override
    public Class<Integer> getDataType() {
        return Integer.class;
    }

    @Override
    protected void onDialogViewCreated(View view) {
        rotaryPicker = view.findViewById(R.id.coveprefs_rotaryPicker);
        rotaryPicker.setOnAngleChangedListener(this);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        rotaryPicker.setAngle(getValueForBindingDialog());
    }

    @Override
    protected void onPreferenceViewCreated (AbsViewHolder view){
        rotaryWidget = (RotaryPreferenceWidget) view.findViewById(R.id.coveprefs_widget);
        rotaryWidget.setValue(getValueForBindingPreferenceView());
    }

    @Override
    protected void onValueChangedAndCommitted() {
        if (rotaryWidget != null)
            rotaryWidget.setValueAnimated(getValueForBindingPreferenceView());
        if (getSummaryProvider() != null)
            notifyChanged(); // This skips the animation so only notify if necessary.
    }

    /**
     * Updates the Preference to show the current value of the underlying shared preference. This is
     * useful if the preference value has been changed by something other than this Preference.
     */
    public void loadPersistedValue() {
        applyPersistedValue();
        onValueChangedAndCommitted();
    }

    @Override
    protected void persistValue(Integer value) {
        persistInt(value);
    }

    @Override
    protected Integer getPersistedValue(Integer defaultReturnValue) {
        return getPersistedInt(defaultReturnValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, getBackupDefaultValue());
    }

    @Override
    public void onAngleChanged(int newAngle) {
        onValueModifiedInDialog(newAngle);
    }

    public static final class SimpleSummaryProvider implements SummaryProvider<RotaryPreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

        /**
         * Retrieve a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation.
         *
         * @return a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation
         */
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(RotaryPreference preference) {
            return preference.getValue() + "°";
        }
    }
}
