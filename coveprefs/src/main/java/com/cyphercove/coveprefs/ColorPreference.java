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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.cyphercove.coveprefs.utils.ColorCache;
import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.widgets.ColorPicker;
import com.cyphercove.coveprefs.widgets.ColorSwatch;

/**
 * A DialogPreference that allows the user to choose a color. The color will be in ARGB 32 bit format, with alpha equal
 * to 255. The default styling is intended for use with no dialog title or icon.
 * <p>
 * {@code android:defaultValue} may be specified as a decimal integer, hexadecimal integer (preceded by {@code #} or
 * {@code 0x}), or color resource ({@code @color/myColor}).
 * <p>
 * The preference XML may specify which widgets appear in the dialog. By default, all are used. Example, showing all
 * available options:
 * <p>
 * <code>app:coveprefs_colorPickerWidgets="hsv|hex|recent"</code>
 */
@SuppressWarnings("WeakerAccess")
public class ColorPreference extends BaseDialogPreference<Integer> implements ColorPicker.OnColorChangedListener{

    public static final int WIDGETS_DEFAULT =
            ColorPicker.WIDGET_HSV_PICKER |
            ColorPicker.WIDGET_HEX_TEXT_EDIT |
            ColorPicker.WIDGET_RECENTLY_PICKED;

    private ColorPicker colorPicker;
    private ColorSwatch colorWidget;
    private int widgets;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.coveprefs_color_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_color_preference_widget);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_ColorPreference);
        widgets = a.getInt(R.styleable.CovePrefs_ColorPreference_coveprefs_colorPickerWidgets, WIDGETS_DEFAULT);
        a.recycle();

        hideDialogTitleIfNoneSpecified();
        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    @Override
    protected @NonNull
    Integer getBackupDefaultValue() {
        return Color.MAGENTA;
    }

    @Override
    public Class<Integer> getDataType() {
        return Integer.class;
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
    protected void onDialogViewCreated(View view) {
        colorPicker = view.findViewById(R.id.coveprefs_colorPicker);
        colorPicker.setOnColorChangedListener(this);
        colorPicker.setWidgets(widgets);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        colorPicker.setColor(getValueForBindingDialog());
    }

    @Override
    protected void onPreferenceViewCreated (AbsViewHolder view){
        colorWidget = (ColorSwatch)view.findViewById(R.id.coveprefs_widget);
        colorWidget.setColor(getValueForBindingPreferenceView());
    }

    @Override
    protected void onValueChangedAndCommitted() {
        if (colorWidget != null)
            colorWidget.setColorAnimated(getValueForBindingPreferenceView());
        ColorCache.submitColor(getContext(), getValueForBindingPreferenceView());
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
        try {
            return a.getInteger(index, getBackupDefaultValue());
        } catch (UnsupportedOperationException e){
            String value = a.getString(index);
            if (value == null)
                return getBackupDefaultValue(); // shouldn't happen
            if (value.contains("color/")){
                Resources res = getContext().getResources();
                int colorId = res.getIdentifier(value, "color", getContext().getPackageName());
                if (colorId == 0)
                    throw new RuntimeException("Could not find color resource with name: " + value);
                if (Build.VERSION.SDK_INT < 23)
                    return res.getColor(colorId);
                else
                    return res.getColor(colorId, null);
            }
            try {
                Long longValue = Long.decode(value);
                return 0xff000000 | (int) (long) longValue;
            } catch (NumberFormatException e2) {
                return getBackupDefaultValue(); // shouldn't happen
            }
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        onValueModifiedInDialog(newColor);
    }

}
