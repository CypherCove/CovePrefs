package com.cyphercove.coveprefs;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @deprecated Use {@link LabeledSeekBarPreference instead. }
 */
@Deprecated
public class SeekBarPlusPreference extends LabeledSeekBarPreference {

    public SeekBarPlusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarPlusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekBarPlusPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarPlusPreference(Context context) {
        super(context);
    }
}
