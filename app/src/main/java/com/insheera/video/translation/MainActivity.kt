package com.insheera.video.translation

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize PlayerView
        playerView = findViewById(R.id.playerView)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Video URI from raw resource
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.sample_video}")
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)

        // Prepare the player
        player.prepare()
        // Start playback automatically
        player.playWhenReady = true
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