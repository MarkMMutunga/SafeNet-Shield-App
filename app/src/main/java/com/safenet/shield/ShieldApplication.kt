package com.safenet.shield

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class ShieldApplication : Application() {
    private val TAG = "ShieldApplication"

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }
} 