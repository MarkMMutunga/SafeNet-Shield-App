/*
 * SafeNet Shield - Personal Safety & Security Application
 * 
 * Copyright (c) 2024 Mark Mikile Mutunga
 * Email: markmiki03@gmail.com
 * Phone: +254 707 678 643
 * 
 * All rights reserved. This software and associated documentation files (the "Software"),
 * are proprietary to Mark Mikile Mutunga. Unauthorized copying, distribution, or modification
 * of this software is strictly prohibited without explicit written permission from the author.
 * 
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a particular
 * purpose and noninfringement. In no event shall the author be liable for any claim,
 * damages or other liability, whether in an action of contract, tort or otherwise,
 * arising from, out of or in connection with the software or the use or other dealings
 * in the software.
 */

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