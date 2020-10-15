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
import android.graphics.Color;
import android.text.*;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.cyphercove.coveprefs.R;
import com.cyphercove.coveprefs.utils.CovePrefsUtils;

/**
 */
public class ColorPicker extends FrameLayout {

    // Make sure these are in sync with attrs.xml:
    // <attr name="coveprefs_colorPickerWidgets">
    public static final int WIDGET_HSV_PICKER = 0x01;
    public static final int WIDGET_HEX_TEXT_EDIT = 0x02;
    public static final int WIDGET_RECENTLY_PICKED = 0x04;

    public interface OnColorChangedListener {
        void onColorChanged(int newColor);
    }
    private HSVSelectorView hsvView;
    private View hexHashMark;
    private EditText hexEditText;
    private boolean hexEditTextWatcherDisabled;
    private ColorSwatch colorSwatch;
    private ColorCacheView colorCacheView;
    private OnColorChangedListener listener;
    private int currentColor;

    public ColorPicker(Context context) {
        this(context, null);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.coveprefs_color_picker, this);

        colorSwatch = findViewById(R.id.coveprefs_single_swatch);
        colorSwatch.setVisibility(View.VISIBLE);
        colorSwatch.setFocusableInTouchMode(true); // Allow touch to unfocus the hexEditText so keyboard closes

        hsvView = findViewById(R.id.coveprefs_hsv);
        HSVSelectorView.OnColorChangedListener onHSVColorSelectedListener = new HSVSelectorView.OnColorChangedListener() {
            @Override
            public void onColorChanged (HSVSelectorView view, int newColor, boolean isFromTouchDown, float localX, float localY) {
                setColorInternal(newColor, true, false, isFromTouchDown,
                        CovePrefsUtils.getRelativeX(view, ColorPicker.this) + localX,
                        CovePrefsUtils.getRelativeY(view, ColorPicker.this) + localY);
            }
        };
        hsvView.setOnColorChangedListener(onHSVColorSelectedListener);
        hsvView.setFocusableInTouchMode(true); // Allow touch to unfocus the hexEditText so keyboard closes
        CovePrefsUtils.clearAncestorOutlineClipping(hsvView, this);

        hexHashMark = findViewById(R.id.coveprefs_hex_hashmark);
        hexEditText = findViewById(R.id.coveprefs_hex);
        InputFilter hexInputFilter = new InputFilter() {
            // Rejects changes that insert non-hex digits.
            @Override
            public CharSequence filter (CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.digit(source.charAt(i), 16) == -1) {
                        return "";
                    }
                }
                return null;
            }
        };
        InputFilter[] inputFilters = {new InputFilter.LengthFilter(6), hexInputFilter, new InputFilter.AllCaps()};
        hexEditText.setFilters(inputFilters);
        hexEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        hexEditText.setSingleLine();
        hexEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        hexEditText.setOnFocusChangeListener(new OnFocusChangeListener() { // auto-hide keyboard when focus lost
            public void onFocusChange(View v, boolean hasFocus) {
                if (v == hexEditText) {
                    if (!hasFocus)
                        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(hexEditText.getWindowToken(), 0);
                }
            }
        });
        hexEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                if (hexEditTextWatcherDisabled)
                    return;
                if (s.length() == 6){
                    int color = Color.parseColor("#" + s.toString());
                    setColorInternal(color, false, true, true,
                            CovePrefsUtils.getRelativeX(hexEditText, ColorPicker.this) + hexEditText.getWidth(),
                            CovePrefsUtils.getRelativeY(hexEditText, ColorPicker.this) + (float)(hexEditText.getHeight() / 2));
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        hexEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Restore shown value if invalid value entered
                if ((actionId == EditorInfo.IME_ACTION_DONE && v.getText().length() != 6)) {
                    setHexEditTextColorWithoutPropagation(currentColor);
                }
                return false; // allow default keyboard close action
            }
        });

        colorCacheView = findViewById(R.id.coveprefs_colorcache);
        // avoid allowing focus jump back to EditText when buttons are pressed
        ColorCacheView.OnColorSelectedListener onColorCacheSelectedListener = new ColorCacheView.OnColorSelectedListener() {
            @Override
            public void onColorChanged (Button view, int newColor, float localX, float localY) {
                setColorInternal(newColor, false, false, true,
                        CovePrefsUtils.getRelativeX(view, ColorPicker.this) + localX,
                        CovePrefsUtils.getRelativeY(view, ColorPicker.this) + localY);
                hsvView.requestFocus(); // avoid allowing focus jump back to EditText when buttons are pressed
            }
        };
        colorCacheView.setOnColorSelectedListener(onColorCacheSelectedListener);
        CovePrefsUtils.clearAncestorOutlineClipping(colorCacheView, this);
    }

    public void setWidgets (int widgets){
        boolean hsv = (widgets & WIDGET_HSV_PICKER) != 0;
        boolean hex = (widgets & WIDGET_HEX_TEXT_EDIT) != 0;
        boolean recent = (widgets & WIDGET_RECENTLY_PICKED) != 0;

        hsvView.setVisibility(hsv ? VISIBLE : GONE);
        hexHashMark.setVisibility(hex ? VISIBLE : GONE);
        hexEditText.setVisibility(hex ? VISIBLE : GONE);
        colorCacheView.setVisibility(recent ? VISIBLE : GONE);
    }

    private void setHexEditTextColorWithoutPropagation (int color){
        hexEditTextWatcherDisabled = true;
        hexEditText.setText(String.format("%06X", (0xFFFFFF & color)));
        hexEditTextWatcherDisabled = false;
    }

    public void setColor (int color) {
        setColorInternal(color, false,  false, false, 0, 0);
    }

    private void setColorInternal(int color, boolean isFromHSV, boolean isFromHex, boolean animated, float animationCenterX, float animationCenterY) {
        if (currentColor == color) {
            return;
        }

        currentColor = color;

        if (animated)
            colorSwatch.setColorAnimated(
                    animationCenterX - CovePrefsUtils.getRelativeX(colorSwatch, this),
                    animationCenterY - CovePrefsUtils.getRelativeY(colorSwatch, this),
                    color | 0xff000000);
        else
            colorSwatch.setColor(color | 0xff000000);

        if (!isFromHSV)
            hsvView.setColor(color, animated);
        if (!isFromHex)
            setHexEditTextColorWithoutPropagation(color);

        if (listener != null){
            listener.onColorChanged(color);
        }
    }

    public int getColor() {
        return currentColor;
    }

    public void setOnColorChangedListener (OnColorChangedListener listener){
        this.listener = listener;
    }


}
