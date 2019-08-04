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
package com.cyphercove.coveprefs.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.cyphercove.coveprefs.R;

/**
 */
public class RotaryPicker extends FrameLayout {
    private static final int MISSING_COLOR = Color.MAGENTA;

    public interface OnAngleChangedListener {
        void onAngleChanged(int newAngle);
    }
    private RotaryView rotaryView;
    private TextView angleTextView;
    private OnAngleChangedListener listener;
    private int currentAngle;

    public RotaryPicker(Context context) {
        this(context, null);
    }

    public RotaryPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotaryPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CovePrefs_RotaryPicker, defStyleAttr, R.style.CovePrefsRotaryPicker);
        int headerTextColor = a.getColor(R.styleable.CovePrefs_RotaryPicker_coveprefs_headerTextColor, MISSING_COLOR);
        int headerBackgroundColor = a.getColor(R.styleable.CovePrefs_RotaryPicker_coveprefs_headerBackgroundColor, MISSING_COLOR);
        int headerTextAppearanceResId = a.getResourceId(
                R.styleable.CovePrefs_RotaryPicker_coveprefs_headerTextAppearance, -1);
        a.recycle();

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.coveprefs_rotary_picker, this);

        rotaryView = (RotaryView)findViewById(R.id.coveprefs_rotary);
        rotaryView.setOnValueChangedListener(onValueSelectedListener);

        angleTextView = (TextView)findViewById(R.id.coveprefs_header);
        angleTextView.setBackgroundColor(headerBackgroundColor);
        angleTextView.setTextColor(headerTextColor);

        if (Build.VERSION.SDK_INT < 23)
            angleTextView.setTextAppearance(context, headerTextAppearanceResId);
        else
            angleTextView.setTextAppearance(headerTextAppearanceResId);
    }

    public void setAngle(int angle) {
        angle %= 360;
        setAngleInternal(angle, false, true);
    }

    private void setAngleInternal(int angle, boolean isFromPicker, boolean announce) {
        if (currentAngle == angle) {
            return;
        }

        currentAngle = angle;
        CharSequence text = angle + "°";
        angleTextView.setText(text);
        if (!isFromPicker){
            rotaryView.setValue(angle);
        }
        if (announce && Build.VERSION.SDK_INT >= 16){
            announceForAccessibility(text);
        }

        if (listener != null){
            listener.onAngleChanged(angle);
        }
    }

    public int getAngle() {
        return currentAngle;
    }

    public void setOnAngleChangedListener (OnAngleChangedListener listener){
        this.listener = listener;
    }

    RotaryView.OnValueSelectedListener onValueSelectedListener = new RotaryView.OnValueSelectedListener() {
        @Override
        public void onValueSelected(int newValue, boolean isTouchRelease) {
            setAngleInternal(newValue, true, isTouchRelease);
        }
    };
}
