package com.safenet.shield.models

data class User(
    val email: String,
    val passwordHash: String,
    val is2FAEnabled: Boolean = false,
    val twoFASecret: String? = null
) 