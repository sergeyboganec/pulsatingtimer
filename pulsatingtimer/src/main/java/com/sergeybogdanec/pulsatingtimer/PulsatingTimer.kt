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
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import kotlin.math.log
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

    private var _max = DEFAULT_MAX
    var max
        get() = _max
        set(value) {
            _max = value
            invalidate()
        }

    private var _textColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint.color = value
        }
    var textColor
        get() = _textColor
        set(value) {
            _textColor = value
            invalidate()
        }

    private var _backgroundTint = DEFAULT_BACKGROUND_TINT
        set(value) {
            field = value
            circlePaint.color = value
        }
    var backgroundTint
        get() = _backgroundTint
        set(value) {
            _backgroundTint = value
            invalidate()
        }

    private var _progress = DEFAULT_PROGRESS
    var progress
        get() = _progress
        set(value) {
            _progress = value
            invalidate()
        }

    private var _textSize = 0f
        set(value) {
            field = value
            textPaint.textSize = value
        }
    var textSize
        get() = _textSize
        set(value) {
            _textSize = value
            invalidate()
        }

    private var _typeface = textPaint.typeface
        set(value) {
            field = value
            textPaint.typeface = value
        }
    var typeface
        get() = _typeface
        set(value) {
            _typeface = value
            invalidate()
        }

    private var _circleRadius = -1f
    var circleRadius
        get() = _circleRadius
        set(value) {
            _circleRadius = value
            invalidate()
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PulsatingTimer).use { typedArray ->
            _max = typedArray.getInt(R.styleable.PulsatingTimer_android_max, DEFAULT_MAX)
            _textColor = typedArray.getColor(R.styleable.PulsatingTimer_android_textColor, DEFAULT_TEXT_COLOR)
            _backgroundTint = typedArray.getColor(R.styleable.PulsatingTimer_android_backgroundTint, DEFAULT_BACKGROUND_TINT)
            _progress = typedArray.getInt(R.styleable.PulsatingTimer_android_progress, DEFAULT_PROGRESS)
            _textSize = typedArray.getDimension(R.styleable.PulsatingTimer_android_textSize, 0f)
            _circleRadius = typedArray.getDimension(R.styleable.PulsatingTimer_circleRadius, -1f)
            try {
                val resId = typedArray.getResourceId(R.styleable.PulsatingTimer_android_fontFamily, -1)
                if (resId != -1) {
                    ResourcesCompat.getFont(context, resId)?.let {
                        _typeface = it
                    }
                }
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private val timerHandler: Handler = Handler(Looper.getMainLooper())

    private var drawCount = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        Log.d(TAG, "onDraw: ${++drawCount}")

        val centerX = round(width / 2f)
        val centerY = round(height / 2f)
        val radius = circleRadius.takeIf { it >= 0 } ?: min(centerX, centerY)

        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        val text = progress.toString()
        val ty = centerY - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(text, centerX, ty, textPaint)

        if (hasWindowFocus()) invalidate()
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
        if (_progress < _max) {
            Log.d(TAG, "handlerRunnable: $progress")
            progress += 1
            pulsatingListener?.onUpdate(_progress)
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
