package com.sergeybogdanec.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
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
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        findViewById<PulsatingTimer>(R.id.timer).let { timer ->
            findViewById<Button>(R.id.start_button).let { button ->
                timer.interpolator = DecelerateInterpolator()
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
                timer.startAnimation(pulse)
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
