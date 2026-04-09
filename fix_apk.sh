#!/bin/bash
sed -i '/applicationVariants.all/d' app/build.gradle.kts
sed -i '/variant.outputs.all/d' app/build.gradle.kts
sed -i '/outputFileName/d' app/build.gradle.kts
sed -i '/}/d' app/build.gradle.kts # this might be dangerous, wait.
