package com.tonymanou.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.tonymanou.vlstools.R

/**
 * Floating action buttons are used for a special type of promoted action. They are distinguished
 * by a circled icon floating above the UI and have special motion behaviors related to morphing,
 * launching, and the transferring anchor point.
 */
class FloatingRoundLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null
    private var mRotation: Float = 0f

    private val mShadowDrawable: BottomCroppedRoundShadowDrawable

    var compatElevation: Float
        get() = mShadowDrawable.shadowSize
        set(elevation) {
            mShadowDrawable.shadowSize = elevation
        }

    init {
        mRotation = rotation

        val density = context.resources.displayMetrics.density

        val a = context.obtainStyledAttributes(attrs, R.styleable.FloatingRoundLayout, defStyleAttr, 0)
        val elevation = 8 * density //a.getDimension(R.styleable.FloatingActionButton_elevation, 0f);
        a.recycle()

        mShadowDrawable = BottomCroppedRoundShadowDrawable(context, elevation).also {
            it.shadowSize = elevation
        }

        super.setBackgroundDrawable(mShadowDrawable)
    }

    override fun setBackgroundDrawable(background: Drawable) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.")
    }

    override fun setBackgroundResource(resid: Int) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.")
    }

    override fun setBackgroundColor(color: Int) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val listener = mPreDrawListener ?: ViewTreeObserver.OnPreDrawListener {
            val rotation = rotation
            if (mRotation != rotation) {
                mRotation = rotation
                updateFromViewRotation(rotation)
            }
            true
        }.also {
            mPreDrawListener = it
        }
        viewTreeObserver.addOnPreDrawListener(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPreDrawListener?.let {
            viewTreeObserver.removeOnPreDrawListener(it)
            mPreDrawListener = null
        }
    }

    private fun updateFromViewRotation(rotation: Float) {
        if (Build.VERSION.SDK_INT == 19) {
            // KitKat seems to have an issue with views which are rotated with angles which are
            // not divisible by 90. Worked around by moving to software rendering in these cases.
            if (rotation % 90 != 0f) {
                if (layerType != View.LAYER_TYPE_SOFTWARE) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                }
            } else {
                if (layerType != View.LAYER_TYPE_NONE) {
                    setLayerType(View.LAYER_TYPE_NONE, null)
                }
            }
        }

        // Offset any View rotation
        mShadowDrawable.rotation = -rotation
    }

    /**
     * A [Drawable] which wraps another drawable and
     * draws a shadow around it.
     */
    @SuppressLint("PrivateResource")
    private class BottomCroppedRoundShadowDrawable(context: Context, shadowSize: Float) : Drawable() {

        private val mCornerShadowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).also {
            it.style = Paint.Style.FILL
        }
        private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.color = Color.WHITE
        }

        private val mContentBounds = RectF()
        private var mCornerRadius: Float = 0f
        private val mCornerShadowPath = Path()

        // multiplied value to account for shadow offset
        private var mShadowSize: Float = 0f
        // actual value set by developer
        var shadowSize: Float = 0f
            set(shadowSize) {
                if (shadowSize < 0) {
                    throw IllegalArgumentException("invalid shadow size")
                }
                val evenShadowSize = toEven(shadowSize).toFloat()
                if (field == evenShadowSize) {
                    return
                }
                field = evenShadowSize
                mShadowSize = Math.round(evenShadowSize * SHADOW_MULTIPLIER).toFloat()
                mDirty = true
                invalidateSelf()
            }

        private var mDirty = true

        private val mShadowStartColor: Int = ContextCompat.getColor(context, R.color.design_fab_shadow_start_color)
        private val mShadowMiddleColor: Int = ContextCompat.getColor(context, R.color.design_fab_shadow_mid_color)
        private val mShadowEndColor: Int = ContextCompat.getColor(context, R.color.design_fab_shadow_end_color)

        var rotation: Float = 0f
            set(rotation) {
                if (field != rotation) {
                    field = rotation
                    invalidateSelf()
                }
            }

        init {
            this.shadowSize = shadowSize
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            mPaint.colorFilter = colorFilter
            invalidateSelf()
        }

        override fun setAlpha(alpha: Int) {
            mPaint.alpha = alpha
            mCornerShadowPaint.alpha = alpha
            invalidateSelf()
        }

        override fun onBoundsChange(bounds: Rect) {
            mDirty = true
        }

        override fun getPadding(padding: Rect): Boolean {
            padding.set(0, 0, 0, 0)
            return true
        }

        override fun getOpacity() = PixelFormat.TRANSLUCENT

        override fun draw(canvas: Canvas) {
            val bounds = bounds
            if (mDirty) {
                buildComponents(bounds)
                mDirty = false
            }

            val clipSaved = canvas.save()
            canvas.clipRect(mContentBounds.left - mShadowSize,
                    mContentBounds.top - mShadowSize,
                    mContentBounds.right + mShadowSize,
                    bounds.bottom.toFloat())
            canvas.rotate(rotation, mContentBounds.centerX(), mContentBounds.centerY())

            val rawShadowSize = shadowSize
            val shadowOffsetTop = rawShadowSize * (1 - SHADOW_TOP_SCALE)
            val shadowOffsetHorizontal = rawShadowSize * (1 - SHADOW_HORIZ_SCALE)
            val shadowOffsetBottom = rawShadowSize * (1 - SHADOW_BOTTOM_SCALE)

            val shadowOffset = mCornerRadius
            val shadowScaleHorizontal = shadowOffset / (shadowOffset + shadowOffsetHorizontal)
            val shadowScaleTop = shadowOffset / (shadowOffset + shadowOffsetTop)
            val shadowScaleBottom = shadowOffset / (shadowOffset + shadowOffsetBottom)

            // LT
            var saved = canvas.save()
            canvas.translate(mContentBounds.left + shadowOffset, mContentBounds.top + shadowOffset)
            canvas.scale(shadowScaleHorizontal, shadowScaleTop)
            canvas.drawPath(mCornerShadowPath, mCornerShadowPaint)
            canvas.restoreToCount(saved)
            // RB
            saved = canvas.save()
            canvas.translate(mContentBounds.right - shadowOffset, mContentBounds.bottom - shadowOffset)
            canvas.scale(shadowScaleHorizontal, shadowScaleBottom)
            canvas.rotate(180f)
            canvas.drawPath(mCornerShadowPath, mCornerShadowPaint)
            canvas.restoreToCount(saved)

            canvas.drawOval(mContentBounds, mPaint)

            canvas.restoreToCount(clipSaved)
        }

        private fun buildComponents(bounds: Rect) {
            val left = bounds.left.toFloat()
            val top = bounds.top.toFloat()
            val right = bounds.right.toFloat()
            val bottom = bounds.bottom.toFloat()
            val width = right - left
            val height = bottom - top

            // Keep our drawable squared and top-aligned
            when {
                width > height -> mContentBounds.set(left, top, right, top + width)
                width < height -> {
                    val newLeft = left - (height - width) / 2f
                    mContentBounds.set(newLeft, top, newLeft + height, bottom)
                }
                else -> mContentBounds.set(left, top, right, bottom)
            }

            mCornerRadius = Math.round(mContentBounds.width() / 2f).toFloat()

            val innerBounds = RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius)
            val outerBounds = RectF(innerBounds)
            outerBounds.inset(-mShadowSize, -mShadowSize)

            mCornerShadowPath.reset()
            mCornerShadowPath.fillType = Path.FillType.EVEN_ODD
            mCornerShadowPath.moveTo(-mCornerRadius, 0f)
            mCornerShadowPath.rLineTo(-mShadowSize, 0f)
            // outer arc
            mCornerShadowPath.arcTo(outerBounds, 180f, 180f, false)
            // inner arc
            mCornerShadowPath.arcTo(innerBounds, 0f, -180f, false)
            mCornerShadowPath.close()

            val shadowRadius = -outerBounds.top
            if (shadowRadius > 0f) {
                val startRatio = mCornerRadius / shadowRadius
                val midRatio = startRatio + (1f - startRatio) / 2f
                mCornerShadowPaint.shader = RadialGradient(0f, 0f, shadowRadius,
                        intArrayOf(0, mShadowStartColor, mShadowMiddleColor, mShadowEndColor),
                        floatArrayOf(0f, startRatio, midRatio, 1f),
                        Shader.TileMode.CLAMP)
            }
        }
    }

    companion object {

        private const val LOG_TAG = "FloatingRoundLayout"

        private const val SHADOW_MULTIPLIER = 1.5f
        private const val SHADOW_TOP_SCALE = 0.25f
        private const val SHADOW_HORIZ_SCALE = 0.5f
        private const val SHADOW_BOTTOM_SCALE = 1f

        @JvmStatic
        private fun toEven(value: Float): Int {
            val i = Math.round(value)
            return if (i % 2 == 1) i - 1 else i
        }
    }
}
