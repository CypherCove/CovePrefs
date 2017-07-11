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
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

/**
 * A category with 0 height, which can be used to hide specific dividers of adjacent preferences. Setting a custom
 * layout has no effect on Divider Hider.
 */
public class DividerHider extends PreferenceCategory {
    public DividerHider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.coveprefs_divider_hider);
    }
}
