package com.ravan.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var micButton: Button
    private lateinit var conversationText: TextView
    private lateinit var statusText: TextView
    private lateinit var outerRing: android.widget.ImageView
    private lateinit var dashedRing: android.widget.ImageView
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer

    private var isSpeaking = false
    private val history = mutableListOf<Pair<String, String>>()
    private val handler = Handler(Looper.getMainLooper())

    private val permissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startContinuousListening() else {
            Toast.makeText(this, "Mic permission chahiye Jarvis ke kaam karne ke liye", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        micButton = findViewById(R.id.micButton)
        conversationText = findViewById(R.id.conversationText)
        statusText = findViewById(R.id.statusText)
        outerRing = findViewById(R.id.outerRing)
        dashedRing = findViewById(R.id.dashedRing)

        startAmbientAnimation()
        setupSpeechRecognizer()
        tts = TextToSpeech(this, this)

        micButton.setOnClickListener { requestMicAndListen() }

        requestMicAndListen()
    }

    private fun requestMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startContinuousListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK
            tts.setPitch(0.85f)
            tts.setSpeechRate(0.95f)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    handler.post {
                        setStatus("SYSTEM IDLE")
                        startContinuousListening()
                    }
                }
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    handler.post { startContinuousListening() }
                }
            })
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                setStatus("LISTENING...")
                startRingAnimation()
                animateWaveform()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                stopRingAnimation()
                stopWaveform()
            }
            override fun onError(error: Int) {
                stopRingAnimation()
                stopWaveform()
                if (!isSpeaking) restartListeningDelayed()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()
                if (!spokenText.isNullOrBlank()) {
                    appendToConversation("Aap", spokenText)
                    askJarvis(spokenText)
                } else {
                    restartListeningDelayed()
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startContinuousListening() {
        if (isSpeaking) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            restartListeningDelayed()
        }
    }

    private fun restartListeningDelayed() {
        handler.postDelayed({ if (!isSpeaking) startContinuousListening() }, 800)
    }

    private fun startAmbientAnimation() {
        val slowSpin = android.view.animation.RotateAnimation(
            0f, 360f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        )
        slowSpin.duration = 12000
        slowSpin.repeatCount = android.view.animation.Animation.INFINITE
        slowSpin.interpolator = android.view.animation.LinearInterpolator()
        dashedRing.startAnimation(slowSpin)
    }

    private fun startRingAnimation() {
        val spin = android.view.animation.RotateAnimation(
            0f, 360f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        )
        spin.duration = 3000
        spin.repeatCount = android.view.animation.Animation.INFINITE
        outerRing.startAnimation(spin)
    }

    private fun stopRingAnimation() {
        outerRing.clearAnimation()
    }

    private fun animateWaveform() {
        val bars = listOf<android.view.View>(
            findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3),
            findViewById(R.id.bar4), findViewById(R.id.bar5)
        )
        bars.forEach { bar ->
            val anim = android.animation.ObjectAnimator.ofFloat(bar, "scaleY", 0.4f, 1.8f, 0.6f)
            anim.duration = (300..700).random().toLong()
            anim.repeatCount = android.animation.ObjectAnimator.INFINITE
            anim.repeatMode = android.animation.ObjectAnimator.REVERSE
            anim.start()
            bar.tag = anim
        }
    }

    private fun stopWaveform() {
        val bars = listOf<android.view.View>(
            findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3),
            findViewById(R.id.bar4), findViewById(R.id.bar5)
        )
        bars.forEach { bar ->
            (bar.tag as? android.animation.ObjectAnimator)?.cancel()
            bar.scaleY = 1f
        }
    }

    private fun askJarvis(userText: String) {
        setStatus("PROCESSING...")
        history.add("user" to userText)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (replyText, action) = callClaude(history)
                withContext(Dispatchers.Main) {
                    history.add("assistant" to replyText)
                    appendToConversation("Jarvis", replyText)
                    setStatus("SPEAKING...")
                    speak(replyText)
                    action?.let { performAction(it) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setStatus("ERROR: ${e.message}")
                    restartListeningDelayed()
                }
            }
        }
    }

    private fun callClaude(turns: List<Pair<String, String>>): Pair<String, String?> {
        val client = OkHttpClient()

        val messagesJson = JSONArray()
        messagesJson.put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
        turns.forEach { (role, content) ->
            messagesJson.put(JSONObject().put("role", role).put("content", content))
        }

        val body = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("max_tokens", 400)
            put("messages", messagesJson)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${Config.GROQ_API_KEY}")
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: throw Exception("Khali response Groq se")
            if (!response.isSuccessful) throw Exception("API error: ${response.code} $responseBody")

            val json = JSONObject(responseBody)
            val fullText = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val actionRegex = Regex("""\[ACTION:\s*(.*?)\]""")
            val match = actionRegex.find(fullText)
            val action = match?.groupValues?.get(1)
            val cleanText = fullText.replace(actionRegex, "").trim()

            return cleanText to action
        }
    }

    private fun performAction(action: String) {
        val parts = action.split("|")
        when (parts.getOrNull(0)?.trim()?.lowercase()) {
            "alarm" -> {
                val timeParts = parts.getOrNull(1)?.split(":") ?: return
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return
                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                }
                startActivity(intent)
            }
            "whatsapp" -> {
                val number = parts.getOrNull(1)?.trim() ?: return
                val message = parts.getOrNull(2)?.trim() ?: ""
                val uri = android.net.Uri.parse("https://wa.me/$number?text=${android.net.Uri.encode(message)}")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            "call" -> {
                val number = parts.getOrNull(1)?.trim() ?: return
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(Intent.ACTION_CALL, android.net.Uri.parse("tel:$number"))
                    startActivity(intent)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 101)
                }
            }
            "open_app" -> {
                val packageName = parts.getOrNull(1)?.trim() ?: return
                packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
            }
        }
    }

    private fun speak(text: String) {
        isSpeaking = true
        val params = Bundle()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "jarvis_reply")
    }

    private fun appendToConversation(speaker: String, text: String) {
        conversationText.append("\n\n$speaker: $text")
    }

    private fun setStatus(text: String) {
        statusText.text = text
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val SYSTEM_PROMPT = """
You are Jarvis, a private voice assistant on the user's personal phone. The user
runs a cricket bat business (Hard/Soft Tennis Ball bats, Kashmir/English willow)
and speaks Hinglish. Reply naturally in the same language style as the user,
keep spoken replies short and conversational since they will be read aloud by
text-to-speech.

If the user asks you to do one of these specific actions, end your reply with
an action tag in EXACTLY this format on its own, after your spoken reply:
- Set an alarm: [ACTION: alarm|HH:MM]  (24-hour time)
- Send a WhatsApp message: [ACTION: whatsapp|PHONE_NUMBER_WITH_COUNTRY_CODE|MESSAGE_TEXT]
- Make a phone call: [ACTION: call|PHONE_NUMBER]
- Open an app: [ACTION: open_app|PACKAGE_NAME]  (only if you know the exact Android package name)

Only include an action tag when the user's request clearly matches one of these.
Otherwise just reply normally with no tag. Never invent phone numbers — ask the
user for the number if they haven't given it.
"""
    }
}
