package com.insheera.video.translation

import android.media.AudioFormat
import android.media.AudioTrack
import com.google.cloud.texttospeech.v1.AudioConfig
import com.google.cloud.texttospeech.v1.AudioEncoding
import com.google.cloud.texttospeech.v1.SsmlVoiceGender
import com.google.cloud.texttospeech.v1.SynthesisInput
import com.google.cloud.texttospeech.v1.TextToSpeechClient
import com.google.cloud.texttospeech.v1.VoiceSelectionParams

fun convertTextToSpeechAndPlay(text: String) {
    TextToSpeechClient.create().use { textToSpeechClient ->
        // Set the text input to be synthesized
        val input = SynthesisInput.newBuilder()
            .setText(text)
            .build()

        // Select voice parameters such as language and gender
        val voice = VoiceSelectionParams.newBuilder()
            .setLanguageCode("en-US")  // Language code, e.g., "en-US"
            .setSsmlGender(SsmlVoiceGender.NEUTRAL)  // Voice gender
            .build()

        // Specify audio output format
        val audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.LINEAR16)  // Use raw PCM format
            .build()

        // Perform the text-to-speech request
        val response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig)

        // Retrieve the audio content as byte array
        val audioContents = response.audioContent.toByteArray()

        // Play the audio using AudioTrack
        playAudio(audioContents)
    }
}

fun playAudio(audioData: ByteArray) {
    val audioTrack = AudioTrack.Builder()
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(16000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(audioData.size)
        .build()

    // Load the audio data
    audioTrack.write(audioData, 0, audioData.size)

    // Start playing
    audioTrack.play()
}