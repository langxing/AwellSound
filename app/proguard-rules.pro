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

# 保留kpslibrary的所有类和内部类，禁止R8优化/混淆
-keep class com.awell.kpslibrary.** { *; }
# 若内部类仍报错，额外保留内部类
-keep class com.awell.kpslibrary.Constant$IAUDIOCONTROL$CHANNEL { *; }
-keep class com.awell.kpslibrary.Constant$IAUDIOCONTROL { *; }
-keep class com.awell.kpslibrary.Constant { *; }

# 可选：保留所有内部类（避免漏写）
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-dontwarn android.os.ServiceManager
-dontwarn android.os.SystemProperties