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
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * Shows top and bottom scroll indicator lines, acting as a visual backport of {@code android:scrollIndicators="top|bottom"}
 * for APIs before 23. Not designed for programmatic manipulation of child views. Add a single child view in XML. If the
 * child is not a ListView or GridView, the indicators don't update automatically after a fling.
 */
public class ScrollIndicatorsViewWrapper extends FrameLayout {
    public static final int INDICATOR_COLOR = 0x80808080;
    public static final int LINE_HEIGHT_DIP = 1;
    private View topLine, bottomLine, content;

    public ScrollIndicatorsViewWrapper(Context context) {
        super(context);
        init();
    }

    public ScrollIndicatorsViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollIndicatorsViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(21)
    public ScrollIndicatorsViewWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        if (Build.VERSION.SDK_INT >= 23)
            return;
        int lineHeight =
                Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LINE_HEIGHT_DIP, getResources().getDisplayMetrics()));
        topLine = new View(getContext());
        topLine.setBackgroundColor(INDICATOR_COLOR);
        topLine.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight, Gravity.TOP));
        bottomLine = new View(getContext());
        bottomLine.setBackgroundColor(INDICATOR_COLOR);
        bottomLine.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight, Gravity.BOTTOM));
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child == topLine || child == bottomLine) {
            super.addView(child, index, params);
            return;
        } else if (Build.VERSION.SDK_INT >= 23) {
            super.addView(child, index, params);
            child.setScrollIndicators(SCROLL_INDICATOR_TOP | SCROLL_INDICATOR_BOTTOM);
            return;
        }

        removeAllViews();
        super.addView(child, index, params);
        addView(topLine);
        addView(bottomLine);
        content = child;

        if (content instanceof AbsListView) {
            ((AbsListView) content).setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    updateScrollIndicators();
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    updateScrollIndicators();
                }
            });
        } else {
            content.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        updateScrollIndicators();
                    }
                    return false;
                }
            });
        }

    }

    public void updateScrollIndicators() {
        topLine.setVisibility(content.canScrollVertically(-1) ? View.VISIBLE : View.INVISIBLE);
        bottomLine.setVisibility(content.canScrollVertically(1) ? View.VISIBLE : View.INVISIBLE);
    }
}
