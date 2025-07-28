package com.safenet.shield.blockchain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class EvidenceStorageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply { setPadding(32, 32, 32, 32) }
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        
        val title = TextView(context).apply {
            text = "Evidence Storage"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        val evidenceTypes = listOf(
            "📄 Documents & Reports",
            "📷 Photos & Screenshots", 
            "🎥 Video Evidence",
            "🎵 Audio Recordings",
            "💾 Digital Files"
        )
        
        evidenceTypes.forEach { type ->
            val card = MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                cardElevation = 8f
                radius = 12f
                setPadding(24, 24, 24, 24)
            }
            
            val typeText = TextView(context).apply {
                text = type
                textSize = 18f
            }
            
            card.addView(typeText)
            layout.addView(card)
        }
        
        scrollView.addView(layout)
        return scrollView
    }
}
