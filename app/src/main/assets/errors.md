# 📋 Talaan ng Pagbuo — Proyekto ni Mark Jann Tampok

---
📅 **Petsa:** 2026-08-27 02:34:17 UTC
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
> Task :preBuild UP-TO-DATE
> Task :preDebugBuild UP-TO-DATE
> Task :mergeDebugNativeDebugMetadata NO-SOURCE
> Task :checkKotlinGradlePluginConfigurationErrors
> Task :generateDebugResValues
> Task :checkDebugAarMetadata
> Task :mapDebugSourceSetPaths
> Task :generateDebugResources
> Task :packageDebugResources
> Task :mergeDebugResources
> Task :createDebugCompatibleScreenManifests
> Task :extractDeepLinksDebug
> Task :parseDebugLocalResources
> Task :processDebugMainManifest FAILED

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':processDebugMainManifest' (type 'ProcessApplicationManifest').
  - In plugin 'com.android.internal.version-check' type 'com.android.build.gradle.tasks.ProcessApplicationManifest' property 'mainManifest' specifies file '/home/runner/work/bisyo/bisyo/src/main/AndroidManifest.xml' which doesn't exist.
    
    Reason: An input file was expected to be present but it doesn't exist.
    
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.
    
    For more information, please refer to https://docs.gradle.org/8.2/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 50s
10 actionable tasks: 10 executed
```

---
✅ **Katapusan ng Talaan**
