package com.sergeybogdanec.pulsatingtimer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import kotlin.math.min
import kotlin.math.round

class PulsatingTimer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val circlePaint = Paint()

    private val pulsationPaint = Paint()

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var _target = DEFAULT_TARGET
    var target: Int
        get() = _target
        set(value) {
            _target = value
            invalidate()
        }

    private var _textColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint.color = value
        }
    var textColor: Int
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
    var backgroundTint: Int
        get() = _backgroundTint
        set(value) {
            _backgroundTint = value
            invalidate()
        }

    private var _progress = DEFAULT_PROGRESS
    var progress: Int
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
    var textSize: Float
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
    var typeface: Typeface
        get() = _typeface
        set(value) {
            _typeface = value
            invalidate()
        }

    private var _circleRadius = -1f
    var circleRadius: Float
        get() = _circleRadius
        set(value) {
            _circleRadius = value
            invalidate()
        }

    private var _pulsationAlpha = DEFAULT_PULSATION_ALPHA
    var pulsationAlpha: Float
        get() = _pulsationAlpha
        set(value) {
            _pulsationAlpha = value
            invalidate()
        }

    private var _pulsationInterval = DEFAULT_PULSATION_INTERVAL
    var pulsationInterval: Long
        get() = _pulsationInterval
        set(value) {
            _pulsationInterval = value
            invalidate()
        }

    private var _pulsationDuration = DEFAULT_PULSATION_DURATION
    var pulsationDuration: Long
        get() = _pulsationDuration
        set(value) {
            _pulsationInterval = value
            invalidate()
        }

    private var _pulsationColor = DEFAULT_PULSATION_COLOR
        set(value) {
            field = value
            pulsationPaint.color = value
        }
    var pulsationColor: Int
        get() = _pulsationColor
        set(value) {
            _pulsationColor = value
            invalidate()
        }

    var interpolator: Interpolator = LinearInterpolator()

    init {
        isSaveEnabled = true
        context.obtainStyledAttributes(attrs, R.styleable.PulsatingTimer).use { typedArray ->
            _target = typedArray.getInt(R.styleable.PulsatingTimer_target, DEFAULT_TARGET)
            _textColor = typedArray.getColor(R.styleable.PulsatingTimer_android_textColor, DEFAULT_TEXT_COLOR)
            _backgroundTint = typedArray.getColor(R.styleable.PulsatingTimer_android_backgroundTint, DEFAULT_BACKGROUND_TINT)
            _progress = typedArray.getInt(R.styleable.PulsatingTimer_android_progress, DEFAULT_PROGRESS)
            _textSize = typedArray.getDimension(R.styleable.PulsatingTimer_android_textSize, 0f)
            _circleRadius = typedArray.getDimension(R.styleable.PulsatingTimer_circleRadius, -1f)
            _pulsationAlpha = typedArray.getDimension(R.styleable.PulsatingTimer_pulsationAlpha, DEFAULT_PULSATION_ALPHA)
            _pulsationInterval = typedArray.getInt(R.styleable.PulsatingTimer_pulsationIntervalMillis, DEFAULT_PULSATION_INTERVAL.toInt()).toLong()
            _pulsationDuration = typedArray.getInt(R.styleable.PulsatingTimer_pulsationDuration, DEFAULT_PULSATION_DURATION.toInt()).toLong()
            _pulsationColor = typedArray.getColor(R.styleable.PulsatingTimer_pulsationColor, DEFAULT_PULSATION_COLOR)
            val fontResId = typedArray.getResourceId(R.styleable.PulsatingTimer_android_fontFamily, -1)
            if (fontResId != -1) {
                ResourcesCompat.getFont(context, fontResId)?.let {
                    _typeface = it
                }
            }
            val interpolatorResId = typedArray.getResourceId(R.styleable.PulsatingTimer_pulsationInterpolator, -1)
            if (interpolatorResId != -1) {
                interpolator = AnimationUtils.loadInterpolator(context, interpolatorResId)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pulsationTargetRadius = min(w, h) / 2
    }

    private var pulsationTargetRadius: Int = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        val centerX = round(width / 2f)
        val centerY = round(height / 2f)
        val radius = circleRadius.takeIf { it >= 0 } ?: min(centerX, centerY)

        val currentTime = System.currentTimeMillis()
        val radiusDiff = pulsationTargetRadius - radius

        val iterator = pulsations.iterator()
        while(iterator.hasNext()) {
            val time = iterator.next()
            val diff = (currentTime - time).toFloat()
            val fraction = interpolator.getInterpolation(diff / _pulsationDuration)
            if (fraction < MAX_FRACTION && diff < _pulsationDuration) {
                val pulsationRadius = radiusDiff * fraction + radius
                pulsationPaint.alpha = ((pulsationAlpha * (1f - fraction)) * MAX_ALPHA).toInt()
                canvas.drawCircle(centerX, centerY, pulsationRadius, pulsationPaint)
            } else {
                iterator.remove()
            }
        }

        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        val text = progress.toString()
        val ty = centerY - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(text, centerX, ty, textPaint)

        if (pulsations.isNotEmpty()) invalidate()
    }

    private var pulsatingListener: PulsatingListener? = null

    fun setListener(listener: PulsatingListener) {
        pulsatingListener = listener
    }

    private val animationHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    private val pulsations: MutableList<Long> by lazy { mutableListOf() }

    private var isStarted: Boolean = false

    fun start() {
        animationHandler.removeCallbacksAndMessages(null)
        animationHandler.postDelayed(::timerRunnable, ONE_SECOND)
        animationHandler.postDelayed(::pulsationRunnable, _pulsationInterval)
        pulsatingListener?.onStart()
        isStarted = true
    }

    fun pause() {
        isStarted = false
        animationHandler.removeCallbacksAndMessages(null)
        pulsatingListener?.onPause()
    }

    private fun timerRunnable() {
        if (_progress != _target) {
            if (_progress < _target) {
                progress++
            } else {
                progress--
            }
            pulsatingListener?.onUpdate(_progress)
            animationHandler.postDelayed(::timerRunnable, ONE_SECOND)
        } else {
            animationHandler.removeCallbacksAndMessages(null)
            isStarted = false
            pulsatingListener?.onEnd()
        }
    }

    private fun pulsationRunnable() {
        pulsations.add(System.currentTimeMillis())
        invalidate()
        animationHandler.postDelayed(::pulsationRunnable, _pulsationInterval)
    }

    override fun onDetachedFromWindow() {
        animationHandler.removeCallbacksAndMessages(null)
        pulsatingListener = null
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? = SavedState(super.onSaveInstanceState()).also {
        it.isStarted = isStarted
        it.progress = _progress
        it.pulsations.addAll(pulsations)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        _progress = savedState.progress
        pulsations.clear()
        pulsations.addAll(savedState.pulsations)
        requestLayout()
        invalidate()
        if (savedState.isStarted) start()
    }

    private class SavedState: BaseSavedState {
        var isStarted: Boolean = false
        var progress: Int = DEFAULT_PROGRESS
        val pulsations: MutableList<Long> = mutableListOf()

        constructor(superState: Parcelable?): super(superState) { }

        constructor(parcel: Parcel): super(parcel) {
            isStarted = parcel.readByte().toBoolean()
            progress = parcel.readInt()
            pulsations.addAll(parcel.createLongArray()?.toList().orEmpty())
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte(isStarted.toByte())
            out.writeInt(progress)
            out.writeLongArray(pulsations.toLongArray())
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    companion object {

        private const val DEFAULT_TARGET = 1000
        private const val DEFAULT_PROGRESS = 0
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND_TINT = Color.WHITE
        private const val DEFAULT_PULSATION_COLOR = Color.WHITE
        private const val DEFAULT_PULSATION_ALPHA = .5f
        private const val DEFAULT_PULSATION_INTERVAL = 1000L
        private const val DEFAULT_PULSATION_DURATION = 1200L

        private const val ONE_SECOND = 1000L
        private const val MAX_FRACTION = 1f
        private const val MAX_ALPHA = 255

        private const val TAG = "PulsatingTimer"

    }
}

