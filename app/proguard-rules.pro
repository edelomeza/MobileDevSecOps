# Ktor (necesita reflection para content negotiation y auth)
-keep,allowshrinking class io.ktor.** { *; }
-dontwarn io.ktor.**

# Koin (necesita reflection para DI)
-keep,allowshrinking class org.koin.** { *; }
-dontwarn org.koin.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.mobiledevsecops.**$$serializer { *; }
-keepclassmembers class com.example.mobiledevsecops.data.remote.dto.** {
    <init>(...);
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Remover logs de release (seguridad)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# OkHttp (Ktor engine)
-dontwarn okhttp3.**
-dontwarn okio.**

# AndroidX Security (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }

# ViewModels y sus constructores (para Koin DI)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
