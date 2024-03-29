### Version 3.0.0
 * Updated Android SDK target to 33, `appcompat` to 1.6.1, and `preference` to 1.2.1.
 * **BREAKING:** Increase minSdkVersion to 19
 * **BREAKING:** BaseDialogPreference and BaseInlinePreference constructors changed to the four-argument
 constructor.
 * Deprecated DividerHider
 * **BREAKING:** `useSimpleSummaryProvider` changed to `coveprefs_useSimpleSummaryProvider` due to a build tools
 change that is preventing the original version from working.
 * Added missing getters and setters.
 * Added LinkPreference. Changed hierarchy so BannerLinkPreference and RatingRequestPreference both
 inherit from LinkPreference.
 * Added titleColor and summaryColor overrides to BannerLinkPreference.
 * **BREAKING:** StringPreference no longer implements TextWatcher.
 * **BREAKING:** MultiColor.Definition outputs CharSequence and CharSequence arrays instead of Strings.
 * Added constructors to MultiColor.Definition.
 * Renamed SeekBarPlusPreference to LabeledSeekBarPreference.
 * **BREAKING:** Renamed PreferenceImageView to CovePrefs_PreferenceImageView. 

### Version 2.0.4
 * Added DuplicateKeyDetector class.
 * ImageListPreference image widget is now right-biased.
 * ImageListPreference dialog ImageButtons now use borderless buttons.
 * Support image content descriptions in ImageListPreference via `coveprefs_entryContentDescriptions`.
 * Added `coveprefs_preference_banner_link` layout that can be used to preserve ripple effects for BannerLinkPreference. Requires ConstraintLayout library.
 * Allow ImageListPreference images to grow to fill column when no row height is set.
 * ImageListPreference now has vertical padding around the image widget.

### Version 2.0.3
 * Updated Android SDK target to 30, `appcompat` to 1.3.0-alpha02, and `preference` to 1.1.1.
 * **BREAKING:** Refactored MultiColor.Definition to use the resource ID for the disabled label
 rather than direct use of a String.
 * **BREAKING:** `MultiColor.set(String)` now throws IllegalArgumentException if the argument is invalid.
 * Added nullability annotations for MultiColor and MultiColor.Definition.
 * Added `loadPersistedValue()` to ColorPreference, MultiColorPreference, RotaryPreference, and SeekBarPlusPreference.
 * Added `coveprefs_backupUri` property for BannerLinkPreference.
 * BannerLinkPreference will not set its Intent if there is no Activity match for the URI.
 * **BREAKING:** Removed ColorUtils, Curve, and ViewUtils to avoid polluting the public API.
 * Added RatingRequestPreference

### Version 2.0.2
 * Fix default values not being loaded for BaseDialogPreferences.

### Version 2.0.1
 * **BREAKING:** Changed `BaseDialogPreference.onValueModifedInDialog` to `onValueModifiedInDialog`
 * Added StringPreference

### Version 2.0.0
 * Removed the separate support-based library. The base library now uses the androidx.preference support library due to the deprecation of android.preference.
 * Removed SummaryUpdater utility, since similar functionality is built into the androidx library.
 * Added coveprefs_setSimpleSummaryProvider option to RotaryPreference.
 * Renamed SeekBarPreference to SeekBarPlusPreference to avoid confusion with the androidx component.

### Version 1.0.2
 * Safety net for MultiColor instantiation / setting.

### Version 1.0.1
  * Made BaseDialogPreference and BaseInlinePreference constructors protected.