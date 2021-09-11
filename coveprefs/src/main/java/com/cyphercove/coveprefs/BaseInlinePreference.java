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
import android.content.res.TypedArray;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.cyphercove.coveprefs.state.SingleValueSavedState;
import com.cyphercove.coveprefs.utils.PreferenceViewHolderWrapper;
import com.cyphercove.coveprefs.utils.AbsViewHolder;

/**
 * A Preference intended for preference layouts that have a widget for editing built in. There is no need to override
 * {@link #onSetInitialValue(boolean, Object)} but there are some abstract methods that must be filled in. It is still
 * necessary to override {@link Preference#onGetDefaultValue(TypedArray, int)}.
 * <p>
 * The change listener of any widget on the preference layout should call {@link #syncValueFromWidgetChange(Object)} to
 * cause the value to be updated and persisted, provided the preference's change listener (if it exists) accepts it.
 *
 * @param <T> The data type of the preference.
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseInlinePreference<T> extends Preference {

    private T currentValue;
    private boolean valueSet;

    public abstract Class<T> getDataType();

    protected BaseInlinePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (currentValue == null) {
            return superState; // no need to save
        }

        final SingleValueSavedState<T> myState = SingleValueSavedState.create(superState, getDataType());
        myState.setValue(currentValue);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !SingleValueSavedState.isOfCorrectType(state, getDataType())) {
            super.onRestoreInstanceState(state);
            return;
        }

        @SuppressWarnings("unchecked")
        SingleValueSavedState<T> myState = (SingleValueSavedState<T>) state;
        currentValue = myState.getValue();
        super.onRestoreInstanceState(myState.getSuperState());

    }

    public void setValue (T value){
        setValueInternal(value, true);
    }

    private void setValueInternal (T value, boolean notifyChanged){
        final boolean changed = value instanceof CharSequence && currentValue instanceof CharSequence ?
                !TextUtils.equals((CharSequence)currentValue, (CharSequence)value) :
                !value.equals(currentValue);
        if (changed || !valueSet) {
            valueSet = true;
            currentValue = value;
            persistValue(currentValue);
            onValueChanged(currentValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    /** Called when the value has changed. This is a good time to apply the changes to any passive views that show the value.
     * The value has already been persisted when this is called.
     */
    protected abstract void onValueChanged (T newValue);

    /** @return The current preference value. */
    public T getValue (){
        return currentValue;
    }

    /** @return A value suitable for binding to the preference view. This is the currently persisted value. */
    protected T getValueForBindingPreferenceView (){
        return currentValue != null ? currentValue : getDefaultValue();
    }

    /** Call this when the user changes the value using the widget. */
    protected void syncValueFromWidgetChange(T newValue) {
        if (!newValue.equals(currentValue)) {
            if (callChangeListener(newValue)) {
                setValueInternal(newValue, false);
            } else {
                onWidgetValueChangeRejected(currentValue);
            }
        }
    }

    /** Called when a value change on the widget was rejected by the change listener, so the widget's shown value
     * must be manually restored.
     * @param restoreValue The value to set on the widget.
     */
    protected abstract void onWidgetValueChangeRejected (T restoreValue);

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        //noinspection unchecked
        setValue(getPersistedValue((T) defaultValue));
    }

    /** Use {@link #onPreferenceViewCreated(AbsViewHolder)} for obtaining references to preference layout views. */
    @Override
    public final void onBindViewHolder(PreferenceViewHolder holder){
        super.onBindViewHolder(holder);

        onPreferenceViewCreated(new PreferenceViewHolderWrapper(holder));
    }

    /** Called when the preference view has been created. This is a good time for caching references to any custom
     * widgets in the preference layout.
     * @param view A wrapper for the preference view, which can find child views.
     */
    protected void onPreferenceViewCreated (AbsViewHolder view){

    }

    /** Persists the value. */
    protected abstract void persistValue (T value);

    /** @param defaultReturnValue The value to return if the persisted value doesn't exist.
     * @return The persisted value, or {@code defaultReturnValue} if it doesn't exist. */
    protected abstract T getPersistedValue (T defaultReturnValue);

    /** @return A backup default value, in case none was provided via XML. Must not return null.*/
    protected abstract T getDefaultValue();

}
