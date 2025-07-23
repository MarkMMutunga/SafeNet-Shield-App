# SafeNet Shield - Security Incident Reporting App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)

A secure Android application built with Kotlin for reporting security incidents and managing safety information. The app provides a comprehensive platform for users to report incidents, access safety tips, and manage emergency contacts.

## 🚀 Features

- **🔐 Secure Authentication** - Firebase Auth with enhanced security measures
- **📝 Incident Reporting** - Comprehensive incident reporting with file attachments
- **📍 Location-based Reports** - Country and city selection for accurate reporting
- **📷 Media Support** - Photo capture and document attachment capabilities
- **📱 Emergency Contacts** - Quick access to emergency services
- **💡 Safety Tips** - Educational content for user safety
- **🔍 Report Management** - View and manage submitted reports
- **📊 QR Code Scanning** - Additional functionality for enhanced features

## 🏗️ Architecture

- **Language**: Kotlin
- **UI Framework**: Android View Binding with Material Design
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Networking**: Retrofit + OkHttp
- **Security**: Custom security utilities with 2FA support
- **Build System**: Gradle with ProGuard enabled

## 🔒 Security Features

This application implements multiple layers of security:

- ✅ **Secure Network Communication** - Proper SSL/TLS certificate validation
- ✅ **Input Validation & Sanitization** - Protection against injection attacks
- ✅ **Secure Session Management** - Cryptographically secure session tokens
- ✅ **Rate Limiting** - Protection against brute force attacks
- ✅ **File Upload Security** - File type validation and secure storage
- ✅ **Build Security** - ProGuard obfuscation and debug logging removal

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API 24+
- Kotlin 1.8+
- Firebase account and project

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/MarkMMutunga/safenetshield.git
cd safenetshield
```

### 2. Firebase Configuration
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name: `com.safenet.shield`
3. Download your `google-services.json` file
4. Place it in the `app/` directory
5. Configure Firestore and Storage security rules (see Security Setup below)

### 3. Build and Run
```bash
./gradlew assembleDebug
```

## 🔧 Configuration

### Firebase Security Rules

**Firestore Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /reports/{reportId} {
      allow read, write: if request.auth != null && 
                        request.auth.uid == resource.data.userId;
    }
    match /users/{userId} {
      allow read, write: if request.auth != null && 
                        request.auth.uid == userId;
    }
  }
}
```

**Storage Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /reports/{reportId}/{fileName} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### API Key Security
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Navigate to APIs & Services > Credentials
3. Restrict your API key to Android apps only
4. Add your package name: `com.safenet.shield`
5. Add your SHA-1 certificate fingerprint

## 📁 Project Structure

```
app/src/main/java/com/safenet/shield/
├── auth/                   # Authentication related activities
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   └── AuthenticationManager.kt
├── data/                   # Data models and location data
├── models/                 # Data classes
├── network/                # Network configuration
├── utils/                  # Utility classes (Security, Validation, etc.)
├── MainActivity.kt         # Main application activity
├── ReportActivity.kt       # Incident reporting functionality
└── Other activities...
```

## 🛡️ Security Considerations

### For Developers:
- Never commit `google-services.json` to version control
- Keep API keys secure and restricted
- Regularly update dependencies
- Test security features thoroughly
- Use ProGuard in production builds

### For Production:
- Enable Firebase App Check
- Implement certificate pinning
- Regular security audits
- Monitor for unusual activity
- Keep security rules updated

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines:
- Follow Kotlin coding conventions
- Add security validations for all user inputs
- Write unit tests for critical functions
- Update documentation for new features
- Test on multiple Android versions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/MarkMMutunga/safenetshield/issues) page
2. Create a new issue with detailed description
3. Provide device information and error logs (without sensitive data)

## 🔄 Version History

- **v1.0.0** - Initial release with security enhancements
  - Secure authentication system
  - Incident reporting functionality
  - Firebase integration
  - Enhanced security measures

## ⚠️ Important Notes

- This app contains security-sensitive features
- Always use HTTPS in production
- Regularly update Firebase security rules
- Monitor for security vulnerabilities
- Test thoroughly before deploying

---

**Made with ❤️ for safer communities**
