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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import com.cyphercove.coveprefs.R;
import com.cyphercove.coveprefs.utils.Curve;

public class RotaryPreferenceWidget extends View {

    private static final long ANIMATION_DURATION = 300;

    private float centerX, centerY, radius, arrowRatio, arrowBottom;
    private Paint paint;
    ValueAnimator valueAnimator;
    private Path arrowPath;
    private float value, oldValue, nextValue;

    public RotaryPreferenceWidget(Context context) {
        this(context, null);
    }

    public RotaryPreferenceWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotaryPreferenceWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes( attrs,
                R.styleable.CovePrefs_RotaryPreferenceWidget, defStyleAttr,
                0);
        int color = a.getColor(R.styleable.CovePrefs_RotaryPreferenceWidget_coveprefs_rotaryWidgetColor, 0x80808080);
        a.recycle();

        Resources res = context.getResources();
        float selectorWidth = res.getDimension(R.dimen.coveprefs_rotary_selector_width);
        float selectorHeight = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_selector_height);
        float strokeWidth = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_selector_stroke);
        arrowRatio = selectorWidth / selectorHeight; // Match ratio of the picker's arrow
        arrowPath = new Path();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);

        valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.addListener(animatorListener);
    }

    /** Instantly changes the angle value.
     * @param value The new value to change the widget to.*/
    public void setValue (float value){
        if (valueAnimator.isRunning()){
            valueAnimator.cancel();
        }
        this.value = nextValue = value % 360;
        invalidate();
    }

    public void setValueAnimated (float value){
        if (valueAnimator.isRunning()){
            valueAnimator.end();
        }
        oldValue = this.value;
        nextValue = value % 360;

        valueAnimator.setFloatValues(oldValue, nextValue);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        radius = Math.min(centerX, centerY);

        float arrowWidth = 0.5f * radius / (float)Math.cos(Math.PI * 2 - Math.atan2(arrowRatio, 0.5));
        float arrowHeight = arrowWidth / arrowRatio;
        arrowBottom = arrowHeight - radius;
        arrowPath.reset();
        arrowPath.moveTo(0, -radius);
        arrowPath.lineTo(arrowWidth / 2, arrowBottom);
        arrowPath.lineTo(-arrowWidth / 2, arrowBottom);
        arrowPath.lineTo(0, -radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(centerX, centerY);
        canvas.rotate(90 - value);
        canvas.drawPath(arrowPath, paint);
        canvas.drawLine(0, arrowBottom, 0, radius, paint);
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener(){
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = Curve.slowInSlowOut(valueAnimator.getAnimatedFraction());

            if (nextValue - oldValue > 180f) // animate SVView's hue in opposite direction, shortest path
                value = (fraction * (nextValue - oldValue - 360f) + oldValue + 360f) % 360f;
            else if (oldValue - nextValue > 180f) // animate SVView's hue in opposite direction, shortest path
                value = (fraction * (nextValue - oldValue + 360f) + oldValue) % 360f;
            else
                value = fraction * (nextValue - oldValue) + oldValue;
            invalidate();
        }
    };

    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            value = nextValue;
            invalidate();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}
