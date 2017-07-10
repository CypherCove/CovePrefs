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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 */
public class MultiColorSwatch extends LinearLayout {

    private final ArrayList<ColorSwatch> swatches = new ArrayList<>(5);
    private static final LayoutParams SWATCH_LAYOUT_PARAMS = new LayoutParams(0, LayoutParams.MATCH_PARENT){
        {
            weight = 1;
        }
    };

    public MultiColorSwatch(Context context) {
        this(context, null);
    }

    public MultiColorSwatch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiColorSwatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()){
            int[] colors = {Color.RED, Color.BLUE};
            setColors(colors, 2);
        }
    }

    private void updateSwatchCount (int newCount){
        if (swatches.size() < newCount){
            for (int i = swatches.size(); i < newCount; i++) {
                ColorSwatch colorSwatch = new ColorSwatch(getContext(), null, 0);
                colorSwatch.setLayoutParams(SWATCH_LAYOUT_PARAMS);
                swatches.add(colorSwatch);
                addView(colorSwatch);
            }
        }
        for (int i = 0; i < swatches.size(); i++) {
            swatches.get(i).setVisibility(i < newCount ? View.VISIBLE : View.GONE);
        }
    }

    /** Instantly changes the colors. Allows animation to finish using the new color if one is running.
     * @param colors An array of colors to apply.
     * @param count The number of colors from the array to apply. Must not exceed array size.*/
    public void setColors (int[] colors, int count){
        updateSwatchCount(colors == null ? 0 : count);
        for (int i = 0; i < count; i++) {
            swatches.get(i).setColor(colors[i]);
        }
    }

    /**
     * Sets the colors with an animation out from the center of each color swatch.
     */
    public void setColorsAnimated (int[] colors, int count){
        updateSwatchCount(colors == null ? 0 : count);
        float swatchWidth = getWidth() / (float)count; //TODO manual centers necessary since these may have animated widths changing?
        for (int i = 0; i < count; i++) {
            swatches.get(i).setColorAnimated (swatchWidth / 2, getHeight() / 2, colors[i]);
        }
    }

    /** Sets the colors with an animation starting at the coordinates, relative to this view's position. */
    public void setColorsAnimated (float centerX, float centerY, int...color){
        updateSwatchCount(color == null ? 0 : color.length);
        float swatchWidth = getWidth() / (float)color.length;
        for (int i = 0; i < color.length; i++) {
            swatches.get(i).setColorAnimated(centerX - i * swatchWidth, centerY, color[i]);
        }
    }

}
