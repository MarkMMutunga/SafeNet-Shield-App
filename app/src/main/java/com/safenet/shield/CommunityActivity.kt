package com.safenet.shield

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.safenet.shield.community.CommunityIntelFragment
import com.safenet.shield.community.ThreatMapFragment
import com.safenet.shield.community.SafetyAlertsFragment
import com.safenet.shield.community.VerificationFragment

/**
 * Community Intelligence Hub Activity
 * Provides access to community-driven safety features
 */
class CommunityActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Community Intelligence"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = CommunityPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Threat Intel"
                1 -> "Safety Map"
                2 -> "Alerts"
                3 -> "Verification"
                else -> "Tab"
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_intelligence
                1 -> R.drawable.ic_map
                2 -> R.drawable.ic_alert
                3 -> R.drawable.ic_verified
                else -> R.drawable.ic_tab_default
            })
        }.attach()
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

    private class CommunityPagerAdapter(activity: AppCompatActivity) : 
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CommunityIntelFragment()
                1 -> ThreatMapFragment()
                2 -> SafetyAlertsFragment()
                3 -> VerificationFragment()
                else -> CommunityIntelFragment()
            }
        }
    }
}
