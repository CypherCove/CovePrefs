### Version 2.0.0
 * Removed the separate support-based library. The base library now uses the androidx.preference support library due to the deprecation of android.preference.
 * Removed SummaryUpdater utility, since similar functionality is built into the androidx library.
 * Added coveprefs_setSimpleSummaryProvider option to RotaryPreference.
 * Renamed SeekBarPreference to SeekBarPlusPreference to avoid confusion with the androidx component.

### Version 1.0.2
 * Safety net for MultiColor instantiation / setting.
 
### Version 1.0.1
  * Made BaseDialogPreference and BaseInlinePreference constructors protected.