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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.SparseIntArray;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * A vector of Colors representing multiple color choices corresponding with a {@link MultiColor.Definition}.
 * <p>
 * This class is mutable, and is intrinsically tied to its definition. Its type and number of colors
 * must comply with the definition.
 * <p>
 * When the type is changed to one with a shorter vector, excess values are kept and restored if the
 * type is later changed to a larger vector.
 */
public class MultiColor {

    /**
     * Defines the possible values for a MultiColor, and String labels for them to be shown in the MultiColorPicker.
     * Contract: the lengths of the arrays must be the same for all resource configurations.
     */
    public static class Definition {

        @SuppressLint("StaticFieldLeak")
        public static final Definition DEFAULT = new Definition();

        private final Context context;
        private final int resId;
        private final int disabledLabelResId;
        private final SparseIntArray typeLengths = new SparseIntArray();
        private int typeCount = -1;
        private int maxColors = -1;
        private int disabledStateTypeIndex = -2;

        /**
         * Legacy, used only for backup.
         */
        private final String disabledLabel; // label text shown for types with 0 values

        /**
         * @param context The application context
         * @param resId   A resource ID for an array of String arrays. Each String array represents a multi-color type
         *                (by index) and the length of each array is the number of colors it can hold. The values of
         *                the String array are used for logging in MultiColor, but they are also used by MultiColorPreference
         *                for labeling the member types.
         */
        public Definition (Context context, @ArrayRes int resId) {
            this(context, resId, -1);
        }

        /**
         * @param context            The application context
         * @param resId              A resource ID for an array of String arrays. Each String array represents a multi-color type
         *                           (by index) and the length of each array is the number of colors it can hold. The values of
         *                           the String array are used for logging in MultiColor, but they are also used by MultiColorPreference
         *                           for labeling the member types.
         * @param disabledLabelResId A String resource that is shown for types that have zero values.
         *                           Optional, a negative value may be passed to omit it.
         */
        public Definition (Context context, @ArrayRes int resId, @StringRes int disabledLabelResId) {
            this.context = context;
            this.resId = resId;
            this.disabledLabelResId = disabledLabelResId;
            disabledLabel = "";
        }

        /**
         * @param context       The application context
         * @param resId         A resource ID for an array of String arrays. Each String array represents a multi-color type
         *                      (by index) and the length of each array is the number of colors it can hold. The values of
         *                      the String array are used for logging in MultiColor, but they are also used by MultiColorPreference
         *                      for labeling the member types.
         * @param disabledLabel A String that is shown for types that have zero values. Optional.
         * @deprecated The disabled label should be specified with a resource identifier in case the
         * configuration changes. Use {@code Definition(Context, int, int)} instead.
         */
        public Definition (Context context, @ArrayRes int resId, @Nullable String disabledLabel) {
            this.context = context;
            this.resId = resId;
            this.disabledLabelResId = 0;
            this.disabledLabel = disabledLabel == null ? "" : disabledLabel;
        }

        /**
         * Constructor for the default, a single type of zero length (disabled), unlabeled.
         */
        private Definition () {
            this.context = null;
            this.resId = 0;
            this.disabledLabelResId = 0;
            this.disabledLabel = "";
            typeCount = 1;
            maxColors = 0;
        }

        /**
         * Returns the String labels for the array of colors for the given color type.
         *
         * @param type The color type index corresponding with the n-th array in the defining
         *             array of arrays.
         * @return An array of Strings for labeling each of the colors in this color type.
         * @throws IndexOutOfBoundsException if the type is out of range of this definition.
         */
        @NonNull
        public String[] getLabels (int type) {
            if (context == null || resId <= 0) {
                String[] def = new String[1];
                def[0] = "";
                return def;
            }
            TypedArray definitionArray = context.getResources().obtainTypedArray(resId);
            int memberId = definitionArray.getResourceId(type, 0);
            definitionArray.recycle();
            if (memberId == 0)
                throw new IndexOutOfBoundsException("The type " + type + " is not in the definition.");
            String[] labels = context.getResources().getStringArray(memberId);
            typeLengths.put(type, labels.length);
            return labels;
        }

        /**
         * Returns the label used for disabled state. This is used for any zero-length array in the
         * defining arrays.
         *
         * @return The label to use for disabled state, or an empty String if there is none.
         */
        @NonNull
        public String getDisabledLabel () {
            if (context == null || disabledLabelResId <= 0) {
                return disabledLabel;
            }
            return context.getString(disabledLabelResId);
        }

        /**
         * Converts a String preference value representing a MultiColor into a MultiColor instance,
         * based on this Definition.
         *
         * @param preferenceValue A String preference value that can be converted to a MultiColor.
         * @return A new MultiColor, evaluated for this definition and the given preference value.
         */
        @NonNull
        public MultiColor getValue (@NonNull String preferenceValue) {
            return new MultiColor(this, preferenceValue);
        }

        /**
         * Converts a String preference value representing a MultiColor into a MultiColor instance,
         * based on this Definition.
         *
         * @param preferenceValue A String preference value that can be converted to a MultiColor.
         * @return A new MultiColor, evaluated for this definition and the given preference value.
         * @deprecated A temporary value is no longer used. Use {@link #getValue(String)} instead.
         */
        @NonNull
        public MultiColor getTemporaryValue (@NonNull String preferenceValue) {
            return getValue(preferenceValue);
        }

        /**
         * Interprets the given color type and color values as a MultiColor based on this
         * Definition.
         *
         * @param type   The type of the MultiColor, as defined in the {@linkplain Definition}.
         * @param values The color values for the type.
         * @return A new MultiColor, evaluated for this definition and the type and colors.
         */
        @NonNull
        public MultiColor getValue (int type, @NonNull int[] values) {
            return new MultiColor(this, type, values);
        }

        /**
         * Interprets the given color type and color values as a MultiColor based on this
         * Definition.
         *
         * @param type   The type of the MultiColor, as defined in the {@linkplain Definition}.
         * @param values The color values for the type.
         * @return A new MultiColor, evaluated for this definition and the type and colors.
         * @deprecated A temporary value is no longer used. Use {@link #getValue(int, int[])} instead.
         */
        @NonNull
        public MultiColor getTemporaryValue (int type, @NonNull int[] values) {
            return getValue(type, values);
        }

        /**
         * @return the number of color types defined.
         */
        public int getTypeCount () {
            if (typeCount >= 0)
                return typeCount;
            if (context == null || resId <= 0)
                return 1;
            TypedArray definitionArray = context.getResources().obtainTypedArray(resId);
            typeCount = definitionArray.length();
            definitionArray.recycle();
            return typeCount;
        }

        /**
         * @param type The color type index corresponding with the n-th array in the defining array
         *             of arrays.
         * @return number of values for the given color type.
         * @throws IndexOutOfBoundsException if the type is out of range of this definition.
         */
        public int getValueCount (int type) {
            int knownCount = typeLengths.get(type, -1);
            if (knownCount >= 0)
                return knownCount;
            return getLabels(type).length;
        }

        /**
         * @return The highest number of values accepted for any type.
         */
        public int getMaxColors () {
            if (maxColors >= 0)
                return maxColors;
            final int typeCount = getTypeCount();
            maxColors = 0;
            for (int i = 0; i < typeCount; i++) {
                maxColors = Math.max(maxColors, getValueCount(i));
            }
            return maxColors;
        }

        /**
         * @return The first type index with a length of zero, meaning it is a disabled state, or
         * -1 if there is no disabled state in the definition.
         */
        public int getDisabledStateTypeIndex () {
            if (disabledStateTypeIndex > -2)
                return disabledStateTypeIndex;

            final int typeCount = getTypeCount();
            for (int i = 0; i < typeCount; i++) {
                if (getValueCount(i) == 0) {
                    disabledStateTypeIndex = i;
                    return i;
                }
            }
            disabledStateTypeIndex = -1;
            return -1;
        }
    }

    public final Definition definition;
    private int type;
    /**
     * Is the size of the biggest type in the definition. This allows the extra color values to be remembered when
     * swiping between types.
     */
    private final int[] values;

    public MultiColor (@NonNull Definition definition) {
        this.definition = definition;
        values = new int[definition.getMaxColors()];
        for (int i = 0; i < values.length; i++) {
            values[i] = Color.BLACK;
        }
    }

    public MultiColor (@NonNull Definition definition, int type, @Nullable int... values) {
        this(definition);
        set(type, values);
    }

    public MultiColor (@NonNull Definition definition, @NonNull String preferenceValue) {
        this(definition);
        set(preferenceValue);
    }

    public MultiColor (@NonNull MultiColor multiColor) {
        this(multiColor.definition);
        this.type = multiColor.type;
        System.arraycopy(multiColor.values, 0, this.values, 0, this.values.length);
    }

    /**
     * Set the MultiColor to a specific type and give it a specific color vector.
     * @param type The index of the color type.
     * @param values The vector of color values for the type. If the vector is shorter than the
     *               maximum vector length for this MultiColor's Definition, excess colors are
     *               not modified.
     */
    public void set (int type, @Nullable int... values) {
        setType(type);
        if (values == null)
            values = new int[0];
        System.arraycopy(values, 0, this.values, 0, Math.min(this.values.length, values.length));
    }

    /**
     * Read the given String back into a MultiColor. It should be a String formerly produced using
     * {@link #toPreferenceValue()}. If the String is improperly formatted, the type will be set to
     * 0 and the color values will all be set to {@link Color#GRAY}.
     *
     * @param preferenceValue A String containing a formatted MultiColor.
     * @throws IllegalArgumentException If the preference value cannot be interpreted from the given
     * String.
     */
    public void set (@NonNull String preferenceValue) {
        preferenceValue = preferenceValue.replace("#", ""); // In case of manually typed values with optional hash marks
        String[] elements = preferenceValue.split("\\s+");
        try {
            int type = Integer.parseInt(elements[0]);
            int[] parsedElements = new int[elements.length - 1];
            for (int i = 0; i < parsedElements.length; i++) {
                parsedElements[i] = (int) Long.parseLong(elements[i + 1], 16);
            }
            // Finish all parsing before setting values in case of invalid input.
            this.type = type;
            for (int i = 0; i < values.length; i++) {
                if (i >= parsedElements.length)
                    values[i] = Color.BLACK;
                else {
                    values[i] = parsedElements[i];
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Color value \"%1$s\" is invalid.", preferenceValue));
        }
    }

    /**
     * Set the MultiColor to a specific type. The color values are not modified.
     * @param type The index of the color type.
     */
    public void setType (int type) {
        if (type < 0 || type >= definition.getTypeCount())
            throw new IllegalArgumentException("The type " + type + " is an invalid value for the definition \n" + definition);
        this.type = type;
    }

    /**
     * @return The values of each color in the type. The length of the array may be higher than the size of the type.
     */
    public int[] getValues () {
        return values;
    }

    /**
     * @return The index of the color type.
     */
    public int getType () {
        return type;
    }

    /**
     * @return The number of values defined for the current type.
     */
    public int getValueCount () {
        return definition.getValueCount(type);
    }

    /**
     * @return Whether the current type is for a disabled state.
     */
    public boolean isDisabled () {
        return definition.getValueCount(type) == 0;
    }

    /**
     * Converts the MultiColor to a String suitable for storing as a Preference value.
     *
     * @return A String containing the type and colors. It can be converted back using {@link #set(String)}.
     */
    @NonNull
    public String toPreferenceValue () {
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

    /**
     * Create a String suitable for storing a MultiColor as a Preference value without checking it
     * against any {@link Definition}.
     * @param type The index of the color type.
     * @param colors The vector of color values for the type.
     * @return  A String containing the type and colors. It can be converted back using {@link #set(String)}
     * on a MultiColor with an appropriate Definition.
     */
    public static String preferenceValueOf (int type, int... colors){
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (colors == null || colors.length == 0)
            return builder.toString();
        builder.append(" ");
        for (int i = 0; i < colors.length; i++) {
            builder.append(Integer.toHexString(colors[i]));
            if (i < colors.length - 1)
                builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append("[ Type: ");
        builder.append(type);
        if (values.length > 0)
            builder.append(", Values: ");
        int valueCount = definition.getValueCount(type);
        for (int i = 0; i < valueCount; i++) {
            builder.append(Integer.toHexString(values[i]));
            if (i < values.length - 1)
                builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

}
