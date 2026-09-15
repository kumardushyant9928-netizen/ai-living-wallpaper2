package com.ailivingworld.wallpaper.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.TextView
import com.ailivingworld.wallpaper.R
import timber.log.Timber

/**
 * Settings Activity - configure AI provider and character
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var providerSpinner: Spinner
    private lateinit var apiKeyInput: EditText
    private lateinit var endpointInput: EditText
    private lateinit var modelIdInput: EditText
    private lateinit var saveButton: Button
    private lateinit var testButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Timber.plant(Timber.DebugTree())

        prefs = getSharedPreferences("ai_config", Context.MODE_PRIVATE)

        // Initialize views
        providerSpinner = findViewById(R.id.provider_spinner)
        apiKeyInput = findViewById(R.id.api_key_input)
        endpointInput = findViewById(R.id.endpoint_input)
        modelIdInput = findViewById(R.id.model_id_input)
        saveButton = findViewById(R.id.save_button)
        testButton = findViewById(R.id.test_button)
        statusText = findViewById(R.id.status_text)

        // Setup provider spinner
        val providers = arrayOf("OpenAI", "Hugging Face", "Custom API")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        providerSpinner.adapter = adapter

        // Load saved configuration
        loadConfiguration()

        // Setup button listeners
        saveButton.setOnClickListener { saveConfiguration() }
        testButton.setOnClickListener { testAIConnection() }

        providerSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                updateUIForProvider(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
    }

    private fun loadConfiguration() {
        val providerType = prefs.getString("provider_type", "openai") ?: "openai"
        val apiKey = prefs.getString("api_key", "") ?: ""
        val endpoint = prefs.getString("endpoint", "") ?: ""
        val modelId = prefs.getString("model_id", "") ?: ""

        apiKeyInput.setText(apiKey)
        endpointInput.setText(endpoint)
        modelIdInput.setText(modelId)

        val position = when (providerType.lowercase()) {
            "openai" -> 0
            "huggingface" -> 1
            "custom" -> 2
            else -> 0
        }
        providerSpinner.setSelection(position)

        updateUIForProvider(position)
        Timber.d("Configuration loaded: $providerType")
    }

    private fun updateUIForProvider(position: Int) {
        when (position) {
            0 -> { // OpenAI
                endpointInput.isEnabled = false
                modelIdInput.isEnabled = false
                modelIdInput.hint = "N/A - Uses GPT-3.5-turbo"
                endpointInput.hint = "N/A - Uses OpenAI API"
            }
            1 -> { // Hugging Face
                endpointInput.isEnabled = false
                modelIdInput.isEnabled = true
                endpointInput.hint = "N/A - Uses Hugging Face API"
                modelIdInput.hint = "e.g., gpt2, distilgpt2"
            }
            2 -> { // Custom
                endpointInput.isEnabled = true
                modelIdInput.isEnabled = true
                endpointInput.hint = "e.g., https://your-api.com/v1/chat/completions"
                modelIdInput.hint = "Optional: model name or ID"
            }
        }
    }

    private fun saveConfiguration() {
        val apiKey = apiKeyInput.text.toString().trim()
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "API Key is required!", Toast.LENGTH_SHORT).show()
            return
        }

        val providerType = when (providerSpinner.selectedItemPosition) {
            0 -> "openai"
            1 -> "huggingface"
            2 -> "custom"
            else -> "openai"
        }

        prefs.edit().apply {
            putString("provider_type", providerType)
            putString("api_key", apiKey)
            putString("endpoint", endpointInput.text.toString())
            putString("model_id", modelIdInput.text.toString())
            apply()
        }

        statusText.text = "✅ Configuration saved for $providerType"
        Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show()
        Timber.d("Configuration saved: $providerType")
    }

    private fun testAIConnection() {
        statusText.text = "🔄 Testing AI connection..."
        // Implementation would test the connection
        Toast.makeText(this, "Connection test started", Toast.LENGTH_SHORT).show()
    }
}

// Extension: Disable EditText by setting alpha and input type
private var EditText.isEnabled: Boolean
    get() = isEnabled
    set(value) {
        this.isEnabled = value
        this.alpha = if (value) 1f else 0.5f
    }