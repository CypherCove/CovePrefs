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

import androidx.annotation.IdRes;
import androidx.preference.PreferenceViewHolder;
import android.view.View;

public class PreferenceViewHolderWrapper implements AbsViewHolder {
    final PreferenceViewHolder viewHolder;

    public PreferenceViewHolderWrapper(PreferenceViewHolder viewHolder){
        this.viewHolder = viewHolder;
    }

    @Override
    public View findViewById(@IdRes int id) {
        return viewHolder.findViewById(id);
    }

    @Override
    public View baseView() {
        return viewHolder.itemView;
    }
}
