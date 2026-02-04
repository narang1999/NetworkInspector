package com.networkinspector.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.networkinspector.AnalyticsInspector
import com.networkinspector.R
import com.networkinspector.core.AnalyticsEvent
import com.networkinspector.core.AnalyticsSource

class AnalyticsListActivity : AppCompatActivity(), AnalyticsInspector.EventListener {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private var currentFilter: AnalyticsSource? = null
    private var searchQuery: String = ""
    
    private lateinit var rvEvents: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvEventCount: TextView
    private lateinit var etSearch: EditText
    
    private val adapter = EventAdapter { event ->
        startActivity(AnalyticsDetailActivity.intent(this, event.id))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_analytics_list)
            
            setupViews()
            setupToolbar()
            setupSearch()
            setupFilters()
            setupRecyclerView()
            
            AnalyticsInspector.addListener(this)
            
            // Add sample events if empty (for demo/testing)
            if (AnalyticsInspector.getEventCount() == 0) {
                AnalyticsInspector.addSampleEvents()
            }
            
            updateList()
        } catch (e: Throwable) {
            finish()
        }
    }
    
    override fun onDestroy() {
        try {
            AnalyticsInspector.removeListener(this)
        } catch (e: Throwable) { }
        super.onDestroy()
    }
    
    private fun setupViews() {
        rvEvents = findViewById(R.id.rvEvents)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvEventCount = findViewById(R.id.tvEventCount)
        etSearch = findViewById(R.id.etSearch)
        
        findViewById<TextView>(R.id.tvClear).setOnClickListener {
            showClearDialog()
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
    
    private fun setupSearch() {
        etSearch.doAfterTextChanged { text ->
            searchQuery = text?.toString() ?: ""
            updateList()
        }
    }
    
    private fun setupFilters() {
        val chipAll = findViewById<Chip>(R.id.chipAll)
        val chipFirebase = findViewById<Chip>(R.id.chipFirebase)
        val chipClevertap = findViewById<Chip>(R.id.chipClevertap)
        val chipAppsflyer = findViewById<Chip>(R.id.chipAppsflyer)
        val chipFacebook = findViewById<Chip>(R.id.chipFacebook)
        
        val chips = listOf(chipAll, chipFirebase, chipClevertap, chipAppsflyer, chipFacebook)
        
        fun updateSelection(selected: Chip) {
            chips.forEach { it.isChecked = it == selected }
        }
        
        chipAll.setOnClickListener {
            currentFilter = null
            updateSelection(chipAll)
            updateList()
        }
        
        chipFirebase.setOnClickListener {
            currentFilter = AnalyticsSource.FIREBASE
            updateSelection(chipFirebase)
            updateList()
        }
        
        chipClevertap.setOnClickListener {
            currentFilter = AnalyticsSource.CLEVERTAP
            updateSelection(chipClevertap)
            updateList()
        }
        
        chipAppsflyer.setOnClickListener {
            currentFilter = AnalyticsSource.APPSFLYER
            updateSelection(chipAppsflyer)
            updateList()
        }
        
        chipFacebook.setOnClickListener {
            currentFilter = AnalyticsSource.FACEBOOK
            updateSelection(chipFacebook)
            updateList()
        }
    }
    
    private fun setupRecyclerView() {
        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = adapter
        rvEvents.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
    
    private fun updateList() {
        try {
            var events = if (currentFilter != null) {
                AnalyticsInspector.getEvents(currentFilter!!)
            } else {
                AnalyticsInspector.getEvents()
            }
            
            if (searchQuery.isNotBlank()) {
                events = events.filter { it.matchesSearch(searchQuery) }
            }
            
            adapter.updateEvents(events)
            tvEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            tvEventCount.text = "${events.size} events"
        } catch (e: Throwable) { }
    }
    
    private fun showClearDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Clear Events")
                .setMessage("Delete all analytics events?")
                .setPositiveButton("Clear") { _, _ -> AnalyticsInspector.clearAll() }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Throwable) { }
    }
    
    override fun onEventsUpdated(events: List<AnalyticsEvent>) {
        mainHandler.post { updateList() }
    }
    
    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, AnalyticsListActivity::class.java)
        }
    }
    
    // ==================== Adapter ====================
    
    private class EventAdapter(
        private val onItemClick: (AnalyticsEvent) -> Unit
    ) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
        
        private val events = mutableListOf<AnalyticsEvent>()
        
        fun updateEvents(newEvents: List<AnalyticsEvent>) {
            events.clear()
            events.addAll(newEvents)
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_analytics_event, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(events[position])
        }
        
        override fun getItemCount() = events.size
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvSource: TextView = view.findViewById(R.id.tvSource)
            private val tvEventName: TextView = view.findViewById(R.id.tvEventName)
            private val tvParamsPreview: TextView = view.findViewById(R.id.tvParamsPreview)
            private val tvTime: TextView = view.findViewById(R.id.tvTime)
            private val tvParamsCount: TextView = view.findViewById(R.id.tvParamsCount)
            
            fun bind(event: AnalyticsEvent) {
                tvSource.text = event.source.name
                tvSource.background = createSourceBadge(event.source)
                tvEventName.text = event.eventName
                tvTime.text = event.formattedTime
                tvParamsCount.text = "${event.paramsCount} params"
                
                val paramsPreview = event.params.entries
                    .take(3)
                    .joinToString(", ") { "${it.key}=${it.value}" }
                tvParamsPreview.text = paramsPreview.ifEmpty { "No parameters" }
                tvParamsPreview.visibility = View.VISIBLE
                
                itemView.setOnClickListener { onItemClick(event) }
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
                    cornerRadius = 4f * itemView.resources.displayMetrics.density
                }
            }
        }
    }
}

