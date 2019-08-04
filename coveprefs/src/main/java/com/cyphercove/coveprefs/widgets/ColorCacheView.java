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
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.cyphercove.coveprefs.R;
import com.cyphercove.coveprefs.utils.ColorCache;
import com.cyphercove.coveprefs.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public class ColorCacheView extends FrameLayout {
    private HashMap<Button, Integer> buttonsToColors = new HashMap<>();
    private OnColorSelectedListener listener;

    interface OnColorSelectedListener {
        void onColorChanged(Button view, int newColor, float localX, float localY);
    }

    public ColorCacheView(Context context) {
        this(context, null);
    }

    public ColorCacheView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorCacheView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.coveprefs_color_cache_view, this);

        LinearLayout root = (LinearLayout) findViewById(R.id.coveprefs_colorcache_container);
        ViewUtils.clearAncestorOutlineClipping(root, this);

        Resources resources = getResources();
        final int buttonWidth = resources.getDimensionPixelSize(R.dimen.coveprefs_recent_color_button_width);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(context, attrs){
            {
                height = WRAP_CONTENT;
                width = buttonWidth;
            }
        };

        ArrayList<Integer> colors = ColorCache.getCachedColors(getContext());
        buttonsToColors.clear();
        if (colors.size() > 0) {
            for (Integer color : colors){
                color |= 0xFF000000;
                AppCompatButton button = new AppCompatButton(getContext(), attrs, android.R.attr.buttonStyle);
                button.setSupportBackgroundTintList(ColorStateList.valueOf(color));
                button.setLayoutParams(buttonLayoutParams);
                button.setOnClickListener(onClickListener);
                button.setFocusable(true);
                buttonsToColors.put(button, color);
                root.addView(button);
            }
        }
    }

    @Override
    public void setEnabled (boolean enabled){
        super.setEnabled(enabled);
        for (Button button : buttonsToColors.keySet()) {
            button.setEnabled(enabled);
            button.setAlpha(enabled ? 1 : .3f);
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null){
                listener.onColorChanged((Button)v, buttonsToColors.get(v), v.getWidth() / 2, v.getHeight() / 2);
            }
        }
    };

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }
}
