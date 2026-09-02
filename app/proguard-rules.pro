# Keep Gson model classes
-keep class com.wopro.app.data.remote.dto.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Room
-keep class * extends androidx.room.RoomDatabase
