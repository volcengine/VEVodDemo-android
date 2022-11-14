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
-keep class com.bytedance.playerkit.player.volcengine.**{*;}

# 点播模块
-keep class com.ss.ttm.** {*;}
-keep class com.ss.ttvideoengine.** {*;}
-keep class com.ss.mediakit.** {*;}
-keep class com.ss.texturerender.** {*;}
-keep class com.bytedance.**{*;}
-keep class com.pandora.ttlicense2.**{*;}
-keep class com.pandora.common.applog.**{*;}
-keep class com.pandora.vod.VodSDK {*;}

# 超分
-keep class com.ss.bmf.** {*;}
-keep class com.ss.bmf_mods.** {*;}
-keep class com.ss.bmf_mods_api.** {*;}