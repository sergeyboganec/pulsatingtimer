package com.sergeybogdanec.pulsatingtimer

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import kotlin.math.min
import kotlin.math.round

class PulsatingTimer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val circlePaint = Paint().apply {
        color = Color.CYAN
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    var max = DEFAULT_MAX
        set(value) {
            field = value
            invalidate()
        }

    var textColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }

    var backgroundTint = DEFAULT_BACKGROUND_TINT
        set(value) {
            field = value
            circlePaint.color = value
            invalidate()
        }

    var progress = DEFAULT_PROGRESS
        set(value) {
            field = value
            invalidate()
        }

    var textSize = 0f
        set(value) {
            field = value
            textPaint.textSize = value
            invalidate()
        }

    var typeface = textPaint.typeface
        set(value) {
            field = value
            textPaint.typeface = value
            invalidate()
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PulsatingTimer).use { typedArray ->
            max = typedArray.getInt(R.styleable.PulsatingTimer_android_max, DEFAULT_MAX)
            textColor = typedArray.getColor(R.styleable.PulsatingTimer_android_textColor, DEFAULT_TEXT_COLOR)
            backgroundTint = typedArray.getColor(R.styleable.PulsatingTimer_android_backgroundTint, DEFAULT_BACKGROUND_TINT)
            progress = typedArray.getInt(R.styleable.PulsatingTimer_android_progress, DEFAULT_PROGRESS)
            textSize = typedArray.getDimension(R.styleable.PulsatingTimer_android_textSize, 0f)
            try {
                val resId = typedArray.getResourceId(R.styleable.PulsatingTimer_android_fontFamily, -1)
                if (resId != -1) {
                    ResourcesCompat.getFont(context, resId)?.let {
                        typeface = it
                    }
                }
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private val timerHandler: Handler = Handler(Looper.getMainLooper())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        val centerX = round(width / 2f)
        val centerY = round(height / 2f)
        val radius = min(centerX, centerY)

        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        val text = progress.toString()
        val ty = centerY - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(text, centerX, ty, textPaint)
    }

    fun start() {
        timerHandler.removeCallbacksAndMessages(null)
        timerHandler.postDelayed(::handlerRunnable, ONE_SECOND)
        pulsatingListener?.onStart()
    }

    fun pause() {
        timerHandler.removeCallbacksAndMessages(null)
        pulsatingListener?.onPause()
    }

    private var pulsatingListener: PulsatingListener? = null

    fun setListener(listener: PulsatingListener) {
        pulsatingListener = listener
    }

    private fun handlerRunnable() {
        if (progress < max) {
            progress += 1
            invalidate()
            pulsatingListener?.onUpdate(progress)
            timerHandler.postDelayed(::handlerRunnable, ONE_SECOND)
        } else {
            pulsatingListener?.onEnd()
        }
    }

    companion object {

        private const val DEFAULT_MAX = 1000
        private const val DEFAULT_PROGRESS = 0
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND_TINT = Color.WHITE

        private const val ONE_SECOND = 1000L
        private const val TAG = "PulsatingTimer"

    }
}
