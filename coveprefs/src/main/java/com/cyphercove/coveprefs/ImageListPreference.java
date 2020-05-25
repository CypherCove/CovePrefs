/*
 * Copyright (C) 2007 The Android Open Source Project
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

/*
 * This class contains modified derivative code from the ASOP android.preference.ListPreference class.
 */
package com.cyphercove.coveprefs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.ImageViewCompat;

import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.utils.ViewUtils;
import com.cyphercove.coveprefs.widgets.PreferenceImageView;

/**
 * Like a ListPreference, but the user selects from an array of Drawables instead of Strings. The selected image is shown in
 * the widget area of the preference.
 */
@SuppressWarnings("WeakerAccess")
public class ImageListPreference extends BaseDialogPreference<String>{

    private int[] entryIds;
    private CharSequence[] entryValues;
    private PreferenceImageView selectedImageWidget;
    private ColorStateList imageTintColor;
    private PorterDuff.Mode imageTintMode;
    private int buttonElevation, rowHeight, columnWidth;
    private boolean smallWidget;

    public ImageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.coveprefs_image_list_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_image_list_preference_widget);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_ImageListPreference);
        int entriesId = a.getResourceId(R.styleable.CovePrefs_ImageListPreference_entries, 0);
        entryValues = a.getTextArray(R.styleable.CovePrefs_ImageListPreference_entryValues);
        imageTintColor = a.getColorStateList(R.styleable.CovePrefs_ImageListPreference_tint);
        if (a.hasValue(R.styleable.CovePrefs_ImageListPreference_coveprefs_tintMode)){
            imageTintMode = ViewUtils.parseTintMode(a.getInt(R.styleable.CovePrefs_ImageListPreference_coveprefs_tintMode, -1),
                    PorterDuff.Mode.SRC_IN);
        }
        columnWidth = a.getDimensionPixelSize(R.styleable.CovePrefs_ImageListPreference_coveprefs_dialogColumnWidth, 0);
        rowHeight = a.getDimensionPixelSize(R.styleable.CovePrefs_ImageListPreference_coveprefs_dialogRowHeight, 0);
        smallWidget = a.getBoolean(R.styleable.CovePrefs_ImageListPreference_coveprefs_smallWidget, false);
        a.recycle();

        final Resources res = context.getResources();
        if (columnWidth == 0)
            columnWidth = res.getDimensionPixelSize(R.dimen.coveprefs_image_list_default_column_width);
        if (rowHeight == 0)
            rowHeight = res.getDimensionPixelSize(R.dimen.coveprefs_image_list_default_row_height);
        buttonElevation = res.getDimensionPixelSize(R.dimen.coveprefs_image_list_button_elevation);

        if (entriesId != 0)
            setEntries(entriesId);

        setPositiveButtonText(null); // Like a ListPreference, the dialog is automatically closed when a selection is made
        setInternalButtonBar();
    }

    /** Sets the resource Ids of the Drawables that the user will pick from. This will be shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in {@link #setEntryValues(CharSequence[])}.
     * @param entryIds  The IDs of the drawables.*/
    public void setEntryDrawableIds(int[] entryIds) {
        this.entryIds = entryIds;
    }

    /**
     * @see #setEntryDrawableIds(int[])
     * @param entriesResId The entries array as a resource Id.
     */
    public void setEntries(@ArrayRes int entriesResId) {
        TypedArray typedArray = getContext().getResources().obtainTypedArray(entriesResId);
        int[] ids = new int[typedArray.length()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = typedArray.getResourceId(i, 0);
        }
        typedArray.recycle();
        setEntryDrawableIds(ids);
    }

    /**
     * The list of entries (Drawable resource IDs) to be shown in the list in subsequent dialogs.
     *
     * @return The list as an array.
     */
    public int[] getEntryDrawableIds (){
        return entryIds;
    }

    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        this.entryValues = entryValues;
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResId The entry values array as a resource.
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     *
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return entryValues;
    }

    /**
     * Sets the value to the given index from the entry values.
     *
     * @param index The index of the value to set.
     */
    public void setValueIndex(int index) {
        if (entryValues != null) {
            setValue(entryValues[index].toString());
        }
    }

    /**
     * Returns the entry corresponding to the current value.
     *
     * @return The entry corresponding to the current value, or null.
     */
    public int getEntryDrawableResourceId() {
        int index = getValueIndex();
        return index >= 0 && entryIds != null ? entryIds[index] : 0;
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; i--) {
                if (entryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    private int getDrawableForValue (){
        int index = getValueIndex();
        if (index != -1 && entryIds != null){
            return entryIds[index];
        }
        return 0;
    }
    @Override
    protected @NonNull
    String getBackupDefaultValue() {
        return "";
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
        final GridView gridView = view.findViewById(R.id.coveprefs_gridview);
        gridView.setColumnWidth(columnWidth);
        gridView.setAdapter(new ImageButtonAdapter());
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //TODO highlight the button for the currently selected value
    }

    @Override
    protected void onPreferenceViewCreated (AbsViewHolder view){
        selectedImageWidget = (PreferenceImageView)view.findViewById(R.id.coveprefs_widget);
        if (smallWidget){
            ViewGroup.LayoutParams layoutParams = selectedImageWidget.getLayoutParams();
            int size = getContext().getResources().getDimensionPixelSize(R.dimen.coveprefs_preference_widget_small);
            layoutParams.width = layoutParams.height = size;
            selectedImageWidget.setLayoutParams(layoutParams);
        }
        if (imageTintColor != null) {
            ImageViewCompat.setImageTintList(selectedImageWidget, imageTintColor);
            ImageViewCompat.setImageTintMode(selectedImageWidget, imageTintMode);
        }
        int selectedImageId = getDrawableForValue();
        if (selectedImageId != 0){
            selectedImageWidget.setImageResource(selectedImageId);
        }
    }

    @Override
    protected void onValueChangedAndCommitted() {
        if (selectedImageWidget != null) {
            int selectedImageId = getDrawableForValue();
            if (selectedImageId != 0) {
                selectedImageWidget.setImageResource(selectedImageId);
            } else {
                selectedImageWidget.setImageDrawable(null);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String defaultValue = a.getString(index);
        if (defaultValue == null)
            defaultValue = getBackupDefaultValue();
        return defaultValue;
    }

    private class ImageButtonAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return entryIds.length;
        }

        @Override
        public Object getItem(int position) {
            return entryIds[position];
        }

        @Override
        public long getItemId(int position) {
            return 0; // unused
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppCompatImageButton imageButton;
            if (convertView == null){
                imageButton = new AppCompatImageButton(getContext());
                imageButton.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, rowHeight));
                imageButton.setOnClickListener(new ImageButtonClickListener());
                imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (Build.VERSION.SDK_INT >= 21) imageButton.setElevation(buttonElevation);
                if (imageTintColor != null) {
                    ImageViewCompat.setImageTintList(imageButton, imageTintColor);
                    ImageViewCompat.setImageTintMode(imageButton, imageTintMode);
                }
            } else {
                imageButton = (AppCompatImageButton)convertView;
            }
            imageButton.setTag(position);
            imageButton.setImageResource(entryIds[position]);

            return imageButton;
        }
    }

    private class ImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onValueModifiedInDialog(entryValues[(Integer)v.getTag()].toString());
            dismissDialog(true);
        }
    }

}
