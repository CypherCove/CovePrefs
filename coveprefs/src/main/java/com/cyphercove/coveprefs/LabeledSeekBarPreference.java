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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

/**
 * A {@link SeekBarPreference} with left and right labels next to the SeekBar.
 */
@SuppressWarnings("WeakerAccess")
public class LabeledSeekBarPreference extends SeekBarPreference {

    private CharSequence leftLabel, rightLabel;
    private TextView leftLabelView, rightLabelView;

    private static final String TAG = "SeekBarPlusPreference";

    public LabeledSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CovePrefs_LabeledSeekBarPreference, defStyleAttr, defStyleRes);
        leftLabel = a.getString(R.styleable.CovePrefs_LabeledSeekBarPreference_coveprefs_leftLabel);
        rightLabel = a.getString(R.styleable.CovePrefs_LabeledSeekBarPreference_coveprefs_rightLabel);
        setMax(a.getInteger(R.styleable.CovePrefs_LabeledSeekBarPreference_max, getMax())); // This is to deal with a resource linking issue
        a.recycle();
    }

    public LabeledSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LabeledSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.seekBarPreferenceStyle);
    }

    public LabeledSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        SeekBar seekBar = (SeekBar) view.findViewById(androidx.preference.R.id.seekbar);
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
        if (valueLabel != null) {
            if (valueLabel.getParent() == layout) {
                valueLabelLayoutParams = valueLabel.getLayoutParams();
            } else {
                valueLabel = null;
            }
        }

        LinearLayout.LayoutParams labelLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 0);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        leftLabelView = (TextView)inflater.inflate(R.layout.coveprefs_seekbar_label_left, layout, false);
        rightLabelView = (TextView)inflater.inflate(R.layout.coveprefs_seekbar_label_right, layout, false);

        layout.removeAllViews();
        layout.addView(leftLabelView, labelLayoutParams);
        layout.addView(seekBar, seekBarLayoutParams);
        layout.addView(rightLabelView, labelLayoutParams);
        if (valueLabel != null)
            layout.addView(valueLabel, valueLabelLayoutParams);

        leftLabelView.setText(leftLabel);
        rightLabelView.setText(rightLabel);
    }

    public CharSequence getLeftLabel() {
        return leftLabel;
    }

    public void setLeftLabel (CharSequence leftLabel){
        if ((leftLabel == null && this.leftLabel != null) || (leftLabel != null && !leftLabel.equals(this.leftLabel))) {
            this.leftLabel = leftLabel;
            if (leftLabelView != null) {
                leftLabelView.setText(leftLabel);
            }
            notifyChanged();
        }
    }

    public CharSequence getRightLabel() {
        return rightLabel;
    }

    public void setRightLabel (CharSequence rightLabel){
        if ((rightLabel == null && this.rightLabel != null) || (rightLabel != null && !rightLabel.equals(this.rightLabel))) {
            this.rightLabel = rightLabel;
            if (rightLabelView != null) {
                rightLabelView.setText(rightLabel);
            }
            notifyChanged();
        }
    }

    /**
     * Updates the Preference to show the current value of the underlying shared preference. This is
     * useful if the preference value has been changed by something other than this Preference.
     */
    public void loadPersistedValue() {
        setValue(getPersistedInt(getValue()));
    }

}
