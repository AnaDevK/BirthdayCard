package com.example.happybirthday

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class CardActivity : AppCompatActivity() {
    private lateinit var txtHb: TextView
    private lateinit var imgStop: ImageButton
    var mMediaPlayer: MediaPlayer? = null
    var name = ""
    var isOn = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        txtHb = findViewById(R.id.txtHb)
        imgStop = findViewById(R.id.imgStop)
        val bundle = intent.extras
        if (bundle != null) {
            name = bundle.get(constants.name) as String
        }
        if (name != "") {
            txtHb.text = getString(R.string.happy_birthday_text) + ", " + name + "!"
        } else {
            txtHb.text = getString(R.string.happy_birthday_text) + "!"
        }
        playSound()
        imgStop.setOnClickListener {
            if (isOn) {
                pauseSound()
            } else {
                playSound()
            }
        }
    }

    fun playSound() {
        isOn = true
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.happybirthday)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun pauseSound() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) mMediaPlayer!!.pause()
        isOn = false
    }
}