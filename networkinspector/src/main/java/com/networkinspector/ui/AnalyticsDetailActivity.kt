package com.networkinspector.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.gson.GsonBuilder
import com.networkinspector.AnalyticsInspector
import com.networkinspector.R
import com.networkinspector.core.AnalyticsEvent
import com.networkinspector.core.AnalyticsSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyticsDetailActivity : AppCompatActivity() {
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    private var event: AnalyticsEvent? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_analytics_detail)
            
            val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: run {
                finish()
                return
            }
            
            event = AnalyticsInspector.getEvent(eventId) ?: run {
                finish()
                return
            }
            
            setupToolbar()
            bindData()
            setupCopyButton()
        } catch (e: Throwable) {
            finish()
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
    
    private fun bindData() {
        val event = this.event ?: return
        
        val tvSource = findViewById<TextView>(R.id.tvSource)
        val tvTimestamp = findViewById<TextView>(R.id.tvTimestamp)
        val tvEventName = findViewById<TextView>(R.id.tvEventName)
        val tvParamsCount = findViewById<TextView>(R.id.tvParamsCount)
        val tvParams = findViewById<TextView>(R.id.tvParams)
        
        tvSource.text = event.source.name
        tvSource.background = createSourceBadge(event.source)
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS", Locale.getDefault())
        tvTimestamp.text = dateFormat.format(Date(event.timestamp))
        
        tvEventName.text = event.eventName
        tvParamsCount.text = "${event.paramsCount} parameters"
        
        if (event.params.isEmpty()) {
            tvParams.text = "No parameters"
        } else {
            try {
                tvParams.text = gson.toJson(event.params)
            } catch (e: Throwable) {
                tvParams.text = event.params.entries
                    .joinToString("\n") { "${it.key} = ${it.value}" }
            }
        }
    }
    
    private fun setupCopyButton() {
        findViewById<MaterialButton>(R.id.btnCopy).setOnClickListener {
            try {
                val event = this.event ?: return@setOnClickListener
                val text = buildString {
                    appendLine("Event: ${event.eventName}")
                    appendLine("Source: ${event.source.name}")
                    appendLine("Time: ${event.formattedTime}")
                    appendLine()
                    appendLine("Parameters:")
                    event.params.forEach { (key, value) ->
                        appendLine("  $key = $value")
                    }
                }
                
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Analytics Event", text))
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Toast.makeText(this, "Failed to copy", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun createSourceBadge(source: AnalyticsSource): GradientDrawable {
        val color = when (source) {
            AnalyticsSource.FIREBASE -> Color.parseColor("#FFA000")
            AnalyticsSource.CLEVERTAP -> Color.parseColor("#D32F2F")
            AnalyticsSource.APPSFLYER -> Color.parseColor("#4CAF50")
            AnalyticsSource.FACEBOOK -> Color.parseColor("#1877F2")
            AnalyticsSource.CUSTOM -> Color.parseColor("#9E9E9E")
        }
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 4f * resources.displayMetrics.density
        }
    }
    
    companion object {
        private const val EXTRA_EVENT_ID = "event_id"
        
        fun intent(context: Context, eventId: String): Intent {
            return Intent(context, AnalyticsDetailActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
        }
    }
}

