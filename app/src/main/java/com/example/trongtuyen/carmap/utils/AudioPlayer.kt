package com.example.trongtuyen.carmap.utils

import android.content.Context
import android.media.MediaPlayer


class AudioPlayer {

    private var mMediaPlayer: MediaPlayer? = null

    fun stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun play(context: Context, resourceId: Int) {
        stop()

        mMediaPlayer = MediaPlayer.create(context, resourceId)
        mMediaPlayer!!.setOnCompletionListener {
            stop()
        }

        mMediaPlayer!!.start()
    }
}