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
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import com.cyphercove.coveprefs.state.SingleValueSavedState;
import com.cyphercove.coveprefs.utils.ColorCache;
import com.cyphercove.coveprefs.utils.MultiColor;
import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.widgets.ColorPicker;
import com.cyphercove.coveprefs.widgets.MultiColorPicker;
import com.cyphercove.coveprefs.widgets.MultiColorSwatch;

/**
 * A DialogPreference that allows the user to choose a color, or a set of colors, or a disabled color. The preference is
 * stored as a String, which packs the value(s) of the colors. The colors will be in ARGB 32 bit format, with alpha equal
 * to 255. The default styling is intended for use with no dialog title or icon.
 * <p>
 * The preference XML may specify which widgets appear in the dialog. By default, all are used. Example, showing all
 * available options:
 * <p>
 * <code>app:coveprefs_colorPickerWidgets="hsv|hex|recent"</code>
 */
public class MultiColorPreference extends BaseDialogPreference<String> implements
        MultiColorPicker.OnMultiColorChangedListener, MultiColorPicker.OnActiveIndexChangedListener{

    public static final int WIDGETS_DEFAULT =
            ColorPicker.WIDGET_HSV_PICKER |
            ColorPicker.WIDGET_HEX_TEXT_EDIT |
            ColorPicker.WIDGET_RECENTLY_PICKED;

    private MultiColorPicker colorPicker;
    private MultiColorSwatch colorWidget;
    private int widgets;
    private MultiColor.Definition definition;
    private int currentlySelectedColorIndex;

    public MultiColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.coveprefs_multicolor_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_multicolor_preference_widget);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_ColorPreference);
        widgets = a.getInt(R.styleable.CovePrefs_ColorPreference_coveprefs_colorPickerWidgets, WIDGETS_DEFAULT);
        int definitionArrayId = a.getResourceId(R.styleable.CovePrefs_ColorPreference_coveprefs_multiColorDefinition, 0);
        String disabledLabel = a.getString(R.styleable.CovePrefs_ColorPreference_coveprefs_multiColorDisabledLabel);
        a.recycle();

        if (definitionArrayId == 0)
            definition = MultiColor.Definition.DEFAULT;
        else
            definition = new MultiColor.Definition(context, definitionArrayId, disabledLabel);

        hideDialogTitleIfNoneSpecified();
        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SingleValueSavedState myState = SingleValueSavedState.create(superState, Integer.class);
        myState.setValue(currentlySelectedColorIndex);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null) {
            super.onRestoreInstanceState(state);
            return;
        }

        SingleValueSavedState myState = (SingleValueSavedState) state;
        currentlySelectedColorIndex = (Integer)myState.getValue();
        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    protected String getBackupDefaultValue() {
        return "0 FF000000";
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    protected void persistValue(String value) {
        persistString(value);
    }

    @Override
    protected String getPersistedValue(String defaultReturnValue) {
        return getPersistedString(defaultReturnValue);
    }

    @Override
    protected void onDialogViewCreated(View view) {
        colorPicker = (MultiColorPicker) view.findViewById(R.id.coveprefs_colorPicker);
        colorPicker.setOnMultiColorChangedListener(this);
        colorPicker.setOnActiveIndexChangedListener(this);
        colorPicker.setWidgets(widgets);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        MultiColor multiColor = definition.getTemporaryValue(getValueForBindingDialog());
        colorPicker.setMultiColorValue(currentlySelectedColorIndex, multiColor);
    }

    @Override
    protected void onPreferenceViewCreated (AbsViewHolder view){
        colorWidget = (MultiColorSwatch)view.findViewById(R.id.coveprefs_widget);
        MultiColor multiColor = definition.getTemporaryValue(getValueForBindingPreferenceView());
        colorWidget.setColors(multiColor.getValues(), multiColor.getValueCount());
    }

    @Override
    protected void onValueChangedAndCommitted() {
        MultiColor multiColor = definition.getTemporaryValue(getValueForBindingPreferenceView());
        colorWidget.setColorsAnimated(multiColor.getValues(), multiColor.getValueCount());
        ColorCache.submitColor(getContext(), multiColor.getValues(), multiColor.getValueCount());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String value = a.getString(index);
        return value == null ? getBackupDefaultValue() : value;
    }

    @Override
    public void onColorChanged(MultiColor multiColor) {
        onValueModifiedInDialog(multiColor.toPreferenceValue());
    }

    @Override
    public void onActiveIndexChanged(int index) {
        currentlySelectedColorIndex = index;
    }
}
