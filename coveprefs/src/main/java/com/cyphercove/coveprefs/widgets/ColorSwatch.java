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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 */
public class ColorSwatch extends View {

    private static final long ANIMATION_DURATION = 300;

    private int color = 0xFFFF0000;
    private int nextColor = color; // start at same value to indicate no animation occurring

    private float centerX, centerY, radius;
    private Paint colorPaint, nextColorPaint;
    ValueAnimator valueAnimator;

    public ColorSwatch(Context context) {
        this(context, null);
    }

    public ColorSwatch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorSwatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.addListener(animatorListener);
        colorPaint = new Paint();
        colorPaint.setStyle(Paint.Style.FILL);
        nextColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nextColorPaint.setStyle(Paint.Style.FILL);
    }

    /** Instantly changes the color. Allows animation to finish using the new color if one is running.
     * @param color The new color to show. */
    public void setColor (int color){
        if (this.color == nextColor){ // not animating
            this.color = color;
        }
        this.nextColor = color;
        invalidate();
    }

    /** Sets the color with an animation radiating out from the center of this view.
     * @param color The new color to show. */
    public void setColorAnimated (int color) {
        setColorAnimated(getWidth() / 2, getHeight() / 2, color);
    }

    /** Sets the color with an animation starting at the coordinates, relative to this view's position.
     * @param centerX The X coordinate to start the animation from.
     * @param centerY The Y coordinate to start the animation from.
     * @param color The new color to show. */
    public void setColorAnimated (float centerX, float centerY, int color){
        this.color = nextColor; // immediately apply any pending animation
        nextColor = color;

        this.centerX = centerX;
        this.centerY = centerY;

        //Check four corners to find radius needed to fully cover view
        double greatestDst2 = centerX * centerX + centerY * centerY;
        double dst2 = Math.pow(centerX - getWidth(), 2) + centerY * centerY;
        if (dst2 > greatestDst2){
            greatestDst2 = dst2;
        }
        dst2 = centerX * centerX + Math.pow(centerY - getHeight(), 2);
        if (dst2 > greatestDst2){
            greatestDst2 = dst2;
        }
        dst2 = Math.pow(centerX - getWidth(), 2) + Math.pow(centerY - getHeight(), 2);
        if (dst2 > greatestDst2){
            greatestDst2 = dst2;
        }

        valueAnimator.setFloatValues(0, (float)Math.sqrt(greatestDst2));
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        colorPaint.setColor(isEnabled() ? color : withDisabledAlpha(color));
        canvas.drawRect(0, 0, getWidth(), getHeight(), colorPaint);

        if (nextColor != color){
            nextColorPaint.setColor(isEnabled() ? nextColor : withDisabledAlpha(color));
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.drawCircle(centerX, centerY, radius, nextColorPaint);
        }
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener(){
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            radius = (float)valueAnimator.getAnimatedValue();
            invalidate();
        }
    };

    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            color = nextColor;
            invalidate();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private static int withDisabledAlpha(int color) {
        return (color & 0x00ffffff) | 0x80000000;
    }
}
