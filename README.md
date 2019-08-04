# CovePrefs
A variety of Material preferences for Android. These preferences are based on, and require, the Jetpack androidx `appcompat` and `preference` libraries.

## Usage

Import the `coveprefs` library with

    compile com.cyphercove.coveprefs:coveprefs:2.0.0

The AndroidX support library prepares dialogs for dialog preferences separately from the preferences themselves. Therefore, in order to use CovePrefs, you must do one of the following.

It's easiest to make your preference fragment extend CovePreferenceFragmentCompat. This class takes care of creating the dialog fragments for CovePrefs used in the layout.

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

## Preferences

### AboutPreference

AboutPreference is a DialogPreference intended for showing information about the app, such as the version number and libraries used. It uses the same properties as a DialogPreference with some behavior changes.

| Property        | Property type | Description                                                                                                                         |
|-----------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `key`           | String        | Although no value is stored for this type of preference, a unique key must be set if more than AboutPreference is used in a layout. |
| `dialogTitle`   | String        | If the dialog title includes a `%s`, the string will automatically be formatted to fill in the app's version number.                |
| `dialogMessage` | String        | The dialog message may include HTML, which can be used for links to libraries used.                                                 |

### BannerLinkPreference

BannerLinkPreference is a generic Preference with a property for setting a background drawable. This can be used for making a preference that stands out and links to something else.

| Property           | Property type | Description                                                                                                             |
|--------------------|---------------|-------------------------------------------------------------------------------------------------------------------------|
| `key`              | String        | Although no value is stored for this type of preference, a unique key must be set if more than one is used in a layout. |
| `coveprefs_banner` | Drawable      | The image to fill the background of the preference                                                                      |
| `coveprefs_uri`    | String        | Convenience URI link. If used, the Preference will automatically be given an Action.VIEW Intent with the provided URI.  |

### ColorPreference

A preference that shows a color picker. The preference value is saved as an Integer in AARRGGBB format. 

| Property                            | Property type          | Description                                                                                                                                                                                                                                                             |
|-------------------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `coveprefs_colorPickerWidgets`      | Flags:`hsv\|hex\|recent` | Which types of color selection widgets to show. `hsv` is a large HSV color picker. `hex` is a text box for typing in the color with hexadecimal. `recent` shows a scrolling list of recently picked color swatches. Any combination can be used. Defaults to all three. |

### MultiColorPreference

A preference that shows a color picker. The user can select multiple colors at once for different purposes, and select between different sets of purposes.

The possible sets of colors are defined as an array of String arrays. Each String array is a set of colors that can be picked, and one of these sets can be picked. The value is stored as a String that consists of the integer index of the String array set selected, followed by space-separated color values for that set of colors in `#AARRGGBB` format.

The value of the preference can be interpreted by using the MultiColor and MultiColor.Definition classes.

| Property                            | Property type          | Description                                                                                                                                                                                                                                                             |
|-------------------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `coveprefs_colorPickerWidgets`      | Flags:`hsv\|hex\|recent` | Which types of color selection widgets to show. `hsv` is a large HSV color picker. `hex` is a text box for typing in the color with hexadecimal. `recent` shows a scrolling list of recently picked color swatches. Any combination can be used. Defaults to all three. |
| `coveprefs_multiColorDefinition`    | Array of String arrays | Each String array in the array defines one set of colors that can be picked. The Strings are the names of the color options. The user can switch between the arrays by tapping arrows in the dialog. If a String array is empty, it is treated as a disabled state.     |
| `coveprefs_multiColorDisabledLabel` | String                 | If there is an empty String array in the multi-color definition, it will be labeled with this string when selected in the picker.                                                                                                                                       |

### ImageListPreference

Behaves similarly to a ListPreference, but the user selects from an array of drawables. The value is stored as a String from the entry values array.

| Property             | Property type                   | Description                                                                                                                                              |
|----------------------|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `entries`            | Array of Drawables              | The drawables the user must select from.                                                                                                                 |
| `entryValues`        | Array of Strings                | An array of values corresponding to the drawables in `entries`. Must be the same size array as `entries`. The values should not be translated by locale. |
| `tint`               | Color State List                | Colors used to tint the clickable images in the dialog box.                                                                                              |
| `coveprefs_tintMode` | `coveprefs_tintmode` enum value | PorterDuff mode used to tint the drawables with the `tint` attribute. One of `src_over`, `src_in`, `src_stop`, `multiply`, `screen`, and `add`.          |

### RotaryPreference

Allows user to pick an integer from 0-359 with a dialog that depicts an arrow radiating from the center of a circle. The value is represented in the preference as an arrow pointing in the corresponding direction.

| Property                   | Property type | Description                                                                                                    |
|----------------------------|---------------|----------------------------------------------------------------------------------------------------------------|
| `useSimpleSummaryProvider` | boolean       | If true, the value selected with a ° symbol appended is shown as the summary of the preference. Default false. |

### StringPreference

A DialogPreference with an EditText for entering the String value of the preference.

| Property                            | Property type  | Description                                                                                      |
|-------------------------------------|----------------|--------------------------------------------------------------------------------------------------|
| `coveprefs_editTextHint`            | String         | The hint text for the edit test.                                                                 |
| `coveprefs_allowEmptyString`        | boolean        | If false, the positive button of the dialog is hidden if the edit text is empty. Default true.   |
| `useSimpleSummaryProvider`          | boolean        | If true, the String value of the preference is automatically set as the Preference summary.      |

### SeekBarPlusPreference

A subclass of SeekBarPreference that injects labels on either end of the seek bar. All the properties of a typical SeekBarPreference apply.

| Property               | Property type | Description                                    |
|------------------------|---------------|------------------------------------------------|
| `coveprefs_leftLabel`  | String        | Text label placed to the left of the SeekBar.  |
| `coveprefs_rightLabel` | String        | Text label placed to the right of the SeekBar. |