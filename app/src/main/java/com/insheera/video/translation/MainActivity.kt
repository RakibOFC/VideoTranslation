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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var subtitleTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    data class Subtitle(
        val id: Int,
        val startTime: Long,
        val endTime: Long,
        val text: String
    )

    private val subtitles = arrayListOf(
        Subtitle(1, 0, 3200, "O Allah, give me so much patience,"),
        Subtitle(2, 3201, 5497, "As if in the words of any person in the world,"),
        Subtitle(3, 5498, 7094, "Do not be heartbroken.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        subtitleTextView = findViewById(R.id.subtitleTextView)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.sample_video}")
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
                Log.e("TAG", "If: ${player.currentPosition}")

                subtitleTextView.text = currentSubtitle.text
                View.VISIBLE
            } else {
                View.GONE
            }
            lastSubtitleId = currentSubtitle?.id
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
        handler.removeCallbacksAndMessages(null)
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