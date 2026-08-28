# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============ Glide ============
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ============ MediaStore / ContentProvider ============
-keep class android.provider.MediaStore { *; }
-keep class android.provider.MediaStore$Audio { *; }
-keep class android.provider.MediaStore$Audio$Media { *; }

# ============ Custom Views ============
-keep class com.codetrio.overdrive.ui.custom.** { *; }

# ============ Palette ============
-keep class androidx.palette.graphics.** { *; }

# ============ Navigation ============
-keep class androidx.navigation.** { *; }

# ============ Lifecycle ============
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.LiveData { *; }

# ============ Lyrics & JSON Models ============
# Keep lyrics data classes (Gson needs field names to match JSON)
-keep class com.codetrio.overdrive.data.lyrics.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# ============ Retrofit & OkHttp ============
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ============ Gson ============
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ============ Jaudiotagger ============
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# ============ InnerTube & Models ============
-keep class com.codetrio.overdrive.data.innertube.** { *; }
-keep class com.codetrio.overdrive.model.** { *; }

# ============ NewPipe Extractor & Jsoup ============
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ============ Rhino (org.mozilla.javascript) ============
-dontwarn java.beans.**
-dontwarn org.mozilla.javascript.**
-dontwarn javax.script.**

# ============ SLF4J ============
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# ============ Ktor ============
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ============ Kotlinx Serialization ============
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembernames class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ============ Media3 / ExoPlayer ============
-dontwarn androidx.media3.**

# ============ Log Suppression for Release Builds ============
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int println(...);
}

# ============ Firebase & Crashlytics ============
-keepattributes *Annotation*, SourceFile, LineNumberTable
-keepclassmembers class * {
    @com.google.firebase.crashlytics.** *;
}
-dontwarn com.google.firebase.**

# ============ Room Database ============
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ============ Hilt & Dependency Injection ============
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }

