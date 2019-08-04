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

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.cyphercove.coveprefs.R;
import com.cyphercove.coveprefs.utils.Curve;
import com.cyphercove.coveprefs.utils.ViewUtils;

public class HSVSelectorView extends FrameLayout {
    private static final long ANIMATION_DURATION = 300;

    interface OnColorChangedListener {
        void onColorChanged(HSVSelectorView view, int newColor, boolean isFromTouchDown, float localX, float localY);
    }

    OnColorChangedListener mListener;

    private float[] hsvSelected = {0f, 0f, 0f};
    private float[] hsvOld = {0f, 0f, 0f}, hsvAnimationOut = {0f, 0f, 0f};
    private ValueAnimator valueAnimator;
    private SaturationValueSelectorView svView;
    private HueSelectorView hueView;

    public HSVSelectorView(Context context) {
        this(context, null);
    }

    public HSVSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSVSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            Color.colorToHSV(0xFF45FAA6, hsvSelected);
        }

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CovePrefs_ColorPicker, defStyleAttr, R.style.CovePrefsColorPicker);
        final int cornerRadii = a.getDimensionPixelSize(R.styleable.CovePrefs_ColorPicker_coveprefs_colorPickerCornerRadii, 1);
        a.recycle();

        valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.setFloatValues(0, 1); // arbitrary, only using animation fraction

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.coveprefs_hsv_selector, this);
        hueView = (HueSelectorView) findViewById(R.id.coveprefs_hueSelector);
        hueView.setOnHueChangedListener(onHueChangedListener);
        svView = (SaturationValueSelectorView) findViewById(R.id.coveprefs_saturationValueSelector);
        svView.setOnSaturationValueChangedListener(onSaturationValueChangedListener);

        if (Build.VERSION.SDK_INT > 21) {
            applyOutlineProviders(cornerRadii);
        }

        if (isInEditMode()) {
            setColor(0xff33c7af, false);
        }
    }

    @TargetApi(21)
    private void applyOutlineProviders (final int cornerRadii){
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadii);
            }
        };
        hueView.setOutlineProvider(viewOutlineProvider);
        svView.setOutlineProvider(viewOutlineProvider);
        ViewUtils.clearAncestorOutlineClipping(hueView, this);
        ViewUtils.clearAncestorOutlineClipping(svView, this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hueView.setEnabled(enabled);
        svView.setEnabled(enabled);
        if (Build.VERSION.SDK_INT >= 21) {
            if (enabled) {
                hueView.setTranslationZ(0);
                svView.setTranslationZ(0);
            } else {
                hueView.setTranslationZ(-hueView.getZ());
                svView.setTranslationZ(-svView.getZ());
            }
        }
    }

    public int getColor() {
        return Color.HSVToColor(hsvSelected);
    }

    /**
     * Sets the current color, without triggering the listener callback.
     * @param color The new color.
     * @param animated Whether to animate the color change.
     */
    public void setColor(int color, boolean animated) {
        boolean wasAnimating = valueAnimator.isRunning();
        if (wasAnimating) {
            valueAnimator.cancel();
        }
        if (animated) {
            for (int i = 0; i < hsvOld.length; i++) {
                if (wasAnimating)
                    hsvOld[i] = hsvAnimationOut[i];
                else
                    hsvOld[i] = hsvAnimationOut[i] = hsvSelected[i];
            }
            Color.colorToHSV(color, hsvSelected);
            if (hsvSelected[1] == 0)
                hsvSelected[0] = hsvOld[0];
            toViews(hsvOld);
            valueAnimator.setDuration(ANIMATION_DURATION);
            valueAnimator.start();
        } else {
            float oldHue = hsvSelected[0];
            Color.colorToHSV(color, hsvSelected);
            if (hsvSelected[1] == 0)
                hsvSelected[0] = oldHue;
            toViews(hsvSelected);
        }
        invalidate();
    }

    private void toViews(float[] hsv) {
        hueView.setHue(hsv[0]);
        svView.setColor(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * During an animation, a different hue may be used for the svView.
     */
    private void toViews(float[] hsv, float hueForSV) {
        hueView.setHue(hsv[0]);
        svView.setColor(hueForSV, hsv[1], hsv[2]);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = Curve.slowInSlowOut(valueAnimator.getAnimatedFraction());

            for (int i = 0; i < hsvOld.length; i++) {
                hsvAnimationOut[i] = fraction * (hsvSelected[i] - hsvOld[i]) + hsvOld[i];
            }

            if (hsvSelected[0] - hsvOld[0] > 180f) // animate SVView's hue in opposite direction, shortest path
                toViews(hsvAnimationOut, (fraction * (hsvSelected[0] - hsvOld[0] - 360f) + hsvOld[0] + 360f) % 360f);
            else if (hsvOld[0] - hsvSelected[0] > 180f) // animate SVView's hue in opposite direction, shortest path
                toViews(hsvAnimationOut, (fraction * (hsvSelected[0] - hsvOld[0] + 360f) + hsvOld[0]) % 360f);
            else
                toViews(hsvAnimationOut);
        }
    };

    HueSelectorView.OnHueChangedListener onHueChangedListener = new HueSelectorView.OnHueChangedListener() {
        @Override
        public void onHueChanged(HueSelectorView view, float newHue, boolean isFromTouchDown, float localX, float localY) {
            hsvSelected[0] = newHue;
            svView.setHue(newHue);
            if (mListener != null)
                mListener.onColorChanged(HSVSelectorView.this, getColor(), isFromTouchDown,
                        ViewUtils.getRelativeX(view, HSVSelectorView.this) + localX,
                        ViewUtils.getRelativeY(view, HSVSelectorView.this) + localY);
        }
    };

    SaturationValueSelectorView.OnSaturationValueChangedListener onSaturationValueChangedListener = new SaturationValueSelectorView.OnSaturationValueChangedListener() {
        @Override
        public void onSaturationValueChanged(SaturationValueSelectorView view, float newSaturation, float newValue, boolean isFromTouchDown, int localX, int localY) {
            hsvSelected[1] = newSaturation;
            hsvSelected[2] = newValue;
            if (mListener != null)
                mListener.onColorChanged(HSVSelectorView.this, getColor(), isFromTouchDown,
                        ViewUtils.getRelativeX(view, HSVSelectorView.this) + localX,
                        ViewUtils.getRelativeY(view, HSVSelectorView.this) + localY);
        }
    };

}