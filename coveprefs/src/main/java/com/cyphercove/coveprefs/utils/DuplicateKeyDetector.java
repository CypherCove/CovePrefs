/*
 * Copyright (C) 2020 Cypher Cove, LLC
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

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Finds string resources with the same value. This can be used to ensure there are no duplicate
 * preference keys if they are stored as String resources. Override {@link #isKeyResource(String)}
 * to define the predicate for determining whether a String resource is a preference key. By default,
 * the resource name is determined to be a preference key if it starts with "key".
 */
public class DuplicateKeyDetector {

    /**
     * Finds String resources in the given resource class that appear more than once.
     * @param context The application context
     * @param stringResources The class of the String resources, e.g. {@code R.string.class}.
     * @return A map of String key values that appear more than once to the number of times they
     * appear.
     */
    public final Map<String, Integer> findDuplicateKeys (Context context, Class<?> stringResources) {
        Resources resources = context.getResources();
        Map<String, Integer> countByKeys = new HashMap<>();
        for (Field field : stringResources.getFields()) {
            String resourceName = field.getName();
            if (isKeyResource(resourceName)) {
                try {
                    String key = resources.getString(field.getInt(null));
                    Integer count = countByKeys.get(key);
                    count = count == null ? 1 : count + 1;
                    countByKeys.put(key, count);
                } catch (IllegalAccessException e) {
                    Log.w("DuplicateKeyDetection", "Failed to access field " + field);
                }
            }
        }
        for(Iterator<Map.Entry<String, Integer>> it = countByKeys.entrySet().iterator(); it.hasNext(); ) {
            Integer count = it.next().getValue();
            if(count == 1) {
                it.remove();
            }
        }
        return countByKeys;
    }

    /**
     * Determines whether a resource with the given name should be considered to represent a key.
     * @param resourceName Name of the String resource
     * @return True if the associated resource value is a key.
     */
    protected boolean isKeyResource(String resourceName) {
        return resourceName.startsWith("key");
    }
}
