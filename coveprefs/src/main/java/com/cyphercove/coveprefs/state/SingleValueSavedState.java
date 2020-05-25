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
package com.cyphercove.coveprefs.state;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <T> The data type of the save state
 */
public abstract class SingleValueSavedState<T> extends Preference.BaseSavedState {

    private static final Map<Class, Class> DATA_TYPES_TO_STATES_TYPES = new HashMap<>();
    static {
        DATA_TYPES_TO_STATES_TYPES.put(Integer.class, IntegerSavedState.class);
        DATA_TYPES_TO_STATES_TYPES.put(String.class, StringSavedState.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> SingleValueSavedState<T> create (Parcelable superState, Class<T> dataType){
        Class<?> stateType = DATA_TYPES_TO_STATES_TYPES.get(dataType);
        if (stateType == null)
            throw new RuntimeException("Unsupported data type: " + dataType.getName());

        try {
            Constructor<?> constructor = stateType.getConstructor(Parcelable.class);
            return (SingleValueSavedState<T>)constructor.newInstance(superState);
        } catch (Exception e ) {
            throw new RuntimeException("Saved state type: " + stateType.getName() +
                    " must have a public constructor that takes a Parcelable.", e);
        }

    }

    public static boolean isOfCorrectType (Parcelable state, Class dataType){
        Class stateType = DATA_TYPES_TO_STATES_TYPES.get(dataType);
        if (stateType == null)
            throw new RuntimeException("Unsupported data type: " + dataType.getName());

        return state.getClass().equals(stateType);
    }

    SingleValueSavedState (Parcel source){
        super(source);
    }

    SingleValueSavedState(Parcelable superState) {
        super(superState);
    }

    public abstract T getValue();
    public abstract void setValue(T newValue);

}
