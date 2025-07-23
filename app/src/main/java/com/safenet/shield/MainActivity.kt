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

package com.safenet.shield

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safenet.shield.databinding.ActivityMainBinding
import com.safenet.shield.auth.LoginActivity
import com.safenet.shield.utils.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var securityUtils: SecurityUtils
    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            securityUtils = SecurityUtils.getInstance(this)
            auth = FirebaseAuth.getInstance()

            // Check session validity
            if (!securityUtils.isValidSession()) {
                Log.d(TAG, "Invalid session, redirecting to login")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }

            // Set up UI
            setupUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupUI() {
        try {
            // Get current user and update welcome message
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.reload().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displayName = currentUser.displayName ?: "User"
                        val capitalizedName = displayName.split(" ").joinToString(" ") { 
                            it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                        binding.welcomeText.text = "Welcome, $capitalizedName!"
                    } else {
                        binding.welcomeText.text = "Welcome, User!"
                    }
                }
            } else {
                binding.welcomeText.text = "Welcome, User!"
            }

            // Set up report button
            binding.reportButton.setOnClickListener {
                startActivity(Intent(this, ReportActivity::class.java))
            }

            // Set up safety tips button
            binding.safetyTipsButton.setOnClickListener {
                startActivity(Intent(this, SafetyTipsActivity::class.java))
            }

            // Set up emergency contacts button
            binding.emergencyContactsButton.setOnClickListener {
                startActivity(Intent(this, EmergencyContactsActivity::class.java))
            }

            // Set up logout button
            binding.logoutButton.setOnClickListener {
                securityUtils.clearSession()
                securityUtils.setLoggedIn(false)
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupUI", e)
            binding.welcomeText.text = "Welcome, User!"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_view_reports -> {
                startActivity(Intent(this, ViewReportsActivity::class.java))
                true
            }
            R.id.menu_emergency_contacts -> {
                startActivity(Intent(this, EmergencyContactsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 