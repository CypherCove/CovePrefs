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
 * This is a modified derivative version of the v7.preference library's android.support.v7.preference.SeekBarPreference class.
 */

package com.cyphercove.coveprefs.support;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.cyphercove.coveprefs.utils.AbsViewHolder;

/**
 * SeekBarPreference is based on SeekBarPreference in the v7.preference library. It shows a SeekBar within the preference
 * layout itself, and automatically updates and commits the new value when the user adjusts the SeekBar. It additionally
 * has the option for String labels to be shown on the left and right ends of the SeekBar. This may be useful in situations
 * where it is simpler for the user not to think in terms of the numerical value of the preference.
 * <p>
 * Compared to the v7.preference SeekBarPreference, this class's SeekBar has a bigger vertical hit area so it is easier
 * to press.
 * <p>
 * Setting a custom layout via {@code android:layout} in xml has no effect on SeekBarPreference.
 */
public class SeekBarPreference extends BaseInlinePreference<Integer> {

    private int min, max, seekBarIncrement;
    private boolean trackingTouch;
    private SeekBar seekBar;
    private TextView seekBarValueTextView;
    private boolean adjustable; // whether the seekbar should respond to the left/right keys
    private boolean showSeekBarValue;
    private String leftLabel, rightLabel;

    private static final String TAG = "SeekBarPreference";

    /**
     * Listener reacting to the SeekBar changing value by the user
     */
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                int potentialNewValue = min + seekBar.getProgress();
                if (trackingTouch) // v7.preference fully rejects changes during touch tracking, but it's more user friendly to show the value as it changes
                    if (seekBarValueTextView != null) seekBarValueTextView.setText(String.valueOf(potentialNewValue));
                else
                    syncValueFromWidgetChange(potentialNewValue);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            trackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            trackingTouch = false;
            syncValueFromWidgetChange(min + seekBar.getProgress());
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the SeekBar
     * to be handled accordingly.
     */
    private View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!adjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the seekbar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (seekBar == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return seekBar.onKeyDown(keyCode, event);
        }
    };

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.coveprefs_seekbar_preference);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CovePrefs_SeekBarPreference);

        /**
         * The ordering of these two statements are important. If we want to set max first, we need
         * to perform the same steps by changing min/max to max/min as following:
         * max = a.getInt(...) and setMin(...).
         */
        min = a.getInt(R.styleable.CovePrefs_SeekBarPreference_coveprefs_min, 0);
        setMax(a.getInt(R.styleable.CovePrefs_SeekBarPreference_android_max, 100));
        setSeekBarIncrement(a.getInt(R.styleable.CovePrefs_SeekBarPreference_coveprefs_seekBarIncrement, 0));
        adjustable = a.getBoolean(R.styleable.CovePrefs_SeekBarPreference_coveprefs_adjustable, true);
        showSeekBarValue = a.getBoolean(R.styleable.CovePrefs_SeekBarPreference_coveprefs_showSeekBarValue, true);
        leftLabel = a.getString(R.styleable.CovePrefs_SeekBarPreference_coveprefs_leftLabel);
        rightLabel = a.getString(R.styleable.CovePrefs_SeekBarPreference_coveprefs_rightLabel);
        a.recycle();
    }

    @Override
    protected void onPreferenceViewCreated(AbsViewHolder view) {
        view.baseView().setOnKeyListener(mSeekBarKeyListener);
        seekBar = (SeekBar) view.findViewById(R.id.coveprefs_seekbar);

        seekBarValueTextView = (TextView) view.findViewById(R.id.coveprefs_seekbar_value);
        if (showSeekBarValue) {
            seekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            seekBarValueTextView.setVisibility(View.GONE);
            seekBarValueTextView = null;
        }

        TextView leftLabelView = (TextView) view.findViewById(R.id.coveprefs_seekbar_left_label);
        if (leftLabel != null) {
            leftLabelView.setVisibility(View.VISIBLE);
            leftLabelView.setText(leftLabel);
        } else {
            leftLabelView.setVisibility(View.GONE);
        }

        TextView rightLabelView = (TextView) view.findViewById(R.id.coveprefs_seekbar_right_label);
        if (rightLabel != null) {
            rightLabelView.setVisibility(View.VISIBLE);
            rightLabelView.setText(rightLabel);
        } else {
            rightLabelView.setVisibility(View.GONE);
        }

        if (seekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }
        seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        seekBar.setMax(max - min);
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (seekBarIncrement != 0) {
            seekBar.setKeyProgressIncrement(seekBarIncrement);
        } else {
            seekBarIncrement = seekBar.getKeyProgressIncrement();
        }

        seekBar.setProgress(getValue() - min);
        if (seekBarValueTextView != null) {
            seekBarValueTextView.setText(String.valueOf(getValue()));
        }
        seekBar.setEnabled(isEnabled());
    }

    @Override
    protected Integer getDefaultValue() {
        return Color.MAGENTA;
    }

    @Override
    public Class<Integer> getDataType() {
        return Integer.class;
    }

    public void setMin(int min) {
        if (min > max) {
            min = max;
        }
        if (min != this.min) {
            this.min = min;
            notifyChanged();
        }
    }

    public int getMin() {
        return min;
    }

    public final void setMax(int max) {
        if (max < min) {
            max = min;
        }
        if (max != this.max) {
            this.max = max;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     *
     * @return The amount of increment on the SeekBar performed after each user's arrow key press.
     */
    public final int getSeekBarIncrement() {
        return seekBarIncrement;
    }

    /**
     * Sets the increment amount on the SeekBar for each arrow key press.
     *
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != this.seekBarIncrement) {
            this.seekBarIncrement = Math.min(max - min, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public void setLeftLabel(String leftLabel) {
        if (!leftLabel.equals(this.leftLabel)) {
            this.leftLabel = leftLabel;
            notifyChanged();
        }
    }

    public void setRightLabel(String rightLabel) {
        if (!rightLabel.equals(this.rightLabel)) {
            this.rightLabel = rightLabel;
            notifyChanged();
        }
    }

    public int getMax() {
        return max;
    }

    public void setAdjustable(boolean adjustable) {
        this.adjustable = adjustable;
    }

    public boolean isAdjustable() {
        return adjustable;
    }

    protected void onValueChanged(Integer newValue) {
        if (seekBarValueTextView != null) {
            seekBarValueTextView.setText(String.valueOf(newValue));
        }
    }

    @Override
    protected void onWidgetValueChangeRejected(Integer restoreValue) {
        seekBar.setProgress(restoreValue - min);
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

}
