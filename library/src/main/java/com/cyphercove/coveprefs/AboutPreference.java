package com.cyphercove.coveprefs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import static android.text.Html.*;

/**
 * A DialogPreference intended for use to show information about_message the application. The {@code android:dialogTitle}
 * is passed through {@link String#format(Locale, String, Object...)} using the {@code versionName} from the manifest,
 * so it can conveniently show the version string, for example, "MyApp 1.0.1". The {@code android:dialogMessage}
 * is formatted as Html using {@link Html#fromHtml(String)} so it can contain clickable hyperlinks to web sites.
 */
// Extends BaseDialogPreference so it can easily be ported to the support library without code changes.
public class AboutPreference extends BaseDialogPreference<String> {

    private static final String KEY = "com.cyphercove.coveprefs.AboutPreferences";


    @Override
    public Class getDataType() {
        return String.class; //Arbitrary
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

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
                setDialogMessage(Html.fromHtml(getDialogMessage().toString(), FROM_HTML_MODE_LEGACY));
        }

        setNegativeButtonText(null);
    }

    @Override
    protected void onDialogViewCreated(View view) {

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        TextView messageView = (TextView)view.findViewById(android.R.id.message);
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
    protected String getDefaultValue() {
        return "";
    }
}
