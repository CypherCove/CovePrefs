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

import android.support.annotation.IdRes;
import android.view.View;

public class StandardViewHolder implements AbsViewHolder {
    final View view;
    public StandardViewHolder(View view){
        this.view = view;
    }

    @Override
    public View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    @Override
    public View baseView() {
        return view;
    }
}
