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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

/**
 */
public class MultiColor {

    /**
     * Defines the possible values for a MultiColor, and String labels for them to be shown in the MultiColorPicker.
     */
    public static class Definition {

        public static final Definition DEFAULT = new Definition();

        final String[][] value;
        final String disabledLabel; // label text shown for types with 0 values
        private MultiColor temp;

        /**
         * @param context       The application context
         * @param resID         A resource ID for an array of String arrays. Each String array represents a multi-color type
         *                      (by index) and the length of each array is the number of colors it can hold. The values of
         *                      the String array are used for logging in MultiColor, but they are also used by MultiColorPreference
         *                      for labeling the member types.
         * @param disabledLabel A String that is shown for types that have zero values. Optional.
         */
        public Definition(Context context, int resID, String disabledLabel) {
            Resources resources = context.getResources();
            TypedArray definitionArray;
            definitionArray = resources.obtainTypedArray(resID);
            value = new String[definitionArray.length()][];
            for (int i = 0; i < value.length; i++) {
                int memberID = definitionArray.getResourceId(i, 0);
                if (memberID != 0) {
                    value[i] = resources.getStringArray(memberID);
                } else {
                    throw new IllegalArgumentException("Invalid MultiColor Definition. Member " + i +
                            " of the array resource is not a String array or could not be found.");
                }
            }

            this.disabledLabel = disabledLabel == null ? "" : disabledLabel;
        }

        /**
         * Constructor for the default, a single type with a single unlabeled value.
         */
        private Definition() {
            value = new String[1][1];
            value[0][0] = "";
            disabledLabel = "";
        }

        public String[] getLabels(int type) {
            return value[type];
        }

        public String getDisabledLabel (){
            return disabledLabel;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length; i++) {
                String[] array = value[i];
                builder.append("[ Type: " + i + ", Values: (");
                for (int j = 0; j < array.length; j++) {
                    String name = array[j];
                    builder.append(name.equals("") ? "[emptyString]" : name);
                    if (j < array.length - 1)
                        builder.append(", ");
                }
                builder.append(") ]");
            }
            return builder.toString();
        }

        /**
         * @param preferenceValue A String preference value that can be converted to a MultiColor.
         * @return A temporary MultiColor, evaluated for this definition and the given preference value. Do not store
         * the value, because the object is reused for subsequent calls.
         */
        public MultiColor getTemporaryValue(String preferenceValue) {
            if (temp == null)
                temp = new MultiColor(this, preferenceValue);
            else
                temp.set(preferenceValue);
            return temp;
        }

        /**
         * @param type The type of the MultiColor, as defined in the {@linkplain Definition}.
         * @param values The color values for the type.
         * @return A temporary MultiColor, evaluated for this definition and the type and colors. Do not store
         * the value, because the object is reused for subsequent calls.
         */
        public MultiColor getTemporaryValue(int type, int[] values) {
            if (temp == null)
                temp = new MultiColor(this, type, values);
            else
                temp.set(type, values);
            return temp;
        }

        public int getTypeCount() {
            return value.length;
        }

        public int getValueCount (int type){
            return value[type].length;
        }

        /**
         * @return The highest number of values accepted for any type.
         */
        public int getMaxColors() {
            int maxCount = 0;
            for (int i = 0; i < value.length; i++) {
                maxCount = Math.max(maxCount, value[i].length);
            }
            return maxCount;
        }
    }

    public final Definition definition;
    private int type;
    /** Is the size of the biggest type in the definition. This allows the extra color values to be remembered when
     * swiping between types.
     */
    private final int[] values;

    public MultiColor(Definition definition){
        this.definition = definition;
        values = new int[definition.getMaxColors()];
        for (int i = 0; i < values.length; i++) {
            values[i] = Color.BLACK;
        }
    }

    public MultiColor(Definition definition, int type, int[] values) {
        this(definition);
        set(type, values);
    }

    public MultiColor(Definition definition, String preferenceValue) {
        this(definition);
        set(preferenceValue);
    }

    public MultiColor (MultiColor multiColor){
        this(multiColor.definition);
        this.type = multiColor.type;
        System.arraycopy(multiColor.values, 0, this.values, 0,this.values.length);
    }

    public void set(int type, int[] values) {
        setType(type);

        int providedValueCount = values == null ? 0 : values.length;
        System.arraycopy(values, 0, this.values, 0, Math.min(values.length, providedValueCount));
    }

    public void set(String preferenceValue) {
        preferenceValue = preferenceValue.replace("#", ""); // In case of manually typed values with optional hash marks
        String[] elements = preferenceValue.split("\\s+");
        try {
            type = Integer.parseInt(elements[0]);
            for (int i = 0, count = Math.min(values.length, elements.length - 1); i < count; i++) {
                if (i >= elements.length)
                    values[i] = Color.BLACK;
                else {
                    values[i] = (int) Long.parseLong(elements[i + 1], 16);
                }
            }
        } catch (NumberFormatException e){
            Log.e("MultiColor", String.format("Color value \"%1$s\" is invalid. Setting MultiColor to type 0 and all values to gray.", preferenceValue));
            type = 0;
            Arrays.fill(values, Color.GRAY);
            return;
        }
    }

    public void setType (int type){
        if (type < 0 || type >= definition.value.length)
            throw new IllegalArgumentException("The type " + type + " is an invalid value for the definition \n" + definition);
        this.type = type;
    }

    /** @return The values of each color in the type. The length of the array may be higher than the size of the type. */
    public int[] getValues() {
        return values;
    }

    public int getType() {
        return type;
    }

    /** @return The number of values defined for the current type. */
    public int getValueCount(){
        return definition.value[type].length;
    }

    public String toPreferenceValue() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (values.length > 0)
            builder.append(" ");
        for (int i = 0; i < values.length; i++) {
            builder.append(Integer.toHexString(values[i]));
            if (i < values.length - 1)
                builder.append(" ");
        }
        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ Type: " + type);
        if (values.length > 0)
            builder.append(", ");
        for (int i = 0; i < definition.value[type].length; i++) {
            builder.append(definition.value[type][i]);
            builder.append(": " + Integer.toHexString(values[i]));
            if (i < values.length - 1)
                builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

}
