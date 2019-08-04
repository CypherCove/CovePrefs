/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

/*
 * This is a modified derivative version of the v7.preference library's android.support.v7.internal.widget.PreferenceImageView
 * class. Attributes duplicated for use without preference library. Removed four-input constructor for compatibility
 * back to API 15. */
package com.cyphercove.coveprefs.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import com.cyphercove.coveprefs.R;

/**
 * Extension of AppCompatImageView that correctly applies maxWidth and maxHeight.
 */
public class PreferenceImageView extends AppCompatImageView {

    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;

    public PreferenceImageView(Context context) {
        this(context, null);
    }

    public PreferenceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CovePrefs_PreferenceImageView, defStyleAttr, 0);

        setMaxWidth(a.getDimensionPixelSize(
                R.styleable.CovePrefs_PreferenceImageView_maxWidth, Integer.MAX_VALUE));

        setMaxHeight(a.getDimensionPixelSize(
                R.styleable.CovePrefs_PreferenceImageView_maxHeight, Integer.MAX_VALUE));

        a.recycle();
    }

    @Override
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        super.setMaxWidth(maxWidth);
    }

    public int getMaxWidthCompat() {
        return mMaxWidth;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        super.setMaxHeight(maxHeight);
    }

    public int getMaxHeightCompat() {
        return mMaxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int maxWidth = getMaxWidthCompat();
            if (maxWidth != Integer.MAX_VALUE
                    && (maxWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
            }
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            final int maxHeight = getMaxHeightCompat();
            if (maxHeight != Integer.MAX_VALUE
                    && (maxHeight < heightSize || heightMode == MeasureSpec.UNSPECIFIED)) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
