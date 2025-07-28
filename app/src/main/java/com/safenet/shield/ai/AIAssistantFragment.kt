package com.safenet.shield.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AIAssistantFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val scrollView = ScrollView(context).apply {
            setPadding(32, 32, 32, 32)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val title = TextView(context).apply {
            text = "AI Safety Assistant"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        // Chat interface
        val chatCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            ).apply { setMargins(0, 0, 0, 16) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }
        
        val chatScrollView = ScrollView(context)
        val chatHistory = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // Sample chat messages
        val sampleMessages = listOf(
            "👤 How can I protect myself from phishing emails?",
            "🤖 Here are key signs of phishing emails:\n• Check sender address carefully\n• Look for spelling/grammar errors\n• Don't click suspicious links\n• Verify requests independently",
            "👤 What should I do if I think I've been hacked?",
            "🤖 Immediate steps:\n1. Change all passwords\n2. Enable 2FA\n3. Run antivirus scan\n4. Check account activity\n5. Report to relevant authorities"
        )
        
        sampleMessages.forEach { message ->
            val messageView = TextView(context).apply {
                text = message
                textSize = 14f
                setPadding(16, 12, 16, 12)
                val isUser = message.startsWith("👤")
                if (isUser) {
                    background = context.getDrawable(android.R.drawable.editbox_background)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 0) }
                } else {
                    alpha = 0.9f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 4, 0, 8) }
                }
            }
            chatHistory.addView(messageView)
        }
        
        chatScrollView.addView(chatHistory)
        chatCard.addView(chatScrollView)
        layout.addView(chatCard)
        
        // Input area
        val inputCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }
        
        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val messageInput = EditText(context).apply {
            hint = "Ask about safety concerns..."
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            background = context.getDrawable(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        inputLayout.addView(messageInput)
        
        val sendButton = MaterialButton(context).apply {
            text = "Send"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 0, 0, 0) }
            setOnClickListener {
                val message = messageInput.text.toString()
                if (message.isNotBlank()) {
                    // Add user message
                    val userMessage = TextView(context).apply {
                        text = "👤 $message"
                        textSize = 14f
                        setPadding(16, 12, 16, 12)
                        background = context.getDrawable(android.R.drawable.editbox_background)
                    }
                    chatHistory.addView(userMessage)
                    
                    // Add AI response
                    val aiResponse = TextView(context).apply {
                        text = "🤖 I understand your concern about '$message'. For specific advice, please consult our safety guidelines or contact relevant authorities."
                        textSize = 14f
                        setPadding(16, 12, 16, 12)
                        alpha = 0.9f
                    }
                    chatHistory.addView(aiResponse)
                    
                    messageInput.text.clear()
                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
        }
        inputLayout.addView(sendButton)
        
        inputCard.addView(inputLayout)
        layout.addView(inputCard)
        
        // Quick actions
        val actionsCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardElevation = 8f
            radius = 12f
            setPadding(24, 24, 24, 24)
        }
        
        val actionsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val actionsTitle = TextView(context).apply {
            text = "Quick Actions"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        actionsLayout.addView(actionsTitle)
        
        val quickActions = listOf(
            "Security Checkup",
            "Threat Assessment",
            "Safety Recommendations",
            "Emergency Contacts"
        )
        
        quickActions.forEach { action ->
            val actionButton = MaterialButton(context).apply {
                text = action
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 8) }
                setOnClickListener {
                    Toast.makeText(context, "$action initiated", Toast.LENGTH_SHORT).show()
                }
            }
            actionsLayout.addView(actionButton)
        }
        
        actionsCard.addView(actionsLayout)
        layout.addView(actionsCard)
        
        scrollView.addView(layout)
        return scrollView
    }
}
