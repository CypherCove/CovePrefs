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
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageButton;
import android.text.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.cyphercove.coveprefs.R;
import com.cyphercove.coveprefs.utils.ColorUtils;
import com.cyphercove.coveprefs.utils.MultiColor;
import com.cyphercove.coveprefs.utils.ViewUtils;

import static com.cyphercove.coveprefs.utils.ViewUtils.getRelativeX;
import static com.cyphercove.coveprefs.utils.ViewUtils.getRelativeY;

/**
 */
public class MultiColorPicker extends FrameLayout {
    private static final int MISSING_COLOR = Color.MAGENTA;

    public interface OnMultiColorChangedListener {
        void onColorChanged(MultiColor multiColor);
    }
    public interface OnActiveIndexChangedListener {
        /**Called when the currently selected color slot for the current MultiColor type has changed. This can be used
         * to restore the view on a configuration change. */
         void onActiveIndexChanged(int index);
    }
    private HSVSelectorView hsvView;
    private View hexHashMark;
    private EditText hexEditText;
    private boolean hexEditTextWatcherDisabled;
    private ViewPager headerViewPager;
    private ImageButton prevButton, nextButton;
    private ColorCacheView colorCacheView;
    private OnMultiColorChangedListener listener;
    private OnActiveIndexChangedListener indexListener;
    private int currentWidgetsColor;
    private float headerShadowRadius;

    private MultiColor multiColor;
    private int activeIndex;

    private final LayoutInflater inflater;
    private int headerTextAppearanceId, headerDisabledBackgroundColor;

    private static class HeaderItem {
        ColorSwatch colorSwatch;
        TextView textView;
    }
    private HeaderItem[][] headerItems;

    public MultiColorPicker(Context context) {
        this(context, null);
    }

    public MultiColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CovePrefs_ColorPicker, defStyleAttr, R.style.CovePrefsColorPicker);
        headerDisabledBackgroundColor = a.getColor(R.styleable.CovePrefs_ColorPicker_coveprefs_multiColorPickerHeaderDisabledColor, MISSING_COLOR);
        headerTextAppearanceId = a.getResourceId(
                R.styleable.CovePrefs_ColorPicker_coveprefs_headerTextAppearance, 0);
        ColorStateList headerIconColorStateList = a.getColorStateList(R.styleable.CovePrefs_ColorPicker_coveprefs_headerIconButtonColor);
        a.recycle();

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.coveprefs_color_picker, this);

        findViewById(R.id.coveprefs_multicolor_header).setVisibility(View.VISIBLE);
        headerViewPager = (ViewPager) findViewById(R.id.coveprefs_pager_swatch);
        headerViewPager.setAdapter(headerAdapter);
        headerViewPager.setFocusableInTouchMode(true); // Allow touch to unfocus the hexEditText so keyboard closes
        headerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                multiColor.setType(position);
                int valueCount = multiColor.getValueCount();
                setWidgetsEnabled(valueCount != 0);
                if (valueCount > 0 && valueCount <= activeIndex)
                    setActiveIndex(valueCount - 1);
                if (listener != null)
                    listener.onColorChanged(multiColor);
                prevButton.setEnabled(position != 0);
                nextButton.setEnabled(position != headerAdapter.getCount() - 1);
                updateImageButtonShadows();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        prevButton = (ImageButton)findViewById(R.id.coveprefs_prev);
        if (headerIconColorStateList != null && prevButton instanceof AppCompatImageButton)
            ((AppCompatImageButton)prevButton).setSupportImageTintList(headerIconColorStateList);
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = headerViewPager.getCurrentItem();
                if (currentItem > 0)
                    headerViewPager.setCurrentItem(currentItem - 1, true);
            }
        });

        nextButton = (ImageButton)findViewById(R.id.coveprefs_next);
        if (headerIconColorStateList != null && nextButton instanceof AppCompatImageButton)
            ((AppCompatImageButton)nextButton).setSupportImageTintList(headerIconColorStateList);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = headerViewPager.getCurrentItem();
                if (currentItem < headerAdapter.getCount() - 1)
                    headerViewPager.setCurrentItem(currentItem + 1, true);
            }
        });

        hsvView = (HSVSelectorView)findViewById(R.id.coveprefs_hsv);
        hsvView.setOnColorChangedListener(onHSVColorSelectedListener);
        hsvView.setFocusableInTouchMode(true); // Allow touch to unfocus the hexEditText so keyboard closes
        ViewUtils.clearAncestorOutlineClipping(hsvView, this);

        hexHashMark = findViewById(R.id.coveprefs_hex_hashmark);
        hexEditText = (EditText)findViewById(R.id.coveprefs_hex);
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
                if (hexEditTextWatcherDisabled) return;
                if (s.length() == 6){
                    int color = Color.parseColor("#" + s.toString());
                    setWidgetsColor(color, false, false, true, true,
                           getRelativeX(hexEditText, MultiColorPicker.this) + hexEditText.getWidth(),
                            getRelativeY(hexEditText, MultiColorPicker.this) + hexEditText.getHeight() / 2);
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
                    setHexEditTextColorWithoutPropagation(currentWidgetsColor);
                }
                return false; // allow default keyboard close action
            }
        });

        colorCacheView = (ColorCacheView) findViewById(R.id.coveprefs_colorcache);
        colorCacheView.setOnColorSelectedListener(onColorCacheSelectedListener);
        ViewUtils.clearAncestorOutlineClipping(colorCacheView, this);

        headerShadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                getContext().getResources().getDisplayMetrics());
    }

    private void setHexEditTextColorWithoutPropagation (int color){
        hexEditTextWatcherDisabled = true;
        hexEditText.setText(String.format("%06X", (0xFFFFFF & color)));
        hexEditTextWatcherDisabled = false;
    }

    /** Sets the multi color. The passed object is copied.
     * @param activeIndex The color slot for the MultiColor's current type that should be selected.*/
    public void setMultiColorValue(int activeIndex, MultiColor multiColor) {
        this.multiColor = new MultiColor(multiColor);
        int valueCount = multiColor.getValueCount();
        setWidgetsEnabled(valueCount != 0);
        if (valueCount > 0 && valueCount <= activeIndex)
            activeIndex = valueCount - 1;
        this.activeIndex = activeIndex;

        int activeColor = multiColor.getValues()[activeIndex];
        hsvView.setColor(activeColor, false);
        setHexEditTextColorWithoutPropagation(activeColor);

        headerViewPager.setOffscreenPageLimit(multiColor.definition.getTypeCount());
        headerItems = new HeaderItem[multiColor.definition.getTypeCount()][];
        for (int i = 0; i < headerItems.length; i++) {
            headerItems[i] = new HeaderItem[multiColor.definition.getValueCount(i)];
            for (int j = 0; j < headerItems[i].length; j++) {
                headerItems[i][j] = new HeaderItem();
            }
        }
        headerAdapter.notifyDataSetChanged();
        headerViewPager.setCurrentItem(multiColor.getType());

    }

    public void setWidgets (int widgets){
        boolean hsv = (widgets & ColorPicker.WIDGET_HSV_PICKER) != 0;
        boolean hex = (widgets & ColorPicker.WIDGET_HEX_TEXT_EDIT) != 0;
        boolean recent = (widgets & ColorPicker.WIDGET_RECENTLY_PICKED) != 0;

        hsvView.setVisibility(hsv ? VISIBLE : GONE);
        hexHashMark.setVisibility(hex ? VISIBLE : GONE);
        hexEditText.setVisibility(hex ? VISIBLE : GONE);
        colorCacheView.setVisibility(recent ? VISIBLE : GONE);
    }

    private void setWidgetsEnabled (boolean enabled){
        hsvView.setEnabled(enabled);
        colorCacheView.setEnabled(enabled);
        hexEditText.setEnabled(enabled);
    }

    private void setWidgetsColor(int color, boolean isFromActivatedIndex, boolean isFromHSV, boolean isFromHex,
                                 boolean animated, float animationCenterX, float animationCenterY) {
        if (multiColor.getValueCount() == 0) {
            return;
        }

        if (!isFromActivatedIndex){
            multiColor.getValues()[activeIndex] = color;

            if (headerItems != null) {
                for (HeaderItem[] headerItemsByType : headerItems) {
                    for (int i = 0; i < headerItemsByType.length; i++) {
                        if (i == activeIndex) {
                            HeaderItem headerItem = headerItemsByType[i];
                            ColorSwatch colorSwatch = headerItem.colorSwatch;
                            TextView textView = headerItem.textView;
                            if (colorSwatch != null) {
                                if (animated)
                                    colorSwatch.setColorAnimated(
                                            animationCenterX - getRelativeX(colorSwatch, this),
                                            animationCenterY - getRelativeY(colorSwatch, this),
                                            color | 0xff000000);
                                else
                                    colorSwatch.setColor(color | 0xff000000);
                            }
                            if (textView != null) {
                                ColorUtils.setShadow(textView, Color.BLACK, needTextShadow(color) ? headerShadowRadius : 0);
                            }
                            updateImageButtonShadows();
                        }
                    }
                }
            }
        }

        if (currentWidgetsColor == color) {
            return;
        }
        currentWidgetsColor = color;

        if (!isFromHSV) {
            hsvView.setColor(color, animated);
        }
        if (!isFromHex) {
            setHexEditTextColorWithoutPropagation(color);
        }

        if (listener != null && !isFromActivatedIndex){
            listener.onColorChanged(multiColor);
        }
    }

    private boolean needTextShadow (int color){
        return ColorUtils.calculateGrayScaleValue(color) > 0.9f;
    }

    private void updateImageButtonShadows (){
//        int type = multiColor.getType();
//        boolean disabled = multiColor.definition.getValueCount(type) == 0;
//        boolean landscape = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        boolean paleFirstColor = ColorUtils.calculateGrayScaleValue(multiColor.getValues()[0]) > 0.9f;
//        boolean prevNeedShadow = !disabled && paleFirstColor;
//        boolean nextNeedShadow =  !disabled && (landscape ? paleFirstColor :
//                ColorUtils.calculateGrayScaleValue(multiColor.getValues()[multiColor.getValueCount() - 1]) > 0.9f);
        //TODO swap drawable to one with shadow or outline
    }

    public int getColor() {
        return currentWidgetsColor;
    }

    public void setOnMultiColorChangedListener (OnMultiColorChangedListener listener){
        this.listener = listener;
    }

    public void setOnActiveIndexChangedListener (OnActiveIndexChangedListener listener){
        this.indexListener = listener;
    }

    protected void setActiveIndex (int index){
        activeIndex = index;
        if (headerItems != null) {
            for (HeaderItem[] headerItemForType : headerItems) {
                for (int i = 0; i < headerItemForType.length; i++) {
                    TextView textView = headerItemForType[i].textView;
                    if (textView != null) textView.setActivated(i == activeIndex);
                }
            }
        }

        setWidgetsColor(multiColor.getValues()[index], true, false, false,
                true, 0 , 0);

        if (indexListener != null)
            indexListener.onActiveIndexChanged(index);
    }

    private HSVSelectorView.OnColorChangedListener onHSVColorSelectedListener = new HSVSelectorView.OnColorChangedListener() {
        @Override
        public void onColorChanged(HSVSelectorView view, int newColor, boolean isFromTouchDown, float localX, float localY) {
            setWidgetsColor(newColor, false, true, false, isFromTouchDown,
                    getRelativeX(view, MultiColorPicker.this) + localX,
                    getRelativeY(view, MultiColorPicker.this) + localY);
        }
    };

    private ColorCacheView.OnColorSelectedListener onColorCacheSelectedListener = new ColorCacheView.OnColorSelectedListener() {
        @Override
        public void onColorChanged(Button view, int newColor, float localX, float localY) {
            setWidgetsColor(newColor, false, false, false, true,
                    getRelativeX(view, MultiColorPicker.this) + localX,
                    getRelativeY(view, MultiColorPicker.this) + localY);
            hsvView.requestFocus(); // avoid allowing focus jump back to EditText when buttons are pressed
        }
    };

    /** Rejects changes that insert non-hex digits. */
    private InputFilter hexInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (Character.digit(source.charAt(i), 16) == -1) {
                    return "";
                }
            }
            return null;
        }
    };

    private PagerAdapter headerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return multiColor == null ? 0 : multiColor.definition.getTypeCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            String[] labels = multiColor.definition.getLabels(position);
            if (labels.length == 0) {
                View view = makeColorItem(multiColor.definition.getDisabledLabel(), headerDisabledBackgroundColor, false, position, -1);
                collection.addView(view);
                return view;
            } else if (labels.length == 1) {
                View view = makeColorItem(labels[0], multiColor.getValues()[0], false, position, 0);
                collection.addView(view);
                return view;
            } else {
                LinearLayout swatchLayout = new LinearLayout(getContext());
                boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                swatchLayout.setOrientation(landscape ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                        landscape ? ViewGroup.LayoutParams.MATCH_PARENT : 0,
                        landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT){
                    {
                        weight = 1;
                    }
                };
                for (int i = 0; i < labels.length; i++) {
                    View view = makeColorItem(labels[i], multiColor.getValues()[i], true, position, i);
                    view.setLayoutParams(itemParams);
                    swatchLayout.addView(view);
                }
                collection.addView(swatchLayout);
                return swatchLayout;
            }
        }

        private View makeColorItem (String label, int initialColor, boolean clickable, final int type, final int index){
            View view = inflater.inflate(R.layout.coveprefs_multicolor_header_item, null);
            boolean disabled = multiColor.definition.getValueCount(type) == 0;
            ColorSwatch colorSwatch = (ColorSwatch)view.findViewById(R.id.coveprefs_swatch);
            colorSwatch.setColor(initialColor);
            if (!disabled) headerItems[type][index].colorSwatch = colorSwatch;
            TextView textView = (TextView)view.findViewById(R.id.coveprefs_label);
            textView.setText(label);
            if (!disabled) {
                textView.setActivated(index == activeIndex);
                if (clickable) {
                    textView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setActiveIndex(index);
                        }
                    });
                } else {
                    textView.setFocusable(false);
                }
                headerItems[type][index].textView = textView;
            }
            if (headerTextAppearanceId != 0){
                if (Build.VERSION.SDK_INT < 23)
                    textView.setTextAppearance(getContext(), headerTextAppearanceId);
                else
                    textView.setTextAppearance(headerTextAppearanceId);
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }
    };


}
