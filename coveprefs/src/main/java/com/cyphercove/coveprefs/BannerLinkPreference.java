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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import com.cyphercove.coveprefs.utils.CenterCropDrawable;
import com.cyphercove.coveprefs.utils.CovePrefsUtils;

import java.util.Objects;

/**
 * A {@link LinkPreference} that can use a drawable as its background with
 * {@code app:coveprefs_banner}. The drawable is scaled up to fill the size of the preference
 * background, and if it is a BitmapDrawable, it will be filtered and mip-mapped.
 * <p>
 * If the standard ripple is desired, the layout should be set to
 * {@code @layout/coveprefs_preference_banner_link}, and the ConstraintLayout library needs to be in
 * the dependencies of the app module.
 */
@SuppressWarnings("WeakerAccess")
public class BannerLinkPreference extends LinkPreference {

    private Drawable banner;
    private int bannerId;
    private ColorStateList titleColor;
    private ColorStateList summaryColor;

    public BannerLinkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_BannerLinkPreference, 0, defStyleRes);
        bannerId = a.getResourceId(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_banner, 0);
        titleColor = a.getColorStateList(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_titleColor);
        summaryColor = a.getColorStateList(R.styleable.CovePrefs_BannerLinkPreference_coveprefs_summaryColor);
        a.recycle();
    }

    public BannerLinkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BannerLinkPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public BannerLinkPreference(Context context) {
        this(context, null);
    }

    public Drawable getBanner() {
        if (banner == null && bannerId != 0) {
            banner = ContextCompat.getDrawable(getContext(), bannerId);
        }
        return banner;
    }

    public void setBanner(@DrawableRes int bannerId) {
        if (this.bannerId != bannerId) {
            this.banner = null;
            this.bannerId = bannerId;
            notifyChanged();
        }
    }

    public void setBanner(Drawable banner) {
        if (this.banner != banner) {
            this.banner = banner;
            bannerId = 0;
            notifyChanged();
        }
    }

    /**
     * The ColorStateList to override the title's layout color with.
     * @return The color, or null if none is set.
     */
    @Nullable
    public ColorStateList getTitleColor() {
        return titleColor;
    }

    /**
     * Sets a ColorStateList to be used for the title text instead of whatever is specified in the
     * layout.
     * @param titleColor The color to use for the title, or null to use the layout's color.
     */
    public void setTitleColor(@Nullable ColorStateList titleColor) {
        if (!Objects.equals(titleColor, this.titleColor)) {
            this.titleColor = titleColor;
            notifyChanged();
        }
    }

    /**
     * Sets a color RGBA integer to be used for the title text instead of whatever is specified in
     * the layout.
     * @param titleColor The color to use for the title.
     */
    public void setTitleColor(@ColorInt int titleColor) {
        ColorStateList colorStateList = ColorStateList.valueOf(titleColor);
        setTitleColor(colorStateList);
    }

    /**
     * Sets a color or ColorStateList to override the layout's title color.
     * @param id The ID of a color resource to set.
     */
    public void setTitleColorResource(@ColorRes int id) {
        ColorStateList colorStateList = AppCompatResources.getColorStateList(getContext(), id);
        setTitleColor(colorStateList);
    }

    /**
     * The ColorStateList to override the summary's layout color with.
     * @return The color, or null if none is set.
     */
    @Nullable
    public ColorStateList getSummaryColor() {
        return summaryColor;
    }

    /**
     * Sets a ColorStateList to be used for the summary text instead of whatever is specified in the
     * layout.
     * @param summaryColor The color to use for the summary, or null to use the layout's color.
     */
    public void setSummaryColor(@Nullable ColorStateList summaryColor) {
        if (!Objects.equals(summaryColor, this.summaryColor)) {
            this.summaryColor = summaryColor;
            notifyChanged();
        }
    }
    /**
     * Sets a color RGBA integer to be used for the summary text instead of whatever is specified in
     * the layout.
     * @param summaryColor The color to use for the summary.
     */
    public void setSummaryColor(@ColorInt int summaryColor) {
        ColorStateList colorStateList = ColorStateList.valueOf(summaryColor);
        setSummaryColor(colorStateList);
    }

    /**
     * Sets a color or ColorStateList to override the layout's summary color.
     * @param id The ID of a color resource to set.
     */
    public void setSummaryColorResource(@ColorRes int id) {
        ColorStateList colorStateList = AppCompatResources.getColorStateList(getContext(), id);
        setSummaryColor(colorStateList);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final Drawable bannerDrawable = getBanner();
        ImageView bannerView = holder.itemView.findViewById(R.id.coveprefs_banner);
        if (bannerDrawable != null){
            if (bannerDrawable instanceof BitmapDrawable)
                ((BitmapDrawable)bannerDrawable).setGravity(Gravity.CENTER);

            if (bannerView != null) {
                bannerView.setImageDrawable(bannerDrawable);
            } else {
                CenterCropDrawable.centerCropDrawableAsBackground(holder.itemView, bannerDrawable);
            }

            float shadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                    getContext().getResources().getDisplayMetrics());
            TextView titleTextView = holder.itemView.findViewById(android.R.id.title);
            if (titleTextView != null) {
                CovePrefsUtils.setContrastingShadow(titleTextView, shadowRadius);
                if (titleColor != null) {
                    titleTextView.setTextColor(titleColor);
                }
            }
            TextView summaryTextView = holder.itemView.findViewById(android.R.id.summary);
            if (summaryTextView != null) {
                CovePrefsUtils.setContrastingShadow(summaryTextView, shadowRadius);
                if (summaryColor != null) {
                    summaryTextView.setTextColor(summaryColor);
                }
            }
            holder.setDividerAllowedAbove(false);
            holder.setDividerAllowedBelow(false);
        } else {
            if (bannerView != null) {
                bannerView.setImageDrawable(null);
            } else {
                holder.itemView.setBackground(null);
            }
        }
    }

}
