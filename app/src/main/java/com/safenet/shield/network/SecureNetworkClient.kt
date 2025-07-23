package com.safenet.shield.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SecureNetworkClient {
    private const val TAG = "SecureNetworkClient"
    private const val BASE_URL = "https://api.example.com/" // Replace with your actual API base URL
    private const val TIMEOUT_SECONDS = 30L
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Only log in debug builds to prevent sensitive data exposure
        level = HttpLoggingInterceptor.Level.NONE // Disabled for security
    }

    // Removed insecure custom TrustManager - using system default for proper certificate validation
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Using system default SSL configuration for proper certificate validation
        // Using system default SSL configuration for proper certificate validation
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
} 