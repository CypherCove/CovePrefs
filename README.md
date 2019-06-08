# CovePrefs
A variety of Material preferences for Android. These preferences are based on, and require, the Jetpack androidx `appcompat` and `preference` libraries.

### Usage

Import the `coveprefs` library with

    compile com.cyphercove.coveprefs:coveprefs:2.0.0

The AndroidX support library prepares dialogs for dialog preferences separately from the preferences themselves. Therefore, in order to use CovePrefs, you must do one of the following.

It's easiest to make your preference fragment to extend CovePreferenceFragmentCompat. This class takes care of creating the dialog fragments for CovePrefs used in the layout.

However, you might want to extend from some other fragment class, possibly if you are using another library. In this case you can still use CovePrefs by making your host Activity implement `androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback` and calling through to `CovePrefs.onPreferenceDisplayDialog()` like this:

    public class MySettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    
        @Override
        public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, Preference pref) {
            return CovePrefs.onPreferenceDisplayDialog(caller, pref);
        }
        
    }

If you want to support other custom dialog preferences, you can tack them on like this:

        @Override
        public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, Preference pref) {
            if (CovePrefs.onPreferenceDisplayDialog(caller, pref))
                return true;
            // Create dialog fragments for other types of preferences here.
        }