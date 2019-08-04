package com.cyphercove.coveprefs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.PreferenceCategory;

/**
 * A category with 0 height, which can be used to hide specific dividers of adjacent preferences. Setting a custom
 * layout has no effect on Divider Hider.
 */
public class DividerHider extends PreferenceCategory {
    public DividerHider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.coveprefs_divider_hider);
    }
}
