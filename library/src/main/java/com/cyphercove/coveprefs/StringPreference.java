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

import androidx.core.content.res.TypedArrayUtils;

import com.cyphercove.coveprefs.utils.AbsViewHolder;
import com.cyphercove.coveprefs.widgets.RotaryPicker;
import com.cyphercove.coveprefs.widgets.RotaryPreferenceWidget;

/**
 * A DialogPreference that allows the user to type in a String.
 */
public class StringPreference extends BaseDialogPreference<String> implements TextWatcher {
    private EditText editText;
    private String editTextHint;
    private boolean disallowEmptyString;

    public StringPreference (Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CovePrefs_StringPreference);
        if (TypedArrayUtils.getBoolean(a, R.styleable.CovePrefs_StringPreference_useSimpleSummaryProvider,
                R.styleable.CovePrefs_StringPreference_useSimpleSummaryProvider, false)){
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }
        editTextHint = TypedArrayUtils.getString(a, R.styleable.CovePrefs_StringPreference_coveprefs_editTextHint,
                R.styleable.CovePrefs_StringPreference_coveprefs_editTextHint);
        disallowEmptyString = TypedArrayUtils.getBoolean(a, R.styleable.CovePrefs_StringPreference_coveprefs_allowEmptyString,
                R.styleable.CovePrefs_StringPreference_coveprefs_allowEmptyString, true);
        a.recycle();

        setDialogLayoutResource(R.layout.coveprefs_string_dialog);

        forcePositiveButton();
        forceNegativeButton();
        setInternalButtonBar();
    }

    @Override
    protected String getBackupDefaultValue() {
        return "";
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    protected void onDialogViewCreated(View view) {
        editText = view.findViewById(R.id.coveprefs_editText);
        editText.removeTextChangedListener(this); //ensure not added multiple times
        editText.addTextChangedListener(this);
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

    @Override
    public void beforeTextChanged (CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged (CharSequence s, int start, int before, int count) {
        onValueModifedInDialog(s.toString());
        setInternalPositiveButtonEnabled(s.length() > 0 || !disallowEmptyString);
    }

    @Override
    public void afterTextChanged (Editable s) {

    }

    public static final class SimpleSummaryProvider implements SummaryProvider<StringPreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

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
