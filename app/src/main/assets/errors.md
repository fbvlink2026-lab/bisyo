# 📋 Talaan ng Pagbuo — Proyekto ni Mark Jann Tampok

---
📅 **Petsa:** 2026-08-26 23:15:16 UTC
🎯 **Katayuan:** ❌ NABIGO
📝 **Detalye:** Hindi nabuo ang APK — tingnan ang log sa ibaba
---

## 📋 Buong Log ng Pagbuo
```
Downloading https://services.gradle.org/distributions/gradle-8.2-bin.zip
............10%............20%............30%.............40%............50%............60%............70%.............80%............90%............100%

Welcome to Gradle 8.2!

Here are the highlights of this release:
 - Kotlin DSL: new reference documentation, assignment syntax by default
 - Kotlin DSL is now the default with Gradle init
 - Improved suggestions to resolve errors in console output

For more details see https://docs.gradle.org/8.2/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:checkKotlinGradlePluginConfigurationErrors
> Task :app:generateDebugResValues
> Task :app:checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:parseDebugLocalResources

> Task :app:processDebugMainManifest
package="com.markjann.bisyoapp" found in source AndroidManifest.xml: /home/runner/work/bisyo/bisyo/app/src/main/AndroidManifest.xml.
Setting the namespace via the package attribute in the source AndroidManifest.xml is no longer supported, and the value is ignored.
Recommendation: remove package="com.markjann.bisyoapp" from the source AndroidManifest.xml: /home/runner/work/bisyo/bisyo/app/src/main/AndroidManifest.xml.

> Task :app:processDebugManifest
> Task :app:javaPreCompileDebug
> Task :app:mergeDebugShaders
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:desugarDebugFileDependencies
> Task :app:compressDebugAssets
> Task :app:mergeDebugJniLibFolders
> Task :app:checkDebugDuplicateClasses
> Task :app:mergeDebugNativeLibs NO-SOURCE
> Task :app:stripDebugDebugSymbols NO-SOURCE
> Task :app:processDebugManifestForPackage
> Task :app:mergeLibDexDebug
> Task :app:processDebugResources FAILED
> Task :app:mergeExtDexDebug
> Task :app:validateSigningDebug

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:processDebugResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskAction
   > Android resource linking failed
     ERROR: /home/runner/work/bisyo/bisyo/app/src/main/AndroidManifest.xml:7:5-24:19: AAPT: error: resource mipmap/ic_launcher (aka com.markjann.bisyoapp:mipmap/ic_launcher) not found.
         
     ERROR: /home/runner/work/bisyo/bisyo/app/src/main/AndroidManifest.xml:7:5-24:19: AAPT: error: resource mipmap/ic_launcher_round (aka com.markjann.bisyoapp:mipmap/ic_launcher_round) not found.
         

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 9s
23 actionable tasks: 23 executed
```

---
✅ **Katapusan ng Talaan**
