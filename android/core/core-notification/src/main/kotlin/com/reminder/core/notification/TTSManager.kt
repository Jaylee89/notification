package com.reminder.core.notification

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TTSManager private constructor(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
                isInitialized = true
                // Flush queued messages from before TTS was ready
                synchronized(pendingMessages) {
                    pendingMessages.forEach { msg ->
                        tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    pendingMessages.clear()
                }
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            synchronized(pendingMessages) {
                if (!isInitialized) {
                    pendingMessages.add(text)
                    return
                }
                // Double-check: TTS may have become ready while we waited for lock
            }
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        @Volatile
        private var instance: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            return instance ?: synchronized(this) {
                instance ?: TTSManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
