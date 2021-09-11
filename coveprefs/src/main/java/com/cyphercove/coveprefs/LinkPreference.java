package com.cyphercove.coveprefs;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.StringRes;
import androidx.preference.Preference;

import com.cyphercove.coveprefs.utils.CovePrefsUtils;

/**
 * A Preference that creates and sets its own Intent based on its URI properties. Two URIs can be
 * set so if one fails, the next can be tried automatically. This can be useful for links that first
 * attempt an app link, and if it's not installed, opening a web URL instead. Setting
 * {@code app:coveprefs_uri} will set a {@link android.content.Intent#ACTION_VIEW} Intent with
 * that URI, without the need to specify an {@code <intent>} in the xml. If that URI cannot be
 * resolved, the backup URI set with {@code app:coveprefs_backupUri} will be tried next. If neither
 * can be resolved, the preference silently does nothing.
 * <p>
 * Setting {@code app:coveprefs_uriFormatArg} will automatically format the URI string(s) with the
 * single argument.
 */
@SuppressWarnings("WeakerAccess")
public class LinkPreference extends Preference {
    private String uri;
    private String backupUri;
    private String uriFormatArg;

    public LinkPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, com.cyphercove.coveprefs.R.styleable.CovePrefs_LinkPreference);
        uri = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_LinkPreference_coveprefs_uri);
        backupUri = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_LinkPreference_coveprefs_backupUri);
        uriFormatArg = a.getString(com.cyphercove.coveprefs.R.styleable.CovePrefs_LinkPreference_coveprefs_uriFormatArg);
        a.recycle();

        Intent intent = CovePrefsUtils.resolveIntent(context, uriFormatArg, uri, backupUri);
        if (intent != null){
            setIntent(intent);
        }
    }

    public LinkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LinkPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public LinkPreference(Context context) {
        this(context, null);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        if ((uri == null && this.uri != null) || (uri != null && !uri.equals(this.uri))) {
            this.uri = uri;
            updateIntent();
            notifyChanged();
        }
    }

    public void setUri(@StringRes int uriId){
        setUri(getContext().getString(uriId));
    }

    public String getBackupUri() {
        return backupUri;
    }

    public void setBackupUri(String backupUri) {
        if ((backupUri == null && this.backupUri != null) || (backupUri != null && !backupUri.equals(this.backupUri))) {
            this.backupUri = backupUri;
            updateIntent();
            notifyChanged();
        }
    }

    public void setBackupUri(@StringRes int uriId){
        setBackupUri(getContext().getString(uriId));
    }

    public String getUriFormatArg() {
        return uriFormatArg;
    }

    public void setUriFormatArg(String uriFormatArg) {
        if ((uriFormatArg == null && uriFormatArg != null) || (uriFormatArg != null && !uriFormatArg.equals(this.uriFormatArg))) {
            this.uriFormatArg = uriFormatArg;
            updateIntent();
            notifyChanged();
        }
    }

    public void setUriFormatArg(@StringRes int uriFormatArgId){
        setUriFormatArg(getContext().getString(uriFormatArgId));
    }

    private void updateIntent() {
        Intent intent = CovePrefsUtils.resolveIntent(getContext(), uriFormatArg, uri, backupUri);
        if (intent != null){
            setIntent(intent);
        }
    }
}
