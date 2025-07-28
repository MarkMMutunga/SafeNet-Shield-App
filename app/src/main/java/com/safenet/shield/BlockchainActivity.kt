package com.safenet.shield

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.safenet.shield.blockchain.EvidenceStorageFragment
import com.safenet.shield.blockchain.VerificationFragment
import com.safenet.shield.blockchain.BlockchainExplorerFragment
import com.safenet.shield.blockchain.DigitalSigningFragment

/**
 * Blockchain Evidence System Activity
 * Immutable evidence storage and verification
 */
class BlockchainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blockchain)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Blockchain Evidence"
            setDisplayHomeAsUpEnabled(true)
            subtitle = "Immutable Digital Evidence"
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = BlockchainPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Store Evidence"
                1 -> "Verify"
                2 -> "Explorer"
                3 -> "Sign"
                else -> "Tab"
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_blockchain_store
                1 -> R.drawable.ic_verify
                2 -> R.drawable.ic_explorer
                3 -> R.drawable.ic_digital_sign
                else -> R.drawable.ic_tab_default
            })
        }.attach()
    }

    private class BlockchainPagerAdapter(activity: AppCompatActivity) : 
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> EvidenceStorageFragment()
                1 -> VerificationFragment()
                2 -> BlockchainExplorerFragment()
                3 -> DigitalSigningFragment()
                else -> EvidenceStorageFragment()
            }
        }
    }
}
