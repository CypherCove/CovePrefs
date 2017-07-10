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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.widgets.RotaryPicker;
import com.cyphercove.coveprefs.widgets.RotaryPreferenceWidget;

/**
 * A DialogPreference that allows the user to choose an integer angle from 0 to 359.  The default styling is intended
 * for use with no dialog title or icon.
 */
public class RotaryPreference extends BaseDialogPreference<Integer> implements RotaryPicker.OnAngleChangedListener{
    private RotaryPicker rotaryPicker;
    private RotaryPreferenceWidget rotaryWidget;

    public RotaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.coveprefs_rotary_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_rotary_preference_widget);

        hideDialogTitleIfNoneSpecified();
        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    @Override
    protected Integer getDefaultValue() {
        return 0;
    }

    @Override
    public Class<Integer> getDataType() {
        return Integer.class;
    }

    @Override
    protected void onDialogViewCreated(View view) {
        rotaryPicker = (RotaryPicker) view.findViewById(R.id.coveprefs_rotaryPicker);
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
        rotaryWidget.setValueAnimated(getValueForBindingPreferenceView());
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
        return a.getInteger(index, getDefaultValue());
    }

    @Override
    public void onAngleChanged(int newAngle) {
        onValueModifedInDialog(newAngle);
    }

    public CharSequence getSummarizedValue (){
        return getValue() + "°";
    }

}
