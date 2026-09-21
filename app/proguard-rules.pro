# Proguard configuration for CallShield
-keep class androidx.room.** { *; }
-keep class com.callshield.app.data.local.entity.** { *; }
-dontwarn com.callshield.app.data.local.entity.**
