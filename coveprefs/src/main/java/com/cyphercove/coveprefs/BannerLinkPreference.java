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
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.cyphercove.coveprefs.utils.CenterCropDrawable;
import com.cyphercove.coveprefs.utils.CovePrefsUtils;

/**
 * A Preference that can use a drawable as the background with {@code app:coveprefs_banner}. The drawable is scaled up
 * to fill the size of the preference background, and if it is a BitmapDrawable, it will be filtered and mip-mapped. As
 * a convenience, setting {@code app:coveprefs_uri} will set a {@link android.content.Intent#ACTION_VIEW} Intent with
 * that uri, without the need to specify an {@code <intent>} in the xml.
 * <p>
 * Setting a custom layout via {@code android:layout} in xml has no effect on BannerLinkPreference.
 */
@SuppressWarnings("WeakerAccess")
public class BannerLinkPreference extends Preference {

    private int bannerId;

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

    protected void init (Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, com.cyphercove.coveprefs.R.styleable.CovePrefs_BannerLinkPreference);
        String uri = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_BannerLinkPreference_coveprefs_uri);
        String backupUri = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_BannerLinkPreference_coveprefs_backupUri);
        String uriFormatArg = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_BannerLinkPreference_coveprefs_uriFormatArg);
        bannerId = a.getResourceId(com.cyphercove.coveprefs.R.styleable.CovePrefs_BannerLinkPreference_coveprefs_banner, 0);
        a.recycle();

        Intent intent = CovePrefsUtils.resolveIntent(context, uriFormatArg, uri, backupUri);
        if (intent != null){
            setIntent(intent);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (bannerId != 0){
            Drawable drawable = ContextCompat.getDrawable(getContext(), bannerId);

            if (drawable instanceof BitmapDrawable)
                ((BitmapDrawable)drawable).setGravity(Gravity.CENTER);

            ImageView bannerView = holder.itemView.findViewById(R.id.coveprefs_banner);
            if (bannerView != null) {
                bannerView.setImageDrawable(drawable);
            } else {
                if (drawable != null) {
                    CenterCropDrawable.centerCropDrawableAsBackground(holder.itemView, drawable);
                } else {
                    holder.itemView.setBackgroundDrawable(null);
                }
            }

            float shadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                    getContext().getResources().getDisplayMetrics());
            TextView titleTextView = holder.itemView.findViewById(android.R.id.title);
            if (titleTextView != null)
                CovePrefsUtils.setContrastingShadow(titleTextView, shadowRadius);
            TextView summaryTextView = holder.itemView.findViewById(android.R.id.summary);
            if (summaryTextView != null)
                CovePrefsUtils.setContrastingShadow(summaryTextView, shadowRadius);

            holder.setDividerAllowedAbove(false);
            holder.setDividerAllowedBelow(false);
        }
    }

}
