# SafeNet Shield - GitHub Copilot Instructions

**ALWAYS follow these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

SafeNet Shield is a comprehensive Android application for personal safety and cybercrime prevention, built with Kotlin, Firebase, and advanced security features including AI/ML models, blockchain evidence management, and community intelligence.

## Working Effectively

### Bootstrap and Build Requirements
**CRITICAL - Network Dependencies Required:**
- Build requires internet connectivity for Android Gradle Plugin and Firebase dependencies
- **NEVER CANCEL builds or dependency downloads** - they may take 30-45 minutes on first run
- **Set timeouts to 60+ minutes** for initial builds and dependency resolution

```bash
# Make gradlew executable (required first step)
chmod +x gradlew

# Clean build - NEVER CANCEL: Takes 30-45 minutes on first run
./gradlew clean --timeout=3600000

# Build debug APK - NEVER CANCEL: Takes 20-30 minutes  
./gradlew assembleDebug --timeout=3600000

# Build release APK (requires signing) - NEVER CANCEL: Takes 25-35 minutes
./gradlew assembleRelease --timeout=3600000

# Run unit tests - NEVER CANCEL: Takes 10-15 minutes
./gradlew testDebugUnitTest --timeout=2700000

# Run instrumentation tests (requires emulator/device) - NEVER CANCEL: Takes 15-25 minutes
./gradlew connectedDebugAndroidTest --timeout=3600000

# Run lint analysis - NEVER CANCEL: Takes 5-10 minutes
./gradlew lint --timeout=1800000
```

### Required Setup Before Building
1. **Java 17 JDK** - Project requires Java 17 (configured in build.gradle)
2. **Android SDK** - Minimum API 24, Target API 34, Compile SDK 34
3. **Network Access** - Required for Google/Firebase dependencies
4. **Firebase Configuration** - `google-services.json` must be in `app/` directory

### Project Structure and Key Components
```
app/src/main/java/com/safenet/shield/
├── MainActivity.kt                    # Main app entry point
├── ai/                               # AI/ML threat detection components
├── analytics/                        # Firebase Analytics integration
├── auth/                            # Authentication (Firebase Auth)
├── blockchain/                      # Blockchain evidence management
├── community/                       # Community intelligence features
├── cybercrime/                      # Cybercrime reporting system
├── data/                           # Data models and repositories
├── government/                     # Government API integrations
├── ml/                             # Machine learning models
├── network/                        # Network security and API clients
├── offline/                        # Offline emergency management
├── ui/                             # UI components and activities
├── utils/                          # Utilities (validation, security)
└── wearable/                       # Wearable device integration
```

## Firebase Configuration (CRITICAL)
```bash
# 1. Create Firebase project at console.firebase.google.com
# 2. Enable services: Authentication, Firestore, Storage, Functions, ML Kit
# 3. Download google-services.json to app/ directory
# 4. Deploy security rules (see firestore.rules and storage.rules)

# Install Firebase CLI (if not installed)
npm install -g firebase-tools

# Deploy Firebase security rules - NEVER CANCEL: Takes 2-5 minutes  
firebase deploy --only firestore:rules --timeout=600000
firebase deploy --only storage:rules --timeout=600000
firebase deploy --only functions --timeout=900000
```

## Testing and Validation

### Unit Tests
```bash
# Run all unit tests - NEVER CANCEL: Takes 10-15 minutes
./gradlew testDebugUnitTest --timeout=2700000

# Current test structure (note: limited example tests exist)
# - ExampleUnitTest.kt (basic arithmetic test)
# - Tests are in app/src/test/java/com/example/myapplication/
# - When creating new tests, place in app/src/test/java/com/safenet/shield/

# Run specific test class
./gradlew testDebugUnitTest --tests="ExampleUnitTest"
```

### Instrumentation Tests  
```bash
# Requires Android emulator or physical device
# Start emulator first, then run - NEVER CANCEL: Takes 15-25 minutes
./gradlew connectedDebugAndroidTest --timeout=3600000
```

### Manual Validation Scenarios
**ALWAYS test these scenarios after making changes:**

1. **Authentication Flow:**
   - Launch app → Register new user → Verify email → Login → Access main features
   - Test Firebase Authentication integration

2. **Report Submission:**
   - Login → Create report → Add evidence → Submit → Verify storage in Firestore
   - Test file upload to Firebase Storage

3. **Emergency Features:**
   - Test emergency contact management
   - Test offline emergency functionality
   - Verify location services integration

4. **Security Features:**
   - Test input validation (SQL injection prevention)
   - Test biometric authentication (if available)
   - Verify encrypted storage functionality

## Code Quality and Linting

```bash
# Run lint analysis - NEVER CANCEL: Takes 5-10 minutes
./gradlew lint --timeout=1800000

# Generate lint report (creates reports in app/build/reports/)
./gradlew lintDebug --timeout=1800000

# **ALWAYS run before committing** - CI will fail without clean lint
./gradlew lint
```

## Security Implementation Notes

### Key Security Features
- **Input Validation**: All user inputs validated in `ValidationUtils.kt` (SQL injection, XSS prevention)
- **Authentication Security**: Login attempt limiting, secure token storage
- **Encrypted Storage**: Using Android Security Crypto library (SharedPreferences encryption)
- **Firebase Security Rules**: Comprehensive rules with role-based access control
- **ProGuard/R8**: Enabled for release builds with obfuscation
- **Network Security**: Certificate pinning configured for Firebase endpoints

### Security-Critical Files
- `app/src/main/java/com/safenet/shield/utils/ValidationUtils.kt` - Input validation (email, password, SQL injection detection)
- `app/src/main/java/com/safenet/shield/utils/SecurityUtils.kt` - Security utilities (login rate limiting, secure storage)
- `firestore.rules` - Firebase security rules (user isolation, timestamp validation)
- `storage.rules` - Firebase Storage security rules (authenticated user access)
- `app/src/main/res/xml/network_security_config.xml` - Network security configuration

### Security Validation Requirements
**ALWAYS test security features when modifying:**
- Input validation: Test with SQL injection patterns, XSS attempts
- Authentication: Verify rate limiting after failed login attempts  
- Data access: Ensure users can only access their own reports and data
- File uploads: Verify only allowed file types are accepted

## Build Limitations and Known Issues

### Network Dependency Issues
**If builds fail with "dl.google.com" connection errors:**
- Build requires internet access for Android Gradle Plugin and Firebase dependencies
- **DO NOT** attempt offline builds - they will fail
- Ensure network connectivity before running builds
- Use corporate network if behind firewall restrictions

### Firebase Requirements
**Build will fail without proper Firebase setup:**
- `google-services.json` must exist in `app/` directory
- Use `google-services.json.template` as reference
- Firebase project must have required services enabled

### Memory Requirements
- Gradle JVM configured for 2GB heap (`gradle.properties`)
- Large builds may require increasing heap size
- Close unnecessary applications during builds

## Dependencies and Versions

### Core Dependencies
- **Android Gradle Plugin**: 8.9.1
- **Kotlin**: 1.9.22  
- **Compile SDK**: 34
- **Min SDK**: 24
- **Target SDK**: 34
- **Java**: 17

### Key Libraries
- **Firebase BOM**: 32.7.0 (Auth, Firestore, Storage, ML Kit)
- **TensorFlow Lite**: 2.14.0 (AI/ML features)
- **Room**: 2.6.1 (Local database)
- **Retrofit**: 2.9.0 (API networking)
- **Security Crypto**: 1.1.0-alpha06

## Troubleshooting Common Issues

### Build Failures
1. **"Could not resolve dependencies"** - Check network connectivity
2. **"google-services.json not found"** - Add Firebase configuration file
3. **"Java version mismatch"** - Ensure Java 17 is installed and configured

### Test Failures
1. **"No devices found"** - Start emulator for instrumentation tests
2. **"Firebase not initialized"** - Ensure proper Firebase configuration
3. **"Network security policy"** - Check network_security_config.xml

### Runtime Issues
1. **"Firebase Auth failed"** - Verify Firebase project configuration
2. **"Permission denied"** - Check AndroidManifest.xml permissions
3. **"Database access failed"** - Verify Firestore security rules

## Development Workflow

### Making Changes
1. **Always run linting first**: `./gradlew lint`
2. **Test affected functionality manually** using validation scenarios
3. **Run relevant unit tests**: `./gradlew testDebugUnitTest`
4. **Build and test on device/emulator**
5. **Verify security implications** for any input handling changes

### Pre-commit Checklist  
- [ ] Lint analysis passes cleanly
- [ ] Unit tests pass
- [ ] Manual validation scenarios tested
- [ ] Security review for sensitive changes
- [ ] Firebase rules updated if data model changed

### Code Style
- Follow Kotlin coding conventions
- Use `viewBinding` for UI components (configured in build.gradle)
- Implement security-first design patterns
- Add KDoc documentation for public APIs
- Use dependency injection patterns (Hilt integration ready)

## CI/CD Integration

### GitHub Actions
- **CodeQL analysis** configured in `.github/workflows/codeql.yml`
- Builds use `autobuild` mode for Kotlin/Java analysis
- Security scanning runs on every push and PR

### Expected CI Build Times
- **CodeQL Analysis**: 15-20 minutes
- **Full Build**: 25-35 minutes
- **Test Suite**: 20-30 minutes total

**Remember: NEVER CANCEL long-running builds or tests. Set appropriate timeouts and wait for completion.**