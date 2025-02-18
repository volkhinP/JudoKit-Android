# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class com.judokit.android.ui.cardverification.components.JsonParsingJavaScriptInterface {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.judokit.** { *; }
-keepnames class com.judokit.** { *; }
-keep class cards.pay.paycardsrecognizer.sdk.** { *; }

#-keep class com.judokit.android.api.model.** { *; }
#-keepnames class com.judokit.android.api.model.** { *; }
#-keep class com.judokit.android.model.** { *; }
#-keepnames class com.judokit.android.model.** { *; }
#-keep class com.judokit.android.api.error.** { *; }
#-keepnames class com.judokit.android.api.error.** { *; }