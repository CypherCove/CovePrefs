package com.cyphercove.coveprefs.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cyphercove.coveprefs.R;

import java.util.Locale;

/** A dialog much like that shown by {@link com.cyphercove.coveprefs.AboutPreference}. The message
 * is parsted as HTML. The title is formatted to fill in the version number.
 */
public class AboutDialogFragment extends DialogFragment {
    private int dialogTitle, dialogMessage, dialogIcon;

    public static AboutDialogFragment newInstance( int dialogTitle, int dialogMessage, int dialogIcon) {
        AboutDialogFragment f = new AboutDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogTitle", dialogTitle);
        args.putInt("dialogMessage", dialogMessage);
        args.putInt("dialogIcon", dialogIcon);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = requireContext();
        final Resources res = context.getResources();
        final Bundle arguments = getArguments();
        if (arguments != null) {
            dialogTitle = arguments.getInt("dialogTitle");
            dialogMessage = arguments.getInt("dialogMessage");
            dialogIcon = arguments.getInt("dialogIcon");
        }

        String stringTitle = res.getString(dialogTitle); //default no formatting
        Locale locale;
        try {
            locale = res.getConfiguration().locale;
            String versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
            stringTitle = String.format(locale, stringTitle, versionName);
        } catch (NullPointerException e){ // occurs in XML editor
        } catch (PackageManager.NameNotFoundException e){ // occurs in XML editor
        }

        Spanned message;
        String stringMessage = res.getString(dialogMessage);
        if (Build.VERSION.SDK_INT < 24)
            message = Html.fromHtml(stringMessage);
        else
            message = Html.fromHtml(stringMessage, 0);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        TextView messageView = (TextView) layoutInflater.inflate(R.layout.coveprefs_about_dialog, null);
        messageView.setText(message);
        Linkify.addLinks(messageView, Linkify.WEB_URLS);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());

        return new AlertDialog.Builder(context)
                .setIcon(dialogIcon)
                .setTitle(stringTitle)
                .setView(messageView)
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog1, int whichButton) {
                            }
                        }
                )
                .create();
    }
}
