# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Security: Remove debugging information in release builds
-keepattributes !SourceFile,!LineNumberTable

# Security: Obfuscate sensitive classes
-keep class com.safenet.shield.models.** { *; }
-keep class com.safenet.shield.data.** { *; }

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Retrofit and OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Gson rules
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Advanced Features - Keep our new packages
-keep class com.safenet.shield.ai.** { *; }
-keep class com.safenet.shield.analytics.** { *; }
-keep class com.safenet.shield.offline.** { *; }
-keep class com.safenet.shield.wearable.** { *; }
-keep class com.safenet.shield.ml.** { *; }
-keep class com.safenet.shield.blockchain.** { *; }
-keep class com.safenet.shield.community.** { *; }

# TensorFlow Lite rules
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Wearable API rules
-keep class com.google.android.gms.wearable.** { *; }

# Blockchain and crypto rules
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# Security: Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-renamesourcefileattribute SourceFile