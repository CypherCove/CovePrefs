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
package com.cyphercove.coveprefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.cyphercove.coveprefs.utils.CenterCropDrawable;
import com.cyphercove.coveprefs.utils.ColorUtils;

/**
 * A Preference that can use a drawable as the background with {@code app:coveprefs_banner}. The drawable is scaled up
 * to fill the size of the preference background, and if it is a BitmapDrawable, it will be filtered and mip-mapped. As
 * a convenience, setting {@code app:coveprefs_uri} will set a {@link android.content.Intent#ACTION_VIEW} Intent with
 * that uri, without the need to specify an {@code <intent>} in the xml.
 * <p>
 * If a key is set for the preference, it will write a boolean of true when it is first clicked, to indicate that at
 * least one click has ever occurred on it. This could be used to hide the banner permanently after the first time
 * the user clicks it.
 * <p>
 * {@code app:coveprefs_titleColor} and {@code app:coveprefs_summaryColor} can be used as a shortcut to styling the text
 * colors.
 * <p>
 * Setting a custom layout via {@code android:layout} in xml has no effect on BannerLinkPreference.
 */
public class BannerLinkPreference extends Preference {

    private String uri;
    private int bannerId;
    private ColorStateList titleColor, summaryColor;

    public BannerLinkPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BannerLinkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public BannerLinkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_BannerLinkPreference);
        uri = a.getString(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_uri);
        bannerId = a.getResourceId(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_banner, 0);
        titleColor = a.getColorStateList(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_titleColor);
        summaryColor = a.getColorStateList(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_summaryColor);
        a.recycle();

        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            setIntent(intent);
        }

    }

    protected void onClick() {
        persistBoolean(true);
    }

    @Override
    protected final void onBindView(View view) {
        super.onBindView(view);
        if (bannerId != 0) {
            Drawable drawable;
            if (Build.VERSION.SDK_INT < 21)
                drawable = getContext().getResources().getDrawable(bannerId);
            else
                drawable = getContext().getDrawable(bannerId);

            if (drawable instanceof BitmapDrawable)
                ((BitmapDrawable) drawable).setGravity(Gravity.CENTER);

            CenterCropDrawable.centerCropDrawableAsBackground(view, drawable);

            float shadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                    getContext().getResources().getDisplayMetrics());
            TextView titleTextView = (TextView) view.findViewById(android.R.id.title);
            if (titleTextView != null)
                ColorUtils.addContrastingShadow(titleTextView, shadowRadius);
            TextView summaryTextView = (TextView) view.findViewById(android.R.id.summary);
            if (summaryTextView != null)
                ColorUtils.addContrastingShadow(summaryTextView, shadowRadius);
        }
        if (titleColor != null){
            TextView titleTextView = (TextView) view.findViewById(android.R.id.title);
            if (titleTextView != null)
                titleTextView.setTextColor(titleColor);
        }
        if (summaryColor != null){
            TextView summaryTextView = (TextView) view.findViewById(android.R.id.summary);
            if (summaryTextView != null)
                summaryTextView.setTextColor(summaryColor);
        }
    }
}
