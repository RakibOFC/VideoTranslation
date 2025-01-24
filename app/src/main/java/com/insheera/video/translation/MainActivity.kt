package com.insheera.video.translation

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import java.util.Locale


@UnstableApi
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var subtitleTextView: TextView
    private lateinit var textToSpeech: TextToSpeech
    private val handler = Handler(Looper.getMainLooper())
    private var isTAudioOn = true

    data class Subtitle(
        val id: Int,
        val startTime: Long,
        val endTime: Long,
        val text: String
    )

    private val subtitles = arrayListOf(
        Subtitle(1, 0, 3500, "Sir, how did you get so much knowledge"),
        Subtitle(2, 3500, 6500, "about Islam by studying in general line?"),
        Subtitle(3, 6501, 8000, "We who study in normal line,"),
        Subtitle(4, 8001, 10000, "How will we learn about Islam?")

        /*
        Subtitle(1, 0, 6500, "Sir, how did you get so much knowledge about Islam by studying in general line?"),
        Subtitle(2, 6501, 10000, "How can we who study in normal lines know about Islam?")
        ---------------------------
        Subtitle(1, 0, 2200, "O Allah, give me so much patience"),
        Subtitle(2, 2201, 5497, "As if in the words of any person in the world"), // 5497
        Subtitle(3, 5498, 7094, "Do not be heartbroken")
        */
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTAudio = findViewById<Button>(R.id.btnTAudio)
        textToSpeech = TextToSpeech(this, this)
        initializePlayer()

        btnTAudio.setOnClickListener {
            isTAudioOn = false
            player.volume = 1f
            textToSpeech.stop()
        }

        convertTextToSpeechAndPlay("This is you ")
    }

    private fun initializePlayer() {
        playerView = findViewById(R.id.playerView)
        subtitleTextView = findViewById(R.id.subtitleTextView)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.sample_video2}")
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)

        player.prepare()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    Log.e("TAG", "playing")
                    startRunnableHandler()

                } else {
                    Log.e("TAG", "paused")
                    stopRunnableHandler()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.e("TAG", "ready")
                    }

                    Player.STATE_ENDED -> {
                        Log.e("TAG", "ended")
                        stopRunnableHandler()
                    }

                    Player.STATE_BUFFERING -> {
                        Log.e("TAG", "buffering")
                    }

                    Player.STATE_IDLE -> {
                        Log.e("TAG", "idle")
                    }
                }
            }
        })
    }

    private fun stopRunnableHandler() {
        textToSpeech.stop()
        handler.removeCallbacks(runnable)
    }

    private fun startRunnableHandler() {
        try {
            handler.removeCallbacks(runnable)
        } catch (_: NullPointerException) {

        }
        handler.post(runnable)
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                updateSubtitle(player.currentPosition)
            }
            handler.postDelayed(this, 300)
        }
    }

    private var lastSubtitleId: Int? = null

    private fun updateSubtitle(currentPosition: Long) {
        val currentSubtitle = subtitles.find {
            currentPosition >= it.startTime && currentPosition < it.endTime
        }
        if (currentSubtitle?.id != lastSubtitleId) {
            subtitleTextView.visibility = if (currentSubtitle != null) {

                val subtitleText = currentSubtitle.text
                if (isTAudioOn) {
                    val subtitleDurationMs =
                        (currentSubtitle.endTime - currentSubtitle.startTime).toFloat()

                    val wordsPerSecond = calculateWordsPerSecond(subtitleText)
                    val estimatedSpeakTimeMs = calculateSpeechDuration(subtitleText, wordsPerSecond)
                    val adjustedSpeechRate = estimatedSpeakTimeMs / subtitleDurationMs
                    textToSpeech.setSpeechRate(adjustedSpeechRate)
                    speakText(subtitleText)
                }

                subtitleTextView.text = subtitleText
                View.VISIBLE
            } else {
                View.GONE
            }
            lastSubtitleId = currentSubtitle?.id
        }
    }

    private fun calculateSpeechDuration(text: String, wordsPerSecond: Float): Float {
        val wordCount = text.split(" ").size
        return (wordCount / wordsPerSecond) * 1000
    }

    private fun speakText(text: String) {
        player.volume = 0f
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
    }

    override fun onStop() {
        super.onStop()

        player.release()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        player.release()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
            textToSpeech.setPitch(0.0f)
        } else {
            Log.e("TTS", "Initialization failed!")
        }
    }

    private fun calculateWordsPerSecond(text: String): Float {
        val wordCount = text.split(" ").size

        // Estimate syllables dynamically
        val syllableCount = estimateSyllables(text)

        // Adjust wordsPerSecond dynamically based on sentence length and complexity
        return if (wordCount > 0) {
            // Short sentences can be spoken faster, while long sentences slower
            when {
                syllableCount > 15 -> 1.8f // For complex or long sentences
                syllableCount > 8 -> 2.2f // Medium complexity
                else -> 2.5f // Short, simple sentences
            }
        } else {
            2.5f // Default value
        }
    }

    private fun estimateSyllables(text: String): Int {

        val vowels = "aeiou"
        val words = text.split(" ")
        var syllableCount = 0

        for (word in words) {
            val cleanWord = word.lowercase(Locale.getDefault()).replace("[^a-z]".toRegex(), "")
            var wordSyllables = 0
            var isPrevVowel = false

            for (char in cleanWord) {
                val isVowel = vowels.contains(char)
                if (isVowel && !isPrevVowel) {
                    wordSyllables++
                }
                isPrevVowel = isVowel
            }

            // Adjustments for special cases (common in English)
            if (cleanWord.endsWith("e") && wordSyllables > 1) {
                wordSyllables-- // Silent 'e' at the end of the word
            }
            syllableCount += wordSyllables
        }

        return syllableCount
    }
}

    /*
    Bengali:
    হে আল্লাহ, আমাকে এতো বেশি ধৈর্য দিন,
    যেন পৃথিবীর কোনো মানুষের কোনো কথায়,
    অন্তর ব্যথিত না হয়,

    1
    00:00:00,0 --> 00:00:02,0
    >> O Allah, give me so much patience,

    2
    00:00:02,0 --> 00:00:04,0
    >> As if in the words of any person in the world,

    3
    00:00:04,0 --> 00:00:07,0
    >> Do not be heartbroken.

    Bengali:
    স্যার, আপনি জেনারেল লাইনে পড়ে কিভাবে ইসলাম সম্পর্কে এত জ্ঞান অর্জন করলেন।
    আমরা যারা নরমাল লাইনে স্টাডি করি, আমরা কিভানে ইসলাম সম্পর্কে জানবো।
    */