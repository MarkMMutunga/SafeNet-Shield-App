package com.safenet.shield

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.safenet.shield.ai.AIAssistantFragment
import com.safenet.shield.ai.ThreatAnalysisFragment
import com.safenet.shield.ai.PredictionsFragment
import com.safenet.shield.ai.SafetyRecommendationsFragment

/**
 * AI Safety Assistant Activity
 * Comprehensive AI-powered safety and threat analysis
 */
class AIAssistantActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_assistant)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "AI Safety Assistant"
            setDisplayHomeAsUpEnabled(true)
            subtitle = "Intelligent Protection"
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = AIPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Assistant"
                1 -> "Analysis"
                2 -> "Predictions"
                3 -> "Recommendations"
                else -> "Tab"
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_ai_assistant
                1 -> R.drawable.ic_analysis
                2 -> R.drawable.ic_predictions
                3 -> R.drawable.ic_recommendations
                else -> R.drawable.ic_tab_default
            })
        }.attach()
    }

    private class AIPagerAdapter(activity: AppCompatActivity) : 
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AIAssistantFragment()
                1 -> ThreatAnalysisFragment()
                2 -> PredictionsFragment()
                3 -> SafetyRecommendationsFragment()
                else -> AIAssistantFragment()
            }
        }
    }
}
