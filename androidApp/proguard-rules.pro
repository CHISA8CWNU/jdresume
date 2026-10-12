
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*

-keep class org.koin.** { *; }

-keep class org.koin.ksp.generated.** { *; }
-keep @org.koin.core.annotation.Module class * { *; }

-keepclassmembers class * {
    @org.koin.core.annotation.Single <init>(...);
    @org.koin.core.annotation.Factory <init>(...);
    @org.koin.core.annotation.KoinViewModel <init>(...);
    @org.koin.core.annotation.Named <init>(...);
}

-keep public class * extends android.appwidget.AppWidgetProvider {
    public void *(android.content.Context, android.content.Intent);
    <init>();
}
-keep class com.chisacwnu.hicwnu.widget.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

-dontwarn io.ktor.**

-keep class org.slf4j.impl.** { *; }

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

-keep class kotlin.Metadata { *; }
-keep @kotlinx.serialization.Serializable class * { ** Companion; }
-keepclassmembers class * { *** write$Self(...); <init>(int, ...); }
-keep class **$$serializer { *; }

-keep class * implements com.squareup.wire.Message {
    <fields>;
    <methods>;
}
-keep class * implements com.squareup.wire.WireEnum { *; }
-keepclassmembers class * implements com.squareup.wire.Message {
    public static *** ADAPTER;
}
-keep class * extends com.squareup.wire.ProtoAdapter { *; }

-dontwarn androidx.sqlite.**
-keep class androidx.sqlite.** { *; }
-keep class com.chisacwnu.hicwnu.shared.** { *; }
-keep class com.chisacwnu.hicwnu.data.db.** { *; }
-keep class com.chisacwnu.hicwnu.data.model.** { *; }

-keep class com.chisacwnu.hicwnu.data.api.update.** { *; }
-keep class com.chisacwnu.hicwnu.tool.AppUpdateCoordinator { *; }
-keep class com.chisacwnu.hicwnu.tool.AndroidAppUpdateCoordinator { *; }
-keep class com.chisacwnu.hicwnu.ui.update.** { *; }
-dontwarn org.slf4j.**