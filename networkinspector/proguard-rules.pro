# NetworkInspector ProGuard rules

# Keep the public API
-keep class com.networkinspector.NetworkInspector { *; }
-keep class com.networkinspector.NetworkInspector$* { *; }
-keep class com.networkinspector.core.** { *; }
-keep class com.networkinspector.ui.** { *; }

