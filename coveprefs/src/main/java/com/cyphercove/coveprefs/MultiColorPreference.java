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

import androidx.annotation.NonNull;

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
@SuppressWarnings("WeakerAccess")
public class MultiColorPreference extends BaseDialogPreference<String> implements
        MultiColorPicker.OnMultiColorChangedListener, MultiColorPicker.OnActiveIndexChangedListener{

    private static final int WIDGETS_DEFAULT =
            ColorPicker.WIDGET_HSV_PICKER |
            ColorPicker.WIDGET_HEX_TEXT_EDIT |
            ColorPicker.WIDGET_RECENTLY_PICKED;

    private MultiColorPicker colorPicker;
    private MultiColorSwatch colorWidget;
    private int widgets;
    private final @NonNull MultiColor.Definition definition;
    private int currentlySelectedColorIndex;

    /**
     * Private constructor to enable optionally programmatically set MultiColor.Definition. If
     * the definition is non-null, it is assumed that attrs is null.
     * @param context
     * @param attrs
     * @param definition If non-null, overrides anything set by attributes.
     */
    private MultiColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, MultiColor.Definition definition) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setDialogLayoutResource(R.layout.coveprefs_multicolor_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_multicolor_preference_widget);

        if (definition != null)
            this.definition = definition;
        else {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_ColorPreference);
            widgets = a.getInt(R.styleable.CovePrefs_ColorPreference_coveprefs_colorPickerWidgets, WIDGETS_DEFAULT);
            int definitionArrayId = a.getResourceId(R.styleable.CovePrefs_ColorPreference_coveprefs_multiColorDefinition, 0);
            int disabledLabel = a.getResourceId(R.styleable.CovePrefs_ColorPreference_coveprefs_multiColorDisabledLabel, 0);
            a.recycle();

            if (definitionArrayId == 0)
                this.definition = MultiColor.Definition.DEFAULT;
            else
                this.definition = new MultiColor.Definition(context, definitionArrayId, disabledLabel);
        }

        hideDialogTitleIfNoneSpecified();
        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    public MultiColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr, defStyleRes, null);
    }

    public MultiColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultiColorPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
        // If defStyleAttr default is ever provided, also need to put it in the last constructor
    }

    public MultiColorPreference(Context context) {
        this(context, (AttributeSet)null);
    }

    public MultiColorPreference(Context context, MultiColor.Definition definition) {
        this(context, null, androidx.preference.R.attr.preferenceStyle, 0, definition);
    }

    @ColorPicker.Widgets
    public int getWidgets() {
        return widgets;
    }

    /** Set the widgets to be shown in the ColorPicker. This should be a combination of flags
     * <ul>
     *     <li>{@link ColorPicker#WIDGET_HSV_PICKER}</li>
     *     <li>{@link ColorPicker#WIDGET_HEX_TEXT_EDIT}</li>
     *     <li>{@link ColorPicker#WIDGET_RECENTLY_PICKED}</li>
     * </ul>
     *
     * @param widgets
     */
    public void setWidgets(@ColorPicker.Widgets int widgets) {
        if (widgets != widgets) {
            this.widgets = widgets;
            notifyChanged();
        }
    }

    @NonNull
    public MultiColor.Definition getMultiColorDefinition() {
        return definition;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SingleValueSavedState<Integer> myState = SingleValueSavedState.create(superState, Integer.class);
        myState.setValue(currentlySelectedColorIndex);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null) {
            super.onRestoreInstanceState(null);
            return;
        }

        @SuppressWarnings("unchecked")
        SingleValueSavedState<Integer> myState = (SingleValueSavedState<Integer>) state;
        currentlySelectedColorIndex = myState.getValue();
        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    protected @NonNull
    String getBackupDefaultValue() {
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
        colorPicker = view.findViewById(R.id.coveprefs_colorPicker);
        colorPicker.setOnMultiColorChangedListener(this);
        colorPicker.setOnActiveIndexChangedListener(this);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        MultiColor multiColor = definition.getValue(getValueForBindingDialog());
        colorPicker.setMultiColorValue(currentlySelectedColorIndex, multiColor);
        colorPicker.setWidgets(widgets);
    }

    @Override
    protected void onPreferenceViewCreated (AbsViewHolder view){
        colorWidget = (MultiColorSwatch)view.findViewById(R.id.coveprefs_widget);
        MultiColor multiColor = definition.getValue(getValueForBindingPreferenceView());
        colorWidget.setColors(multiColor.getValues(), multiColor.getValueCount());
    }

    @Override
    protected void onValueChangedAndCommitted() {
        MultiColor multiColor = definition.getValue(getValueForBindingPreferenceView());
        if (colorWidget != null)
            colorWidget.setColorsAnimated(multiColor.getValues(), multiColor.getValueCount());
        ColorCache.submitColor(getContext(), multiColor.getValues(), multiColor.getValueCount());
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
