# Project-specific ProGuard / R8 rules for release optimization

# Preserve line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Room database, entities, and DAOs
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# Keep Moshi JSON models and codegen adapters
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep Retrofit & OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Application data models
-keep class com.example.data.** { *; }
-keep class com.example.prayer.** { *; }
-keep class com.example.audio.** { *; }
-keep class com.example.widget.** { *; }

# Keep Android App Widgets
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# Keep Coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
