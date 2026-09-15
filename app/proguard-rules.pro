-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

-keep class kotlinx.** { *; }
-keepclassmembers class kotlinx.** { *; }

-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class com.ailivingworld.wallpaper.** { *; }
-keepclassmembers class com.ailivingworld.wallpaper.** { *; }

-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    *;
}

-keep class timber.log.Timber { *; }

-dontwarn java.lang.invoke.*
-dontwarn sun.reflect.*
-dontwarn javax.lang.model.element.*