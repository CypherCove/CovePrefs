# CovePrefs
A variety of Material preferences for Android. These preferences are based on, and require, the Android `appcompat-v7` Support Library and use of the appcompat theme for the Activity that hosts the preferences.

If using Android's own PreferenceActivity, import the base `coveprefs` library:

    compile com.cyphercove.coveprefs:coveprefs:1.0.2
    
If you are using the Android `preference-v14` Support Library, then use the `coveprefs-compat` preference library instead:

    compile com.cyphercove.coveprefs:coveprefs-compat:1.0.2

More detailed documentation is forthcoming.

### Project setup

**Modules**

 * **library** - The base library code.
 * **support** - The compat library, with versions of the Preferences suitable for use with the Android `preference-v14` Support Library.
 * **portTool** - A simple class used to transfer modified code from the `library` module to the `support` module. 
 * **example** - An example project demonstrating the use of all the custom Preferences.
 
**Publishing**

The project is set up to publish to BinTray via `gradlew bintrayUpload`. Configuration parameters are private from the git project. The artifacts are published to both jCenter and Maven Central.


