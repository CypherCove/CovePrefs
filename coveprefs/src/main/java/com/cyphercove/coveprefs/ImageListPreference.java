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
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.utils.CovePrefsUtils;
import com.cyphercove.coveprefs.widgets.PreferenceImageView;

import java.util.Arrays;

/**
 * Like a ListPreference, but the user selects from an array of Drawables instead of Strings. The selected image is shown in
 * the widget area of the preference.
 */
@SuppressWarnings("WeakerAccess")
public class ImageListPreference extends BaseDialogPreference<String>{

    private int[] entryIds;
    private CharSequence[] entryValues;
    private CharSequence[] entryContentDescriptions;
    private PreferenceImageView selectedImageWidget;
    private ColorStateList imageTintColor;
    private PorterDuff.Mode imageTintMode;
    private int rowHeight, columnWidth;
    private boolean smallWidget;

    public ImageListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setDialogLayoutResource(R.layout.coveprefs_image_list_dialog);
        setWidgetLayoutResource(R.layout.coveprefs_image_list_preference_widget);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_ImageListPreference);
        int entriesId = a.getResourceId(R.styleable.CovePrefs_ImageListPreference_entries, 0);
        entryValues = a.getTextArray(R.styleable.CovePrefs_ImageListPreference_entryValues);
        entryContentDescriptions = a.getTextArray(R.styleable.CovePrefs_ImageListPreference_coveprefs_entryContentDescriptions);
        imageTintColor = a.getColorStateList(R.styleable.CovePrefs_ImageListPreference_tint);
        if (a.hasValue(R.styleable.CovePrefs_ImageListPreference_coveprefs_tintMode)){
            imageTintMode = CovePrefsUtils.parseTintMode(a.getInt(R.styleable.CovePrefs_ImageListPreference_coveprefs_tintMode, -1),
                    PorterDuff.Mode.SRC_IN);
        }
        columnWidth = a.getDimensionPixelSize(R.styleable.CovePrefs_ImageListPreference_coveprefs_dialogColumnWidth, 0);
        rowHeight = a.getDimensionPixelSize(R.styleable.CovePrefs_ImageListPreference_coveprefs_dialogRowHeight, 0);
        smallWidget = a.getBoolean(R.styleable.CovePrefs_ImageListPreference_coveprefs_smallWidget, false);
        a.recycle();

        final Resources res = context.getResources();
        if (columnWidth == 0)
            columnWidth = res.getDimensionPixelSize(R.dimen.coveprefs_image_list_default_column_width);

        if (entriesId != 0)
            setEntries(entriesId);

        setPositiveButtonText(null); // Like a ListPreference, the dialog is automatically closed when a selection is made
        setInternalButtonBar();
    }

    public ImageListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public ImageListPreference(Context context) {
        this(context, null);
    }

    /** Sets the resource Ids of the Drawables that the user will pick from. This will be shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in {@link #setEntryValues(CharSequence[])}.
     * @param entryIds  The IDs of the drawables.
     * @deprecated Use {@link #setEntries(int[])}. */
    @Deprecated
    public void setEntryDrawableIds(int[] entryIds) {
        setEntries(entryIds);
    }

    /** Sets the resource Ids of the Drawables that the user will pick from. This will be shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in {@link #setEntryValues(CharSequence[])}.
     * @param entryIds  The IDs of the drawables.*/
    public void setEntries(int[] entryIds) {
        if (!Arrays.equals(this.entryIds, entryIds)) {
            this.entryIds = entryIds;
            notifyChanged();
        }
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
        setEntries(ids);
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
     * Sets the array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        if (!Arrays.equals(this.entryValues, entryValues)) {
            this.entryValues = entryValues;
            notifyChanged();
        }
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResId The entry values array as a resource.
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResIds An array of String resource IDs.
     */
    public void setEntryValues(int[] entryValuesResIds) {
        CharSequence[] values = new CharSequence[entryValuesResIds.length];
        for (int i = 0; i < entryValuesResIds.length; i++) {
            values[i] = getContext().getString(entryValuesResIds[i]);
        }
        setEntryValues(values);
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
     * Returns the entry content descriptions corresponding with the images.
     * @return The array of content descriptions or null if none is set.
     */
    public CharSequence[] getEntryContentDescriptions() {
        return entryContentDescriptions;
    }

    /**
     * Sets the content descriptions corresponding with the image entries.
     *
     * @param entryContentDescriptions The array of content descriptions corresponding with image
     *                                 entries with the same indices, or null to set none.
     */
    public void setEntryContentDescriptions(CharSequence[] entryContentDescriptions) {
        if (!Arrays.equals(this.entryContentDescriptions, entryContentDescriptions)) {
            this.entryContentDescriptions = entryContentDescriptions;
            notifyChanged();
        }
    }

    /**
     * @see #setEntryContentDescriptions(CharSequence[])
     * @param entryContentDescriptionsId The content descriptions array as a resource.
     */
    public void setEntryContentDescriptions(@ArrayRes int entryContentDescriptionsId) {
        setEntryContentDescriptions(getContext().getResources().getTextArray(entryContentDescriptionsId));
    }

    /**
     * Sets the content descriptions corresponding with the image entries.
     * @param entryContentDescriptionIds An array of String resource IDs.
     */
    public void setEntryContentDescriptions(int[] entryContentDescriptionIds) {
        CharSequence[] values = new CharSequence[entryContentDescriptionIds.length];
        for (int i = 0; i < entryContentDescriptionIds.length; i++) {
            values[i] = getContext().getString(entryContentDescriptionIds[i]);
        }
        setEntryContentDescriptions(values);
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
     * @return The entry Drawable resource ID corresponding to the current value, or 0 if it is not
     * retrievable.
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

    private CharSequence getContentDescriptionForValue () {
        int index = getValueIndex();
        if (index != -1 && entryContentDescriptions != null){
            return entryContentDescriptions[index];
        }
        return null;
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
        gridView.setAdapter(new ImageButtonAdapter(getContext()));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        View persistedItemView = view.findViewWithTag((Integer)getValueIndex());
        if (persistedItemView != null){
            persistedItemView.requestFocus(); // TODO this isn't working to highlight selected item
        }
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
            selectedImageWidget.setContentDescription(getContentDescriptionForValue());
        }
    }

    @Override
    protected void onValueChangedAndCommitted() {
        if (selectedImageWidget != null) {
            int selectedImageId = getDrawableForValue();
            if (selectedImageId != 0) {
                selectedImageWidget.setImageResource(selectedImageId);
                selectedImageWidget.setContentDescription(getContentDescriptionForValue());
            } else {
                selectedImageWidget.setImageDrawable(null);
                selectedImageWidget.setContentDescription(null);
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

        LayoutInflater layoutInflater;

        public ImageButtonAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return entryIds == null ? 0 : entryIds.length;
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
            ImageButton imageButton;
            if (convertView == null){
                imageButton = (ImageButton) layoutInflater.inflate(R.layout.coveprefs_image_list_button, parent, false);
                imageButton.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, rowHeight == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : rowHeight));
                imageButton.setOnClickListener(new ImageButtonClickListener());
                imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (imageTintColor != null) {
                    ImageViewCompat.setImageTintList(imageButton, imageTintColor);
                    ImageViewCompat.setImageTintMode(imageButton, imageTintMode);
                }
            } else {
                imageButton = (ImageButton)convertView;
            }
            imageButton.setTag(position);
            imageButton.setImageResource(entryIds[position]);
            imageButton.setContentDescription(entryContentDescriptions == null ? null : entryContentDescriptions[position]);

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
