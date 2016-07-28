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
-keepattributes Signature
-keepclassmembers class * {
  public static <fields>;
  public *;
}

-keep class com.crashlytics.** { *; }

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**, android.support.v7.** { *; }
-keep class !android.support.v7.view.menu.**, android.support.v7.** { *; }

-keep class com.google.** { *; }
-keep interface com.google.** { *; }

-keep class com.google.android.** { *; }
-keep interface com.google.android.** { *; }

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-keep public class com.twoheart.dailyhotel.network.SerializableHttpCookie { *; }

-keepnames class android.widget.ScrollView { *; }
-keepnames class android.widget.AbsListView { *; }
-keepnames class android.support.v4.widget.NestedScrollView { *; }
-keepnames class android.support.v7.widget.RecyclerView { *; }
-keepnames class android.support.v4.view.ViewPager { *; }
-keepnames class android.widget.EdgeEffect { *; }
-keepnames class android.support.v4.widget.EdgeEffectCompat { *; }

#Appboy
-dontoptimize
-dontwarn com.amazon.device.messaging.**
-dontwarn bo.app.**
-dontwarn com.appboy.ui.**
-dontwarn com.google.android.gms.**
-keep class bo.app.** { *; }
-keep class com.appboy.** { *; }

#Tune
-keep public class com.tune.** { public *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-keep public class com.google.android.gms.gcm.** { *; }
-keep public class com.google.android.gms.common.** { *; }

-keepclassmembers class ** {
    public void onEvent(**);
}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn android.support.v4.**, android.support.v7.**, com.ning.http.client.**, org.jboss.netty.**
-dontwarn org.slf4j.**, com.fasterxml.jackson.databind.**, com.google.android.gms.**, com.crashlytics.**
-dontwarn com.google.**, android.net.http.AndroidHttpClient, com.android.volley.**
