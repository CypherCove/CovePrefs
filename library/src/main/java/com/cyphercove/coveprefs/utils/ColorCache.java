/*******************************************************************************
 * Copyright 2017 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.coveprefs.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.ArrayList;

/**
 * Stores the most recently picked colors from any color pickers in the application for easy re-selection by user.
 */
public class ColorCache {
	public static final String PREFS_NAME = "com.cyphercove.coveprefs.utils.ColorCache";
	
	private static final int COLOR_DEFAULT = Color.BLACK;

	private static final String COUNT_KEY = "count"; // The number of currently saved values.
	private static int count = -1;
	private static final String MAX_COUNT_KEY = "maxCount"; // The number of currently saved values.
	private static final int DEFAULT_MAX_COUNT = 10;
	private static int maximumCount = -1;

	private static ArrayList<Integer> cachedColors;
	
	private static SharedPreferences getSharedPreferences(Context context){
		return context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
	}
	
	private static SharedPreferences.Editor getEditor(Context context){
		return getSharedPreferences(context).edit();
	}

	/** @return the number of colors currently stored. */
	public static int getCount (Context context){
		if (count == -1){ // first time access, look it up
			count = getSharedPreferences(context).getInt(COUNT_KEY, 0);
		}
		return count;
	}

	public static int getMaximumCount (Context context){
		if (maximumCount == -1){ // first time access, look it up
			maximumCount = getSharedPreferences(context).getInt(MAX_COUNT_KEY, DEFAULT_MAX_COUNT);
		}
		return maximumCount;
	}

	public static void setMaximumCount (Context context, int count){
		if (count == maximumCount)
			return;
		maximumCount = count;

		getCachedColors(context); // ensure list is populated
		while (cachedColors.size() >= maximumCount){
			cachedColors.remove(cachedColors.size() - 1);
		}

		getEditor(context)
				.putInt(MAX_COUNT_KEY, maximumCount)
				.putInt(COUNT_KEY, cachedColors.size())
				.commit();
	}

	public static boolean submitColor (Context context, int newColor){
		int[] newColors = new int[1];
		newColors[0] = newColor;
		return submitColor(context, newColors, 1);
	}

	/** Submit colors to the cache. They will be pushed to the front of the cache in reverse order.
	 *
	 * @param valueCount The number of values to submit from the array. Must be no greater than the array size.
	 * @return Whether the list was changed.
	 */
	public static boolean submitColor (Context context, int[] newColors, int valueCount){
		if (newColors == null)
			return false;
		getCachedColors(context); // ensure list is populated

		if (valueCount <= cachedColors.size()) {
			boolean changed = false;
			for (int i = 0; i < valueCount; i++) {
				if (!cachedColors.get(i).equals(newColors[i])){
					changed = true;
					break;
				}
			}
			if (!changed)
				return false;
		}

		for (int i = valueCount - 1; i >= 0; i--) {
			int newColor = newColors[i];
			cachedColors.remove((Integer) newColor); // remove if exists, will move to front
			cachedColors.add(0, newColor); // place at front
		}

		getMaximumCount(context); // ensure loaded
		while (cachedColors.size() >= maximumCount) {
			cachedColors.remove(cachedColors.size() - 1);
		}

		count = cachedColors.size();
		SharedPreferences.Editor editor = getEditor(context);
		for (int i=0; i < count; i++){
			editor.putInt(Integer.toString(i), cachedColors.get(i));
		}
		editor.putInt(COUNT_KEY, count);
		editor.commit();
		return true;
	}
	
	public static ArrayList<Integer> getCachedColors(Context context){
		if (cachedColors == null) { // first access, populate it
			cachedColors = new ArrayList<>();
			SharedPreferences sharedPrefs = getSharedPreferences(context);
			for (int i = 0, count = getCount(context); i < count; i++) {
				int nextColor = sharedPrefs.getInt(Integer.toString(i), COLOR_DEFAULT);
				cachedColors.add(nextColor);
			}
		}
		return cachedColors;
	}

}
