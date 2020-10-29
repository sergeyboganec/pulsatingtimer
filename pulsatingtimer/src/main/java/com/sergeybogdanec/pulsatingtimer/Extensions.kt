package com.sergeybogdanec.pulsatingtimer

inline fun PulsatingTimer.setListener(
    crossinline onStart: () -> Unit = {},
    crossinline onPause: () -> Unit = {},
    crossinline onUpdate: (Int) -> Unit = {},
    crossinline onEnd: () -> Unit = {}
) = setListener(object : PulsatingListener {
    override fun onStart() = onStart()
    override fun onPause() = onPause()
    override fun onUpdate(progress: Int) = onUpdate(progress)
    override fun onEnd() = onEnd()
})
