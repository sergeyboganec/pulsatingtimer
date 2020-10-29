package com.sergeybogdanec.pulsatingtimer

interface PulsatingListener {
    fun onStart()
    fun onPause()
    fun onUpdate(progress: Int)
    fun onEnd()
}
