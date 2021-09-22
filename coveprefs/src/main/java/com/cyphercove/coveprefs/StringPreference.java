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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Objects;

/**
 * A DialogPreference that allows the user to type in a String. It is similar to
 * {@link androidx.preference.EditTextPreference}, but it allows specifying a hint for the EditText
 * box and disallowing empty strings.
 */
@SuppressWarnings("WeakerAccess")
public class StringPreference extends BaseDialogPreference<String> {
    private EditText editText;
    private CharSequence editTextHint;
    private boolean allowEmptyString;

    public StringPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_StringPreference);
        if (a.getBoolean(R.styleable.CovePrefs_StringPreference_coveprefs_useSimpleSummaryProvider, false)) {
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }
        editTextHint = a.getText(R.styleable.CovePrefs_StringPreference_coveprefs_editTextHint);
        allowEmptyString = a.getBoolean(R.styleable.CovePrefs_StringPreference_coveprefs_allowEmptyString, true);
        a.recycle();

        setDialogLayoutResource(R.layout.coveprefs_string_dialog);

        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    public StringPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StringPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public StringPreference(Context context) {
        this(context, null);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onValueModifiedInDialog(s.toString());
            setInternalPositiveButtonEnabled(s.length() > 0 || allowEmptyString);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected @NonNull
    String getBackupDefaultValue() {
        return "";
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    public CharSequence getEditTextHint() {
        return editTextHint;
    }

    public void setEditTextHint(CharSequence editTextHint) {
        if (!Objects.equals(editTextHint, this.editText)) {
            this.editTextHint = editTextHint;
            notifyChanged();
        }
    }

    public void setEditTextHint(@StringRes int editTextHint) {
        setEditTextHint(getContext().getText(editTextHint));
    }

    @Override
    protected void onDialogViewCreated(View view) {
        editText = view.findViewById(R.id.coveprefs_editText);
        editText.removeTextChangedListener(textWatcher); //ensure not added multiple times
        editText.addTextChangedListener(textWatcher);
        if (editTextHint != null)
            editText.setHint(editTextHint);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editText.setText(getValueForBindingDialog());
    }

    @Override
    protected void onValueChangedAndCommitted() {
        if (getSummaryProvider() != null)
            notifyChanged(); // This skips the animation so only notify if necessary.
    }

    @Override
    protected void persistValue(String value) {
        persistString(value);
    }

    @Override
    protected String getPersistedValue(String defaultReturnValue) {
        return getPersistedString(defaultReturnValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String value = a.getString(index);
        return value != null ? value : "";
    }

    public static final class SimpleSummaryProvider implements SummaryProvider<StringPreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {
        }

        /**
         * Retrieve a singleton instance of this simple
         * {@link SummaryProvider} implementation.
         *
         * @return a singleton instance of this simple
         * {@link SummaryProvider} implementation
         */
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(StringPreference preference) {
            return preference.getValue();
        }
    }
}
