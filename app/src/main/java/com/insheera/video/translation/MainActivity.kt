package com.insheera.video.translation

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var subtitleTextView: TextView

    data class Subtitle(
        val startTime: Long,
        val endTime: Long,
        val text: String
    )

    private val subtitles = arrayListOf(
        Subtitle(0, 3000, "O Allah, give me so much patience,"),
        Subtitle(3000, 5000, "As if in the words of any person in the world,"),
        Subtitle(5000, 7000, "Do not be heartbroken.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize PlayerView
        playerView = findViewById(R.id.playerView)
        subtitleTextView = findViewById(R.id.subtitleTextView)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Video URI from raw resource
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.sample_video}")
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)

        // Prepare the player
        player.prepare()
        player.playWhenReady = true

        startSubtitleUpdater()
    }

    private fun startSubtitleUpdater() {
        handler.post(object : Runnable {
            override fun run() {
                val currentPosition = player.currentPosition
                displaySubtitle(currentPosition)
                handler.postDelayed(this, 100)

                Log.e("TAG", "CurrentPosition: $currentPosition")
            }
        })
    }

    private fun displaySubtitle(currentPosition: Long) {
        val currentSubtitle = subtitles.find {
            currentPosition >= it.startTime && currentPosition < it.endTime
        }
        subtitleTextView.visibility = if (currentSubtitle != null) {
            subtitleTextView.text = currentSubtitle.text
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
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
    */