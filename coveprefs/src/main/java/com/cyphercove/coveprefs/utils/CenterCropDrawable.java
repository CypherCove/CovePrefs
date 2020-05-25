/*
 * Copyright (C) 2006 The Android Open Source Project
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

/* This class contains modified derivative code from the AOSP's android.widget.ImageView class. */
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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/** Wraps a Drawable and draws it with center cropping. This allows any view's background to emulate ImageView's image
 * with {@link android.widget.ImageView.ScaleType#CENTER_CROP}.
 */
@SuppressWarnings("WeakerAccess")
public class CenterCropDrawable extends Drawable {

    private Drawable drawable;
    private Matrix matrix = new Matrix();

    public CenterCropDrawable (Drawable drawable){
        this.drawable = drawable;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        drawable.setBounds(left, top, right, bottom);
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        final int viewWidth = right - left;
        final int viewHeight = bottom - top;
        float scale;
        if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        if (scale != 1f && drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
            if (Build.VERSION.SDK_INT >= 18)
                bitmapDrawable.setMipMap(true);
            bitmapDrawable.setFilterBitmap(true);
        }

        matrix.setTranslate(Math.round((drawableWidth - viewWidth) * 0.5f), Math.round((drawableHeight - viewHeight) * 0.5f));
        matrix.postScale(scale, scale);
    }

    @Override
    public void draw(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(matrix);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        drawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        drawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return drawable.getOpacity();
    }

    /** Sets the drawable as the background of a view with center cropping.
     * @param view The View to apply the background to.
     * @param drawable The Drawable to apply as a background.*/
    public static void centerCropDrawableAsBackground (final View view, final Drawable drawable){
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth == -1 || drawableHeight == -1){
            if (Build.VERSION.SDK_INT < 16)
                view.setBackgroundDrawable(drawable);
            else
                view.setBackground(drawable);
            return;
        }

        CenterCropDrawable wrapper = new CenterCropDrawable(drawable);
        if (Build.VERSION.SDK_INT < 16) {
            view.setBackgroundDrawable(wrapper);
        } else {
            view.setBackground(wrapper);
        }

    }
}
