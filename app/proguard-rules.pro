# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keepattributes Signature
-keep class com.wled.app.data.model.** { *; }
