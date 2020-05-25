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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import com.cyphercove.coveprefs.R;

public class RotaryView extends View {
	private static final int MISSING_COLOR = Color.MAGENTA;

	private Paint selectorPaint, backgroundPaint;
	private OnValueSelectedListener listener;
	private int value = 0;
	private Path selectorPath;

	private boolean tracking = false;
	private float disabledAlpha;
	private boolean inputEnabled = true;
	private int centerX, centerY, defaultSize, radius, centerDotRadius, defaultSelectorHeight, defaultSelectorWidth, radialPadding;
	private float selectorLineLength;
	private int downValue;

	interface OnValueSelectedListener {
		void onValueSelected(int newValue, boolean isTouchRelease);
	}

	public RotaryView(Context context) {
		this(context, null);
	}

	public RotaryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RotaryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		final TypedValue outValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, outValue, true);
		disabledAlpha = outValue.getFloat();

		final TypedArray a = context.obtainStyledAttributes( attrs,
				R.styleable.CovePrefs_RotaryPicker, defStyleAttr,
				R.style.CovePrefsRotaryPicker);
		final ColorStateList selectorColors = a.getColorStateList(R.styleable.CovePrefs_RotaryPicker_coveprefs_selectorColor);
		int circleBackgroundColor = a.getColor(R.styleable.CovePrefs_RotaryPicker_coveprefs_rotaryCircleBackgroundColor, MISSING_COLOR);
		a.recycle();

		final int selectorActivatedColor;
		if (selectorColors != null) {
			selectorActivatedColor = selectorColors.getColorForState(View.ENABLED_STATE_SET, MISSING_COLOR);
		} else {
			selectorActivatedColor = MISSING_COLOR;
		}
		final Resources res = getResources();
		defaultSize = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_natural_size);
		centerDotRadius = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_center_dot_radius);

		selectorPath = new Path();
		defaultSelectorWidth = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_selector_width);
		defaultSelectorHeight = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_selector_height);
		radialPadding = res.getDimensionPixelSize(R.dimen.coveprefs_rotary_radial_padding);

		selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selectorPaint.setStyle(Paint.Style.FILL);
		selectorPaint.setColor(selectorActivatedColor);
		selectorPaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.coveprefs_rotary_selector_stroke));

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(circleBackgroundColor);

		if (isInEditMode()){
			setValue(100);
		}
	}

	public void setOnValueChangedListener(OnValueSelectedListener listener){
		this.listener = listener;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		value %= 360;
		this.value = value;
		invalidate();
	}

	public boolean isInputEnabled() {
		return inputEnabled;
	}

	public void setInputEnabled(boolean mInputEnabled) {
		this.inputEnabled = mInputEnabled;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (!changed) {
			return;
		}

		centerX = getWidth() / 2;
		centerY = getHeight() / 2;
		radius = Math.min(centerX, centerY);

		float ratio = radius / (float)defaultSize;
		float selectorWidth = ratio * defaultSelectorWidth;
		float selectorHeight = ratio * defaultSelectorHeight;

		selectorPath.reset();
		selectorPath.moveTo(-selectorWidth/2, selectorHeight);
		selectorPath.lineTo(selectorWidth/2, selectorHeight);
		selectorPath.lineTo(0, 0);
		selectorPath.lineTo(-selectorWidth/2, selectorHeight);

		selectorLineLength = radius - radialPadding - selectorHeight / 2;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return Math.max(defaultSize, super.getSuggestedMinimumWidth());
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return Math.max(defaultSize, super.getSuggestedMinimumHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthTarget = MeasureSpec.getSize(widthMeasureSpec);

		switch (widthMode){
		case MeasureSpec.UNSPECIFIED:
			widthTarget = getSuggestedMinimumWidth();
			break;
		case MeasureSpec.AT_MOST:
			case MeasureSpec.EXACTLY:
			widthTarget = Math.min(defaultSize, widthTarget);
			break;
		}

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightTarget = MeasureSpec.getSize(heightMeasureSpec);

		switch (heightMode){
		case MeasureSpec.UNSPECIFIED:
			heightTarget = getSuggestedMinimumHeight();
			break;
		case MeasureSpec.AT_MOST:
			case MeasureSpec.EXACTLY:
			heightTarget = Math.min(defaultSize, heightTarget);
			break;
		}

		setMeasuredDimension(widthTarget, heightTarget);
	}

	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

		final float alphaMod = inputEnabled ? 1 : disabledAlpha;
		selectorPaint.setAlpha((int) (255 * alphaMod + 0.5f));

		canvas.drawCircle(centerX, centerY, centerDotRadius, selectorPaint);

		canvas.drawLine(centerX, centerY,
				centerX + (int)(selectorLineLength * Math.sin(Math.toRadians(value + 90))),
				centerY + (int)(selectorLineLength * Math.cos(Math.toRadians(value + 90))),
				selectorPaint);

		canvas.translate(centerX, centerY);
		canvas.rotate(-value + 90);
		canvas.translate(0, -radius + radialPadding);
		canvas.drawPath(selectorPath, selectorPaint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!inputEnabled) {
			return true;
		}

		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			requestFocus();
			float deltaX = x - centerX;
			float deltaY = y - centerY;
			double distanceFromCenter = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
			tracking = distanceFromCenter <= radius;
			if (tracking)
				playSoundEffect(SoundEffectConstants.CLICK);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if (tracking && value != downValue)
				playSoundEffect(SoundEffectConstants.CLICK);
			tracking = false;
			break;
		}
		if (tracking) {
			int previousValue = value;
			value = valueFromPosition(x, y);
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				downValue = value;
			invalidate();
			if (value != previousValue && listener != null){
				listener.onValueSelected(value, event.getAction() == MotionEvent.ACTION_UP);
			}
		}
		return true;
	}

	private int valueFromPosition (float x, float y){
		return (int)(angleBetweenLines(centerX, 0, x - centerX, centerY - y) + 360) % 360;
	}
	
	private static float angleBetweenLines(float x1, float y1, float x2, float y2){
		float dot = x1*x2 + y1*y2;      // dot product
		float det = x1*y2 - y1*x2;      // determinant
		return (float)Math.toDegrees(Math.atan2(det, dot));
	}
}
