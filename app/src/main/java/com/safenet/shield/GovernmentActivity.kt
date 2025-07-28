package com.safenet.shield

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.safenet.shield.government.ReportingFragment
import com.safenet.shield.government.CaseTrackingFragment
import com.safenet.shield.government.ComplianceFragment
import com.safenet.shield.government.AgencyContactsFragment

/**
 * Government & Law Enforcement Integration Activity
 * Provides direct access to official reporting channels
 */
class GovernmentActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_government)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Government Services"
            setDisplayHomeAsUpEnabled(true)
            subtitle = "Official Reporting Channels"
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = GovernmentPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "File Report"
                1 -> "Track Cases"
                2 -> "Compliance"
                3 -> "Contacts"
                else -> "Tab"
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_report_government
                1 -> R.drawable.ic_track_case
                2 -> R.drawable.ic_compliance
                3 -> R.drawable.ic_contacts_gov
                else -> R.drawable.ic_tab_default
            })
        }.attach()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private class GovernmentPagerAdapter(activity: AppCompatActivity) : 
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ReportingFragment()
                1 -> CaseTrackingFragment()
                2 -> ComplianceFragment()
                3 -> AgencyContactsFragment()
                else -> ReportingFragment()
            }
        }
    }
}
