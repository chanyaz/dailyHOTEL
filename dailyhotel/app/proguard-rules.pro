# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/sheldon/Development/Android/_Android-SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.facebook.** {
   *;
}

-keep class com.twoheart.dailyhotel.activity

-keep public class com.twoheart.dailyhotel.JavaScriptInterface

-keepclassmembers class com.twoheart.dailyhotel.JavaScriptInterface {
    <fields>;
    <methods>;
}

-keepclassmembers class * {
	@android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Google Play Services

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.kakao.** { *; }
-keepclassmembers class * {
  public static <fields>;
  public *;
}

-keep class com.crashlytics.** { *; }

-keep class org.apache.http.** { *; }

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn android.support.v4.**, android.support.v7.**, com.ning.http.client.**,org.jboss.netty.**, org.slf4j.**, com.fasterxml.jackson.databind.**, com.google.android.gms.**, com.crashlytics.**, com.google.**, org.apache.http.**, android.net.http.AndroidHttpClient, com.android.volley.**