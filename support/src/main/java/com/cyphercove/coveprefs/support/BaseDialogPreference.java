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
package com.cyphercove.coveprefs.support;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import com.cyphercove.coveprefs.state.SingleValueSavedState;
import com.cyphercove.coveprefs.support.utils.PreferenceViewHolderWrapper;
import com.cyphercove.coveprefs.utils.AbsViewHolder;

/**
 * A DialogPreference set up to restore dialog state on Activity recreation, and automatically persist resources on
 * positive dialog results. There is no need to override {@link #onSetInitialValue(boolean, Object)} or to manually
 * persist the new value when the dialog is closed. It is still necessary to override
 * {@link Preference#onGetDefaultValue(TypedArray, int)}.
 * <p>
 * Capture view references and prepare views from the inflated dialog in {@link #onDialogViewCreated(View)}.
 * <p>
 * It is necessary to call {@link #onValueModifedInDialog(Object)} as the value is changed via the dialog's widget(s).
 * <p>
 * A neutral button can be set on the dialog by calling {@link #setNeutralButtonText(CharSequence)}. Unlike the positive
 * and negative buttons, it does not automatically close the dialog. An OnClickListner can be added to it so it can
 * call a method before dismissing the dialog.
 *
 * @param <T> The data type of the preference.
 */
public abstract class BaseDialogPreference<T> extends DialogPreference {

    private T newValue;
    private T currentValue;
    private boolean valueSet;
    private CharSequence positiveButtonText, negativeButtonText, neutralButtonText;
    private Button neutralButton;
    private boolean usesInternalButtonBar;
    private DialogFragment dialogFragment;

    protected abstract Class<T> getDataType();

    protected BaseDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Causes the dialog's title to be hidden if none was specified in the preference XML. By default, DialogPreferences
     * copy the value of the preference {@code title} into the {@code dialogTitle}. This overrides that behavior.
     */
    protected void hideDialogTitleIfNoneSpecified (){
        // The == getTitle() check is because DialogPreference sets them to the same value if none was specified in the XML.
        if (getDialogTitle() == getTitle() && getDialogIcon() == null) {
            setDialogTitle(null);
        }
    }

    /** Ensure's that the positive button exists by assigning it {@link android.R.string#ok} if positive button text has
     * not been specified. */
    protected void forcePositiveButton (){
        CharSequence positiveText = getPositiveButtonText();
        if (positiveText == null || !"".equals(positiveText.toString()))
            setPositiveButtonText(android.R.string.ok);
    }

    /** Ensure's that the negative button exists by assigning it {@link android.R.string#cancel} if negative button text
     * has not been specified. */
    protected void forceNegativeButton (){
        CharSequence negativeText = getNegativeButtonText();
        if (negativeText == null || !"".equals(negativeText.toString()))
            setNegativeButtonText(android.R.string.cancel);
    }

    /** Hides the native dialog buttons and replaces them with ones in the custom layout. The layout must include
     * {@code @layout/coveprefs_alert_dialog_button_bar}. This must be called after any calls to {@link #forceNegativeButton()}
     * or {@link #forcePositiveButton()}.
     */
    protected void setInternalButtonBar (){
        usesInternalButtonBar = true;
        positiveButtonText = getPositiveButtonText();
        setPositiveButtonText(null);
        negativeButtonText = getNegativeButtonText();
        setNegativeButtonText(null);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (newValue == null || newValue == currentValue) {
            return superState; // no need to save
        }

        final SingleValueSavedState myState = SingleValueSavedState.create(superState, getDataType());
        myState.setValue(newValue);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !SingleValueSavedState.isOfCorrectType(state, getDataType())) {
            super.onRestoreInstanceState(state);
            return;
        }

        SingleValueSavedState myState = (SingleValueSavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        newValue = (T)myState.getValue();
    }

    public void setValue (T value){
        final boolean changed = value instanceof CharSequence && currentValue instanceof CharSequence ?
                !TextUtils.equals((CharSequence)currentValue, (CharSequence)value) :
                !value.equals(currentValue);
        if (changed || !valueSet) {
            currentValue = value;
            valueSet = true;
            persistValue(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    /** Returns the current value. */
    public T getValue (){
        return currentValue;
    }

    /** @return A value suitable for binding to the dialog when it is shown. This is the currently set value, or in
    * the case of a configuration change, it is the last value that was shown on the dialog. */
    protected T getValueForBindingDialog (){
        return newValue != null ? newValue : (currentValue != null ? currentValue : getDefaultValue());
    }

    /** @return A value suitable for binding to the preference view. This is the currently persisted value. */
    protected T getValueForBindingPreferenceView (){
        return currentValue != null ? currentValue : getDefaultValue();
    }

    @Override
    protected final void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            currentValue = this.getPersistedValue(getDefaultValue());
        } else {
            currentValue = (T) defaultValue;
            persistValue(currentValue);
        }
    }

    /** Dismisses the dialog and calls {@link #onDialogClosed(boolean)}.
     * @param positiveResult Whether the values that were changed with {@link #onValueModifedInDialog(Object)} should
     * be commited because the dialog wasn't canceled.*/
    protected void dismissDialog (boolean positiveResult){
        if (dialogFragment != null){
            Dialog dialog = dialogFragment.getDialog();
            if (dialog != null) {
                dialog.dismiss();
                onDialogClosed(positiveResult);
            }
        }
    }

    private void onDialogClosed(boolean positiveResult) {
        if (positiveResult && callChangeListener(newValue)) {
            currentValue = newValue;
            persistValue(newValue);
            onValueChangedAndCommitted();
        }
        newValue = null;
    }

    /**Binds views in the content View of the dialog to data.
     * @param view The root content view of the dialog.*/
    protected void onBindDialogView(View view) {
    }

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

    /** Called when the dialog was closed with positive result, the change listener accepted the change, and the value
     * has been persisted. This is a good time to update any widgets in the preference view.
     */
    protected void onValueChangedAndCommitted (){

    }

    /** Called when the dialog view has just been inflated.
     *
     * @param view The root content view of the dialog. Will be null if {@link #setDialogLayoutResource(int)} has never
     *             been called.
     */
    protected abstract void onDialogViewCreated (View view);

    /** Persists the value. */
    protected abstract void persistValue (T value);

    /** @param defaultReturnValue The value to return if the persisted value doesn't exist.
     * @return The persisted value, or {@code defaultReturnValue} if it doesn't exist. */
    protected abstract T getPersistedValue (T defaultReturnValue);

    /** @return A backup default value, in case none was provided via XML. Must not return null.*/
    protected abstract T getDefaultValue();

    protected void onValueModifedInDialog (T newValue){
        this.newValue = newValue;
    }

    protected void setNeutralButtonText (CharSequence text){
        neutralButtonText = text;
    }

    protected Button getNeutralButton (){
        return neutralButton;
    }

    protected final void setDialogFragment (DialogFragment dialogFragment){
        this.dialogFragment = dialogFragment;
    }

    public static class DialogFragment<T> extends PreferenceDialogFragmentCompat {

        public static DialogFragment newInstance(String key) {
            final DialogFragment
                    fragment = new DialogFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        private BaseDialogPreference<T> getBasePreference (){
            return (BaseDialogPreference<T>) getPreference();
        }

        /** See {@link PreferenceDialogFragmentCompat#onDialogClosed(boolean)}. */
        @Override
        public void onDialogClosed(boolean positiveResult) {
            getBasePreference().onDialogClosed(positiveResult);
            getBasePreference().setDialogFragment(null);
        }

        @Override
        protected View onCreateDialogView(Context context) {
            View view = super.onCreateDialogView(context);
            handleInternalButtonBar(view);
            getBasePreference().setDialogFragment(this);
            getBasePreference().onDialogViewCreated(view);
            return view;
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getBasePreference().onBindDialogView(view);
        }

        private void handleInternalButtonBar (View layoutView){
            BaseDialogPreference<?> baseDialogPreference = getBasePreference();

            if (baseDialogPreference.usesInternalButtonBar){
                boolean shouldHideButtonBar = true;
                if (baseDialogPreference.positiveButtonText != null){
                    shouldHideButtonBar = false;
                    Button positiveButton = (Button)layoutView.findViewById(com.cyphercove.coveprefs.R.id.coveprefs_button1);
                    positiveButton.setText(baseDialogPreference.positiveButtonText);
                    positiveButton.setVisibility(View.VISIBLE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getDialog().dismiss();
                            onDialogClosed(true);
                        }
                    });
                }
                if (baseDialogPreference.negativeButtonText != null){
                    shouldHideButtonBar = false;
                    Button negativeButton = (Button)layoutView.findViewById(com.cyphercove.coveprefs.R.id.coveprefs_button2);
                    negativeButton.setText(baseDialogPreference.negativeButtonText);
                    negativeButton.setVisibility(View.VISIBLE);
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getDialog().dismiss();
                            onDialogClosed(false);
                        }
                    });
                }
                if (baseDialogPreference.neutralButtonText != null){
                    shouldHideButtonBar = false;
                    baseDialogPreference.neutralButton = (Button)layoutView.findViewById(com.cyphercove.coveprefs.R.id.coveprefs_button3);
                    baseDialogPreference.neutralButton.setText(baseDialogPreference.neutralButtonText);
                    baseDialogPreference.neutralButton.setVisibility(View.VISIBLE);
                }
                if (shouldHideButtonBar)
                    layoutView.findViewById(R.id.coveprefs_buttonPanel).setVisibility(View.GONE);
            }
        }
    }

}
