#!/bin/bash
sed -i '/android {/a \    applicationVariants.all { variant ->\n        variant.outputs.all { output ->\n            outputFileName = "whaled.apk"\n        }\n    }' app/build.gradle.kts
