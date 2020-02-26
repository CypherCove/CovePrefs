/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * This is a modified derivative version of the v7.preference library's android.support.v7.preference.SeekBarPlusPreference class.
 */

package com.cyphercove.coveprefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.view.LayoutInflaterCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.utils.MultiColor;

/**
 * A {@link SeekBarPreference} with left and right labels next to the SeekBar.
 */
public class SeekBarPlusPreference extends SeekBarPreference {

    SeekBar seekBar;
    CharSequence leftLabel, rightLabel;
    TextView leftLabelView, rightLabelView;

    private static final String TAG = "SeekBarPlusPreference";

    public SeekBarPlusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CovePrefs_SeekBarPlusPreference, defStyleAttr, defStyleRes);
        leftLabel = a.getString(R.styleable.CovePrefs_SeekBarPlusPreference_coveprefs_leftLabel);
        rightLabel = a.getString(R.styleable.CovePrefs_SeekBarPlusPreference_coveprefs_rightLabel);
        setMax(a.getInteger(R.styleable.CovePrefs_SeekBarPlusPreference_max, getMax())); // This is to deal with a resource linking issue
        a.recycle();

    }

    public SeekBarPlusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPlusPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.seekBarPreferenceStyle);
    }

    public SeekBarPlusPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        seekBar = (SeekBar) view.findViewById(androidx.preference.R.id.seekbar);
        if (seekBar == null){
            Log.e(TAG, "SeekBar id is missing!");
            return;
        }

        ViewParent viewParent = seekBar.getParent();
        if (!(viewParent instanceof LinearLayout)){
            Log.e(TAG, "SeekBar parent is not a LinearLayout!");
            return;
        }
        LinearLayout layout = (LinearLayout) viewParent;
        ViewGroup.LayoutParams seekBarLayoutParams = seekBar.getLayoutParams();
        View valueLabel = view.findViewById(androidx.preference.R.id.seekbar_value);
        ViewGroup.LayoutParams valueLabelLayoutParams = null;
        if (valueLabel == null){
            Log.e(TAG, "Value label id is missing!");
        } else if (valueLabel.getParent() == layout){
            valueLabelLayoutParams = valueLabel.getLayoutParams();
        } else {
            valueLabel = null;
        }

        LinearLayout.LayoutParams labelLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 0);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        leftLabelView = (TextView) inflater.inflate(R.layout.coveprefs_seekbar_label, null);
        leftLabelView.setPadding(0, leftLabelView.getPaddingTop(), leftLabelView.getPaddingRight(), leftLabelView.getPaddingBottom());
        rightLabelView = (TextView) inflater.inflate(R.layout.coveprefs_seekbar_label, null);
        rightLabelView.setPadding(rightLabelView.getPaddingLeft(), rightLabelView.getPaddingTop(), 0, rightLabelView.getPaddingBottom());

        layout.removeAllViews();
        layout.addView(leftLabelView, labelLayoutParams);
        layout.addView(seekBar, seekBarLayoutParams);
        layout.addView(rightLabelView, labelLayoutParams);
        if (valueLabel != null)
            layout.addView(valueLabel, valueLabelLayoutParams);

        leftLabelView.setText(leftLabel);
        rightLabelView.setText(rightLabel);
    }

    public void setLeftLabel (CharSequence leftLabel){
        this.leftLabel = leftLabel;
        if (leftLabelView != null)
            leftLabelView.setText(leftLabel);
    }

    public void setRightLabel (CharSequence rightLabel){
        this.rightLabel = rightLabel;
        if (rightLabelView != null)
            rightLabelView.setText(rightLabel);
    }

    /**
     * Updates the Preference to show the current value of the underlying shared preference. This is
     * useful if the preference value has been changed by something other than this Preference.
     */
    public void loadPersistedValue() {
        setValue(getPersistedInt(getValue()));
    }

}
