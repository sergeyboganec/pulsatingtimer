package com.sergeybogdanec.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sergeybogdanec.pulsatingtimer.PulsatingTimer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.start_button).let { button ->
            findViewById<PulsatingTimer>(R.id.timer).let { timer ->
                button.setOnClickListener {
                    timer.start()
                }
            }
        }
    }
}
