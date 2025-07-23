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
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safenet.shield.databinding.ActivityViewReportsBinding
import java.text.SimpleDateFormat
import java.util.*

class ViewReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewReportsBinding
    private val TAG = "ViewReportsActivity"
    private lateinit var reportsAdapter: ReportsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadReports()
    }

    private fun setupUI() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Reports"

        // Set up RecyclerView
        reportsAdapter = ReportsAdapter()
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewReportsActivity)
            adapter = reportsAdapter
        }

        // Set up FAB
        binding.fabNewReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }

    private fun loadReports() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.noReportsText.visibility = View.GONE

        db.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                binding.progressBar.visibility = View.GONE

                if (e != null) {
                    Log.e(TAG, "Error loading reports", e)
                    Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val reports = snapshot.documents.map { doc ->
                        Report(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                    reportsAdapter.submitList(reports)
                    binding.noReportsText.visibility = View.GONE
                } else {
                    reportsAdapter.submitList(emptyList())
                    binding.noReportsText.visibility = View.VISIBLE
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 