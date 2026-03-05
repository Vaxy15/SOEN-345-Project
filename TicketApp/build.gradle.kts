// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.0.1" apply false
    id("com.android.library") version "9.0.1" apply false

    // Google Services plugin (for google-services.json)
    id("com.google.gms.google-services") version "4.4.4" apply false

    // Optional: JUnit 5 support on Android (recommended if your course wants JUnit 5)
    id("de.mannodermaus.android-junit") version "2.0.1" apply false
}