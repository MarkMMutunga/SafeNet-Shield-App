#!/bin/bash

# SafeNet Shield - Automated Error Fixes
# This script addresses the critical and high-priority issues identified in the error analysis

echo "SafeNet Shield - Automated Error Fixes"
echo "======================================"

PROJECT_ROOT="/home/runner/work/SafeNet-Shield-App/SafeNet-Shield-App"
cd "$PROJECT_ROOT"

echo "🔧 Starting automated fixes..."

# Fix 1: Update test package names
echo "📝 Fixing test package names..."

# Update ExampleUnitTest.kt
if [ -f "app/src/test/java/com/example/myapplication/ExampleUnitTest.kt" ]; then
    echo "  - Updating ExampleUnitTest.kt package name"
    sed -i 's/package com.example.myapplication/package com.safenet.shield/' \
        "app/src/test/java/com/example/myapplication/ExampleUnitTest.kt"
fi

# Update ExampleInstrumentedTest.kt
if [ -f "app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt" ]; then
    echo "  - Updating ExampleInstrumentedTest.kt package name and assertions"
    sed -i 's/package com.example.myapplication/package com.safenet.shield/' \
        "app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt"
    sed -i 's/"com.example.myapplication"/"com.safenet.shield"/' \
        "app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt"
fi

# Fix 2: Create proper test directory structure
echo "📁 Creating proper test directory structure..."
mkdir -p "app/src/test/java/com/safenet/shield"
mkdir -p "app/src/androidTest/java/com/safenet/shield"

# Move test files to correct location
if [ -f "app/src/test/java/com/example/myapplication/ExampleUnitTest.kt" ]; then
    echo "  - Moving ExampleUnitTest.kt to correct package directory"
    mv "app/src/test/java/com/example/myapplication/ExampleUnitTest.kt" \
       "app/src/test/java/com/safenet/shield/"
fi

if [ -f "app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt" ]; then
    echo "  - Moving ExampleInstrumentedTest.kt to correct package directory"
    mv "app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt" \
       "app/src/androidTest/java/com/safenet/shield/"
fi

# Remove old package directories if empty
rmdir "app/src/test/java/com/example/myapplication" 2>/dev/null || true
rmdir "app/src/test/java/com/example" 2>/dev/null || true
rmdir "app/src/androidTest/java/com/example/myapplication" 2>/dev/null || true
rmdir "app/src/androidTest/java/com/example" 2>/dev/null || true

# Fix 3: Create Firebase configuration template
echo "🔥 Creating Firebase configuration template..."
if [ ! -f "app/google-services.json" ]; then
    cat > "app/google-services.json.template" << 'EOF'
{
  "project_info": {
    "project_number": "YOUR_PROJECT_NUMBER",
    "project_id": "YOUR_PROJECT_ID",
    "storage_bucket": "YOUR_PROJECT_ID.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "YOUR_ANDROID_APP_ID",
        "android_client_info": {
          "package_name": "com.safenet.shield"
        }
      },
      "oauth_client": [
        {
          "client_id": "YOUR_OAUTH_CLIENT_ID",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "YOUR_API_KEY"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "YOUR_OAUTH_CLIENT_ID",
              "client_type": 3
            }
          ]
        }
      }
    }
  ],
  "configuration_version": "1"
}
EOF
    echo "  ✅ Created google-services.json.template"
    echo "  ⚠️  You need to download the actual google-services.json from Firebase Console"
fi

# Fix 4: Create missing ProGuard rules file
echo "🛡️  Creating ProGuard rules..."
if [ ! -f "app/proguard-rules.pro" ]; then
    cat > "app/proguard-rules.pro" << 'EOF'
# SafeNet Shield ProGuard Rules
# Add project specific ProGuard rules here.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep model classes for Firebase Firestore
-keep class com.safenet.shield.models.** { *; }
-keep class com.safenet.shield.data.** { *; }

# Keep classes for TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }

# Keep security and encryption classes
-keep class com.safenet.shield.utils.SecurityUtils { *; }
-keep class com.safenet.shield.utils.CryptographyManager { *; }

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Remove debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
EOF
    echo "  ✅ Created app/proguard-rules.pro"
fi

# Fix 5: Update gradle version to stable
echo "🔧 Checking Gradle configuration..."
if grep -q "8.9.1" build.gradle; then
    echo "  ⚠️  Consider downgrading Android Gradle Plugin from 8.9.1 to 8.7.2 for stability"
    echo "     Update build.gradle line 9: classpath 'com.android.tools.build:gradle:8.7.2'"
fi

# Fix 6: Create .gitignore for sensitive files
echo "📝 Updating .gitignore..."
if ! grep -q "google-services.json" .gitignore 2>/dev/null; then
    cat >> .gitignore << 'EOF'

# Firebase configuration (add real file manually)
google-services.json

# Build artifacts
*.apk
*.aab
build/
captures/

# Local configuration files
local.properties
keystore.properties
EOF
    echo "  ✅ Updated .gitignore"
fi

# Summary
echo ""
echo "✅ Automated fixes completed!"
echo ""
echo "📋 What was fixed:"
echo "  ✅ Test package names corrected"
echo "  ✅ Test directory structure organized"
echo "  ✅ Firebase configuration template created"
echo "  ✅ ProGuard rules file created"
echo "  ✅ .gitignore updated"
echo ""
echo "⚠️  Manual actions still required:"
echo "  1. Download google-services.json from Firebase Console"
echo "  2. Consider downgrading Android Gradle Plugin for stability"
echo "  3. Review duplicate VerificationFragment classes"
echo "  4. Implement missing TODOs in auth classes"
echo ""
echo "📖 See ERROR_ANALYSIS_REPORT.md for complete issue details"