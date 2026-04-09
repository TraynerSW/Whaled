#!/bin/bash
sed -i '/<meta-data android:name="miui.darkmode.supported"/d' app/src/main/AndroidManifest.xml
sed -i '/<application/a \        <meta-data android:name="miui.darkmode.supported" android:value="false" />\n        <meta-data android:name="coloros.darkmode.supported" android:value="false" />\n        <meta-data android:name="oplus.darkmode.supported" android:value="false" />' app/src/main/AndroidManifest.xml
