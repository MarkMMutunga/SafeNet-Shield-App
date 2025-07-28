package com.safenet.shield

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/**
 * Wearable Integration Activity
 * Manages smartwatch and wearable device integration
 */
class WearableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scrollView = ScrollView(this).apply {
            setPadding(32, 32, 32, 32)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "Wearable Devices"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)

        val features = listOf(
            "⌚ Smartwatch Integration",
            "🚨 Emergency SOS",
            "📍 Location Tracking", 
            "💓 Health Monitoring",
            "🔔 Safety Alerts"
        )

        features.forEach { feature ->
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }

            val featureText = TextView(this).apply {
                text = feature
                textSize = 18f
            }

            card.addView(featureText)
            layout.addView(card)
        }

        val comingSoon = TextView(this).apply {
            text = "\n🔄 Wearable integration coming soon!\n\nSupported devices:\n• Apple Watch\n• Samsung Galaxy Watch\n• Fitbit\n• Wear OS devices"
            textSize = 16f
            setPadding(32, 32, 32, 32)
            background = getDrawable(android.R.drawable.editbox_background)
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(comingSoon)

        scrollView.addView(layout)
        setContentView(scrollView)

        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Wearable Devices"
            setDisplayHomeAsUpEnabled(true)
            subtitle = "Smart Device Integration"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
