package com.cyphercove.coveprefs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * A DialogPreference intended for use to show information about_message the application. The {@code android:dialogTitle}
 * is passed through {@link String#format(Locale, String, Object...)} using the {@code versionName} from the manifest,
 * so it can conveniently show the version string, for example, "MyApp 1.0.1". The {@code android:dialogMessage}
 * is formatted as Html using {@link Html#fromHtml(String)} so it can contain clickable hyperlinks to web sites.
 */
public class AboutPreference extends BaseDialogPreference<String> {

    private static final String KEY = "com.cyphercove.coveprefs.AboutPreferences";

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setDialogLayoutResource(R.layout.coveprefs_about_dialog);

        Locale locale;
        try {
            locale = context.getResources().getConfiguration().locale;
            String versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
            setDialogTitle(String.format(locale, getDialogTitle().toString(), versionName));
        } catch (NullPointerException e){ // occurs in XML editor
        } catch (PackageManager.NameNotFoundException e){ // occurs in XML editor
        }

        if (getKey() == null)
            setKey(KEY); // A key is required so the dialog will restore state after a configuration change
        if (getDialogMessage() != null) {
            if (Build.VERSION.SDK_INT < 24)
                setDialogMessage(Html.fromHtml(getDialogMessage().toString()));
            else
                setDialogMessage(Html.fromHtml(getDialogMessage().toString(), 0));
        }

        setNegativeButtonText(null);
    }

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public AboutPreference(Context context) {
        this(context, null);
    }

    @Override
    public Class<String> getDataType() {
        return String.class; //Arbitrary
    }

    @Override
    protected void onDialogViewCreated(View view) {

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        TextView messageView = view.findViewById(android.R.id.message);
        if (messageView != null)
            messageView.setMovementMethod(LinkMovementMethod.getInstance()); // Make HTML links clickable
    }

    @Override
    protected void persistValue(String value) {
    }

    @Override
    protected String getPersistedValue(String defaultReturnValue) {
        return "";
    }

    @Override
    protected @NonNull
    String getBackupDefaultValue() {
        return "";
    }
}
