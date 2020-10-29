package com.sergeybogdanec.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sergeybogdanec.pulsatingtimer.PulsatingTimer
import com.sergeybogdanec.pulsatingtimer.setListener

class MainActivity : AppCompatActivity() {

    private val vibrator: Vibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<PulsatingTimer>(R.id.timer).let { timer ->
            findViewById<Button>(R.id.start_button).let { button ->
                button.setOnClickListener {
                    timer.start()
                }
            }
            findViewById<Button>(R.id.pause_button).let { button ->
                button.setOnClickListener {
                    timer.pause()
                }
            }
            timer.setListener(onStart = {
                toast("start")
            }, onPause = {
                toast("pause")
            }, onUpdate = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100L, 100))
                } else {
                    vibrator.vibrate(100L)
                }
            }, onEnd = {
                toast("end")
            })
        }
    }

}
