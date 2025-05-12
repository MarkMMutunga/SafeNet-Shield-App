package com.example.myapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.safenet.shield.R

class ViewReportsActivity : AppCompatActivity() {
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var fabNewReport: FloatingActionButton
    private lateinit var reportsAdapter: ReportsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reports)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView)
        fabNewReport = findViewById(R.id.fabNewReport)

        // Setup RecyclerView
        reportsAdapter = ReportsAdapter()
        reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewReportsActivity)
            adapter = reportsAdapter
        }

        // Load reports
        loadReports()

        // Setup FAB click listener
        fabNewReport.setOnClickListener {
            // TODO: Navigate to create report activity
        }
    }

    private fun loadReports() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("reports")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    val reports = mutableListOf<Report>()
                    snapshot?.documents?.forEach { document ->
                        val report = document.toObject(Report::class.java)
                        report?.let { reports.add(it) }
                    }
                    reportsAdapter.submitList(reports)
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 