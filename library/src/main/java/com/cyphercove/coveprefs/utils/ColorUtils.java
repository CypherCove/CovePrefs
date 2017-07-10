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
package com.cyphercove.coveprefs.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.widget.TextView;

/**
 */
public class ColorUtils {

    public static void addContrastingShadow (TextView textView, float radiusPx){
        if (radiusPx == 0)
            return;
        ColorStateList colorStateList = textView.getTextColors();
        int enabledColor = colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0xFF808080);
        float[] hsv = new float[3];
        Color.colorToHSV(enabledColor, hsv);
        int contrastColor = hsv[2] > 0.5f ? Color.BLACK : Color.WHITE;
        textView.setShadowLayer(radiusPx, 0, 0, contrastColor);
    }
}
