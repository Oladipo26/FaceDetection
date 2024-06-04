package com.oladipo.facedetection

import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat

class ViewOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var cameraSelector: Int = CameraSelector.LENS_FACING_FRONT

    //vertical bias is in percentage from 0 - 100. works like vertical bias
    // in chained constrained view
    private var verticalBiasPercent = 20f
        get() = field / 100

    //how oval the cut out should be (in percentage from 0 - 100.). the least
    // is the basic circle and the max is when the height touches the top of the screen
    private var ovalStretchPercent = 30f
        get() = field / 100

    //how much the width of the circle/oval fills the width of the screen
    // (in percentage from 0 - 100.)
    private var widthCoveragePercent = 90f
        get() = field / 100

    private var path = Path().apply {
        addRect(RectF(0f.dpToPixels(context), 0f.dpToPixels(context), 4f.dpToPixels(context),  DASHED_HEIGHT.dpToPixels(context)), Path.Direction.CW)
    }

    private val outerBoxPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
        style = Paint.Style.STROKE
        strokeWidth = 5f.dpToPixels(context)
        pathEffect = PathDashPathEffect(path, 17.0f.dpToPixels(context), 0.0f, PathDashPathEffect.Style.MORPH)
    }

    private val scrimPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black_translucent)
    }

    private val eraserPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 5f.dpToPixels(context)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private var valueAnimator: ValueAnimator? = ofFloat(3600f, 0.0f).apply {
        duration = 70000L
        this.repeatMode = RESTART
        repeatCount = INFINITE
        addUpdateListener {
            val value = it.animatedValue as Float
            outerBoxPaint.pathEffect =
                PathDashPathEffect(path, 20.0f.dpToPixels(context), value, PathDashPathEffect.Style.MORPH)
            invalidate()
        }
    }

    fun startAnimation() {
        valueAnimator?.start()
    }

    fun stopAnimation() {
        valueAnimator?.cancel()
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val externalCircleRadius = (width / 2).toFloat() * widthCoveragePercent
        val internalRadius = externalCircleRadius - DASHED_HEIGHT.dpToPixels(context)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        val verticalOvalStretch = (height - ((externalCircleRadius) * 2)) * ovalStretchPercent
        val circleHeight = (externalCircleRadius * 2) + verticalOvalStretch /*HEIGHT_EXTENSION_FOR_OVAL_SHAPE*/

        //adjustment for the y position of the circle
        val circleYPosition = (circleHeight / 2) + (verticalBiasPercent * (height - circleHeight))

        fun getOvalRectF(radius: Float) = RectF().apply {
            left = (width - (radius * 2)) / 2
            top = circleYPosition - radius - (verticalOvalStretch / 2)
            right = left + (radius * 2)
            bottom = circleYPosition + radius + (verticalOvalStretch / 2)
        }

        canvas.drawOval(getOvalRectF(internalRadius), eraserPaint)
        canvas.drawOval(getOvalRectF(externalCircleRadius), outerBoxPaint)

    }

    fun changeCircleColor(@ColorRes color: Int) {
        outerBoxPaint.color = ContextCompat.getColor(context, color)
        invalidate()
    }

    fun toggleSelector() {
        cameraSelector =
            if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
    }

    fun setInitValues(verticalBiasPercent: Float, ovalStretchPercent: Float, widthCoveragePercent: Float){
        this.verticalBiasPercent = verticalBiasPercent
        this.ovalStretchPercent = ovalStretchPercent
        this.widthCoveragePercent = widthCoveragePercent
    }

    companion object {
        private const val DASHED_HEIGHT = 30f
    }

}

fun Float?.dpToPixels(context: Context?): Float {
    return (this?.times(context?.resources?.displayMetrics?.density ?: 0.toFloat()))?: 0f
}