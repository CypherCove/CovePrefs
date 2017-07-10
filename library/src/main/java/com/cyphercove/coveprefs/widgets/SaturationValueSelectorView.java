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

public class SaturationValueSelectorView extends View {
    private static final int MISSING_COLOR = Color.MAGENTA;
    private static final long ANIMATION_DURATION = 300;

    interface OnSaturationValueChangedListener {
        void onSaturationValueChanged(SaturationValueSelectorView view, float newSaturation, float newValue, boolean isFromTouchDown, int localX, int localY);
    }

    OnSaturationValueChangedListener listener;

    private Paint saturationPaint, valuePaint, selectorPaint, disabledPaint;
    private Path roundCornersMask;
    private int[] saturations= new int[] { 0, 0xFFFFFFFF }; // first index is updated when color is updated
    private final int[] values= new int[] { 0x00000000, 0xFF000000 };//clear black to opaque black;

    private int selectorRadius, cornerRadii, naturalWidth, naturalHeight;

    private float currentSaturation, currentValue;

    public SaturationValueSelectorView(Context context){
    	this(context,null);
    }

    public SaturationValueSelectorView(Context context, AttributeSet attrs){
    	this(context, attrs, 0);
    }

    public SaturationValueSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        naturalWidth = res.getDimensionPixelSize(R.dimen.coveprefs_svview_natural_width);
        naturalHeight = res.getDimensionPixelSize(R.dimen.coveprefs_hsv_natural_height);
        selectorRadius = res.getDimensionPixelSize(R.dimen.coveprefs_hsv_selector_radius);

        saturationPaint = new Paint(Paint.DITHER_FLAG); //may not need aa, since it's rectangular
        saturationPaint.setStyle(Paint.Style.STROKE);

        valuePaint = new Paint(Paint.DITHER_FLAG); //may not need aa, since it's rectangular
        valuePaint.setStyle(Paint.Style.STROKE);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setColor(selectorActivatedColor);
        selectorPaint.setStyle(Paint.Style.FILL);

        disabledPaint = new Paint();
        disabledPaint.setColor(0xAA808080);
        disabledPaint.setStyle(Paint.Style.FILL);

        if (Build.VERSION.SDK_INT >= 21){
            roundCornersMask = new Path();
        }
    }

    public float getSaturation(){
        return currentSaturation;
    }

    public float getValue () {
        return currentValue;
    }

    private void updateSaturationPaintColor(){
        Shader saturationShader = new LinearGradient(getWidth(), 0, 0, 0, saturations, null, Shader.TileMode.CLAMP);
        saturationPaint.setShader(saturationShader);
    }

    /** Sets the currently visible color and selector position. Does not trigger a callback on the listener. */
    public void setColor (float hue, float saturation, float value){
        setHue(hue);
        currentSaturation = saturation;
        currentValue = value;
        invalidate();
    }

    /** Sets the visible hue. */
    public void setHue (float hue){
        float[] saturated = {hue,1f,1f};
        saturations[0] = Color.HSVToColor(saturated); //update the opaque saturated color
        updateSaturationPaintColor();
        invalidate();
    }

    public void setOnSaturationValueChangedListener(OnSaturationValueChangedListener listener){
    	this.listener = listener;
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        if (roundCornersMask != null){
            canvas.clipPath(roundCornersMask);
        }

        // Draw saturation as horizontal line across center
        float svCenterY = getHeight() / 2f;
        canvas.drawLine(0, svCenterY, getWidth(), svCenterY, saturationPaint);

        // Draw value as vertical line up center
        float svCenterX = getWidth() / 2f;
        canvas.drawLine(svCenterX, 0, svCenterX, getHeight(), valuePaint);

        if (isEnabled()){
            canvas.drawCircle(currentSaturation * getWidth(),
                    (1 - currentValue) * getHeight(),
                    selectorRadius, selectorPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), disabledPaint);
        }
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;
        float x = Math.max(0, Math.min(getWidth(), event.getX()));
        float y = Math.max(0, Math.min(getHeight(), event.getY()));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                playSoundEffect(SoundEffectConstants.CLICK);
            	requestFocus();
                setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                break;
        }
        invalidate();
        currentSaturation = x / (float)getWidth(); // saturation
        currentValue = 1f - y / (float)getHeight(); // value
        if (listener !=null){
        	listener.onSaturationValueChanged(this, getSaturation(), getValue(),
                    event.getAction() == MotionEvent.ACTION_DOWN, (int)x, (int)y);
        }
        
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }

        saturationPaint.setStrokeWidth(getHeight());
        updateSaturationPaintColor();

        Shader valueShader = new LinearGradient(0, 0, 0, getHeight(), values, null, Shader.TileMode.CLAMP);
        valuePaint.setShader(valueShader);
        valuePaint.setStrokeWidth(getWidth());

        if (Build.VERSION.SDK_INT >= 21){
            roundCornersMask.reset();
            roundCornersMask.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadii, cornerRadii, Path.Direction.CW);
        }
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return Math.max(naturalWidth, super.getSuggestedMinimumWidth());
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
            case MeasureSpec.AT_MOST:
                widthTarget = Math.min(naturalWidth, widthTarget);
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