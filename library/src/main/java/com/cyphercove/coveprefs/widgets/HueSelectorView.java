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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import com.cyphercove.coveprefs.R;

public class HueSelectorView extends View {
    private static final int MISSING_COLOR = Color.MAGENTA;

    interface OnHueChangedListener {
        void onHueChanged(HueSelectorView view, float newHue, boolean isFromTouchDown, float localX, float localY);
    }

    OnHueChangedListener listener;

    private Paint huePaint, selectorPaint, disabledPaint;
    private Path roundCornersMask;
    private final int[] hues= new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};

    private int selectorStrokeWidth, cornerRadii, naturalHeight;

    private float currentHue;

    public HueSelectorView(Context context){
    	this(context,null);
    }

    public HueSelectorView(Context context, AttributeSet attrs){
    	this(context, attrs, 0);
    }

    public HueSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CovePrefs_ColorPicker, defStyleAttr, R.style.CovePrefsColorPicker);
        final ColorStateList indicatorColors = a.getColorStateList(R.styleable.CovePrefs_ColorPicker_coveprefs_selectorColor);
        cornerRadii = a.getDimensionPixelSize(R.styleable.CovePrefs_ColorPicker_coveprefs_colorPickerCornerRadii, 1);
        a.recycle();

        final int selectorActivatedColor;
        if (indicatorColors != null) {
            selectorActivatedColor = indicatorColors.getColorForState(View.ENABLED_STATE_SET, MISSING_COLOR);
        }  else {
            selectorActivatedColor = MISSING_COLOR;
        }

        final Resources res = getResources();
        naturalHeight = res.getDimensionPixelSize(R.dimen.coveprefs_hsv_natural_height);
        selectorStrokeWidth = res.getDimensionPixelSize(R.dimen.coveprefs_hsv_selector_stroke_width);

        huePaint = new Paint(Paint.DITHER_FLAG);
        huePaint.setStyle(Paint.Style.STROKE);

        selectorPaint = new Paint();
        selectorPaint.setColor(selectorActivatedColor);
        selectorPaint.setStrokeWidth(selectorStrokeWidth);
        selectorPaint.setStyle(Paint.Style.FILL);

        disabledPaint = new Paint();
        disabledPaint.setColor(0xAA808080);
        disabledPaint.setStyle(Paint.Style.FILL);

        if (Build.VERSION.SDK_INT >= 21){
            roundCornersMask = new Path();
        }
    }

    public float getHue(){
        return currentHue;
    }

    /** Changes the currently selected hue. Does not trigger the listener.
     * @param hue The hue to set the selector to.*/
    public void setHue (float hue){
        currentHue = hue;
        invalidate();
    }

    public void setOnHueChangedListener(OnHueChangedListener listener){
    	this.listener = listener;
    }
    
    private static float getHue(int color){
    	float[] hsv={0f,0f,0f};
    	Color.colorToHSV(color, hsv);
    	return hsv[0];
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        if (roundCornersMask != null){
            canvas.clipPath(roundCornersMask);
        }

        int hueStripCenterX = getWidth() / 2;
        canvas.drawLine(hueStripCenterX, 0, hueStripCenterX, getHeight(), huePaint);

        if (isEnabled()) {
            float hueSelectorHeight = currentHue / 360f * getHeight();
            canvas.drawLine(0, hueSelectorHeight, getWidth(), hueSelectorHeight, selectorPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), disabledPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                playSoundEffect(SoundEffectConstants.CLICK);
            	requestFocus();
            	setPressed(true);
                currentHue = y / getHeight() * 360;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                y = Math.max(0, Math.min(getHeight(), y));
                currentHue = y / getHeight() * 360;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                invalidate();
                break;
        }
        if (listener !=null){
        	listener.onHueChanged(this, currentHue,
                    event.getAction() == MotionEvent.ACTION_DOWN, (int)x, (int)y);
        }
        
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }

        Shader hueShader = new LinearGradient(0, 0, 0, getHeight(), hues, null, Shader.TileMode.CLAMP);
        huePaint.setShader(hueShader);
        huePaint.setStrokeWidth(getWidth());

        if (Build.VERSION.SDK_INT >= 21){
            roundCornersMask.reset();
            roundCornersMask.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadii, cornerRadii, Path.Direction.CW);
        }
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max(naturalHeight, super.getSuggestedMinimumHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthTarget = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                widthTarget = getSuggestedMinimumWidth();
                break;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightTarget = MeasureSpec.getSize(heightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                heightTarget = getSuggestedMinimumHeight();
                break;
            case MeasureSpec.AT_MOST:
                heightTarget = Math.min(naturalHeight, heightTarget);
                break;
        }

        setMeasuredDimension(widthTarget, heightTarget);
    }
}