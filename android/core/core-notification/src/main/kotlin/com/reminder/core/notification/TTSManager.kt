package com.reminder.core.notification

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSManager private constructor(private val appContext: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()
    private var mediaPlayer: MediaPlayer? = null

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
                isInitialized = true
                // Flush queued messages from before TTS was ready
                synchronized(pendingMessages) {
                    pendingMessages.forEach { msg ->
                        tts?.speak(msg, TextToSpeech.QUEUE_ADD, null, null)
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
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    /**
     * Play audio file from res/raw/ by resource ID (e.g. R.raw.drinkinng)
     */
    fun playRawAudio(rawResId: Int) {
        stopAudio()
        mediaPlayer = MediaPlayer.create(appContext, rawResId).apply {
            setOnCompletionListener { it.release() }
            setOnErrorListener { mp, _, _ -> mp.release(); true }
            start()
        }
    }

    fun stop() {
        tts?.stop()
        stopAudio()
    }

    private fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        stopAudio()
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
