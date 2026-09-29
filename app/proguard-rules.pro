# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep line numbers so Play Console / crash reports can be deobfuscated with the mapping file
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- App models ----
# Parsed by Gson (assets JSON and Retrofit responses) via reflection on field names,
# and passed around as Serializable in bundles / intents.
-keep class com.thesunnahrevival.sunnahassistant.data.model.** { *; }

# Screen names sent to Firebase Analytics use javaClass.simpleName
-keepnames class * extends com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

# ViewsUtil.reduceDragSensitivity reads these private fields reflectively
-keepclassmembers class androidx.viewpager2.widget.ViewPager2 { *** mRecyclerView; }
-keepclassmembers class androidx.recyclerview.widget.RecyclerView { int mTouchSlop; }

# ---- Serializable ----
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---- Gson (2.8.x ships no consumer rules) ----
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ---- Retrofit 2.9 under R8 full mode ----
-keepattributes InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# ---- ummalqura-calendar ----
# Hijri month/day names are ResourceBundle classes looked up by name (UmmalquraFormatData, _ar, ...)
-keep class com.github.msarhan.ummalqura.calendar.text.** { *; }
