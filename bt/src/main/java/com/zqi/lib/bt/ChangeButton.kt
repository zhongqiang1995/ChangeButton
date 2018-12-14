package com.zqi.lib.bt

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.widget.TextView
import android.graphics.RectF
import android.graphics.PaintFlagsDrawFilter
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

/**
 * Created by zhong
 */
class ChangeButton @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    TextView(context, attrs, defStyle), View.OnClickListener {


    companion object {
        const val TAG = "ChangeButton"
        const val GRADIENT_STYLE_LINEAR = 1 // 线性渐变
        const val GRADIENT_STYLE_RADIAL = 2 // 圆形渐变
        const val GRADIENT_STYLE_SWEEP = 3 // 角度渐变

    }


    private var topLeftRadioSize: Float = 0f //左上圆角大小
    private var topRightRadioSize: Float = 0f //右上圆角大小
    private var bottomLeftRadioSize: Float = 0f //左下圆角大小
    private var bottomRightRadioSize: Float = 0f //右下圆角大小

    private var strokeSize: Float = 0f //描边线大小
    private var strokeColor: Int = Color.TRANSPARENT //描边线颜色
    private var bgColor: Int = 0 //普通状态下的背景颜色
    private var bgPressColor: Int = 0 //点击状态下的背景颜色
    private var bgDisabledColor: Int = 0 //不可点击状态下的背景颜色
    private var isShowShadow = true //是否显示阴影
    private var isTouchOverstepView = false //当前触摸移动时是否已经超出过当前 view 的范围
    private var currentAction = -1 //当前触摸动作
    private val normalShadowRadius = dip2px(3).toFloat() // 普通状态下阴影的半径
    private val pressShadowRadius = normalShadowRadius / 1.6f //点击状态下阴影的半径
    private val shadowSize = dip2px(4) //阴影的大小
    private var bgGradient: GradientEntity? = null //普通状态下的渐变颜色
    private var bgPressGradient: GradientEntity? = null //点击状态下的渐变颜色
    private var bgDisabledGradient: GradientEntity? = null //不可以点击时的渐变颜色
    private var gradientStyle: Int = GRADIENT_STYLE_LINEAR //渐变颜色的种类
    private var rippleRadioSize: Float = 0f //当前点击波纹的大小
    private var rippleAnimator: ValueAnimator? = null
    private var rippleFinalRadioSize: Float = 0f //点击波纹半径最终的大小
    private var isClickRipple = true //是否点击的时候显示波纹
    private var rippleAnimatorDuration: Long = 300 //点击波纹显示的时间

    private var paintDefaultColor: Int = 0
    private var rect: RectF? = null
    private var rectStroke: RectF? = null
    private var radiusArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private val paint: Paint = Paint()
    private val paintFlagFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var clickX: Float = 0f
    private var clickY: Float = 0f
    private var isStartDrawRipple = false

    init {

        val array = context.obtainStyledAttributes(attrs, R.styleable.ChangeButton)
        bgColor = array.getColor(R.styleable.ChangeButton_bgColor, resources.getColor(R.color.colorAccent))
        bgPressColor = array.getColor(R.styleable.ChangeButton_bgPressColor, getDarkerColor(bgColor))
        bgDisabledColor = array.getColor(R.styleable.ChangeButton_bgDisabledColor, getBrighterColor(bgColor))
        strokeSize = array.getDimension(R.styleable.ChangeButton_strokeSize, 0f)
        isClickRipple = array.getBoolean(R.styleable.ChangeButton_isClickRipple, true)
        strokeColor = array.getColor(R.styleable.ChangeButton_strokeColor, Color.TRANSPARENT)
        isShowShadow = array.getBoolean(R.styleable.ChangeButton_isShowShadow, true)
        gradientStyle = array.getInt(R.styleable.ChangeButton_gradientStyle, GRADIENT_STYLE_LINEAR)
        val bgGradientStartColor = array.getColor(R.styleable.ChangeButton_bgGradientStartColor, -1)
        val bgGradientEndColor = array.getColor(R.styleable.ChangeButton_bgGradientEndColor, -1)
        val bgGradientDisabledStartColor = array.getColor(R.styleable.ChangeButton_bgGradientDisabledStartColor, -1)
        val bgGradientDisabledEndColor = array.getColor(R.styleable.ChangeButton_bgGradientDisabledEndColor, -1)
        val bgGradientPressStartColor = array.getColor(R.styleable.ChangeButton_bgGradientPressStartColor, -1)
        val bgGradientPressEndColor = array.getColor(R.styleable.ChangeButton_bgGradientPressEndColor, -1)
        val round = array.getDimension(R.styleable.ChangeButton_round, 0f)

        if (bgGradientStartColor != -1 && bgGradientEndColor != -1) {
            bgGradient = GradientEntity(bgGradientStartColor, bgGradientEndColor)
        }
        if (bgGradientDisabledStartColor != -1 && bgGradientDisabledEndColor != -1) {
            bgDisabledGradient = GradientEntity(bgGradientDisabledStartColor, bgGradientDisabledEndColor)
        }
        if (bgGradientPressStartColor != -1 && bgGradientPressEndColor != -1) {
            bgPressGradient = GradientEntity(bgGradientPressStartColor, bgGradientPressEndColor)
        }

        if (bgGradient != null && (bgDisabledGradient == null || bgPressGradient == null)) {
            if (bgDisabledGradient == null) {
                bgDisabledGradient = GradientEntity(
                    getBrighterColor(bgGradient?.startColor!!),
                    getBrighterColor(bgGradient?.endColor!!)
                )
            }
            if (bgPressGradient == null) {
                bgPressGradient = GradientEntity(
                    getDarkerColor(bgGradient?.startColor!!),
                    getDarkerColor(bgGradient?.endColor!!)
                )
            }
        }


        if (round != 0f) {
            topLeftRadioSize = round
            topRightRadioSize = round
            bottomLeftRadioSize = round
            bottomRightRadioSize = round
        } else {
            topLeftRadioSize = array.getDimension(R.styleable.ChangeButton_topLeftRound, 0f)
            topRightRadioSize = array.getDimension(R.styleable.ChangeButton_topRightRound, 0f)
            bottomLeftRadioSize = array.getDimension(R.styleable.ChangeButton_bottomLeftRound, 0f)
            bottomRightRadioSize = array.getDimension(R.styleable.ChangeButton_bottomRightRound, 0f)
        }
        array.recycle()


        setOnClickListener(this)
        setBackgroundColor(Color.TRANSPARENT)
        resetRadiusConfig()
        gravity = Gravity.CENTER
        paintDefaultColor = paint.color
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.isAntiAlias = true
        paint.isDither = true

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        if (isShowShadow) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

    }

    override fun onClick(v: View?) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h: Int
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        //设置在 wrap_content 下的高度为45dp
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            h = MeasureSpec.makeMeasureSpec(dip2px(45), MeasureSpec.EXACTLY)
        } else {
            h = heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, h)
    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (isEnabled) {
            val action = event?.action!!
            if (action != currentAction) {
                when (action) {

                    MotionEvent.ACTION_CANCEL -> {
                        currentAction = action
                        if (rippleAnimator != null) {
                            isTouchOverstepView = true
                            rippleAnimator?.cancel()
                            rippleAnimator = null
                        }
                        rippleRadioSize = 0f
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                        currentAction = action
                        rippleRadioSize = 0f
                        invalidate()
                    }

                    MotionEvent.ACTION_DOWN -> {
                        isTouchOverstepView = false
                        currentAction = action
                        clickX = event.x
                        clickY = event.y
                        handleDrawRipple()

                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = event.x
                        val y = event.y
                        //判断移动是否超出 view 范围
                        if (x > measuredWidth || y > measuredHeight || x < 0 || y < 0) {
                            if (!isTouchOverstepView) {
                                rippleRadioSize = 0f
                                isTouchOverstepView = true
                                Log.d(TAG, "overstep view")
                                invalidate()
                            }
                        }

                    }

                }
            }

        }
        return super.dispatchTouchEvent(event)

    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawFilter = paintFlagFilter
        drawContent(canvas)
        super.onDraw(canvas)
    }

    private fun dip2px(i: Int): Int {
        return (0.5 + (getDensity() * i.toFloat()).toDouble()).toInt()
    }

    private fun getDensity(): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 获取渐变数据
     */
    private fun getGradientShader(): Shader? {
        var shape: Shader? = null

        if (isEnabled) {
            if (currentAction == -1 || currentAction == MotionEvent.ACTION_CANCEL || currentAction == MotionEvent.ACTION_UP || isTouchOverstepView) {
                if (bgGradient != null) {
                    shape = getGradientShaderStyle(bgGradient!!)
                }

            } else {
                if (bgPressGradient != null) {
                    shape = getGradientShaderStyle(bgPressGradient!!)

                }
            }

        } else if (!isEnabled && bgDisabledGradient != null) {
            if (bgDisabledGradient != null) {
                shape = getGradientShaderStyle(bgDisabledGradient!!)
            }

        }

        return shape
    }

    /**
     * 获取普通和不可点击下的渐变颜色
     */
    private fun getGradientShaderStyle(gradientEntity: GradientEntity): Shader? {
        var shader: Shader? = null
        when (gradientStyle) {
            GRADIENT_STYLE_LINEAR -> {
                shader = LinearGradient(
                    rect?.top!!,
                    rect?.left!!,
                    rect?.right!!,
                    rect?.top!!,
                    gradientEntity.startColor,
                    gradientEntity.endColor,
                    Shader.TileMode.MIRROR
                )
            }
            GRADIENT_STYLE_RADIAL -> {
                shader = RadialGradient(
                    (rect?.right!! - rect?.left!!) / 2,
                    (rect?.bottom!! - rect?.top!!) / 2,
                    max(rect?.right!! - rect?.left!!, rect?.bottom!! - rect?.top!!),
                    gradientEntity.startColor,
                    gradientEntity.endColor,
                    Shader.TileMode.MIRROR
                )


            }
            GRADIENT_STYLE_SWEEP -> {
                shader = SweepGradient(
                    (rect?.right!! - rect?.left!!) / 2,
                    (rect?.bottom!! - rect?.top!!) / 2,
                    gradientEntity.startColor,
                    gradientEntity.endColor
                )

            }
        }
        return shader
    }


    private fun drawContent(canvas: Canvas?) {

        //画矩形
        resetPaintStyle()
        paint.style = Paint.Style.FILL
        val shape = getGradientShader()
        if (shape != null) {
            paint.shader = shape
            paint.color = paintDefaultColor
        } else {
            paint.shader = null
            paint.color = bgColor

        }

        //设置阴影属性
        if (isShowShadow) {
            if (currentAction == MotionEvent.ACTION_UP) {
                paint.setShadowLayer(
                    pressShadowRadius,
                    0f,
                    0f,
                    Color.parseColor("#6e6e6e")
                )
            } else {
                paint.setShadowLayer(
                    normalShadowRadius,
                    0f,
                    0f, Color.parseColor("#6e6e6e")
                )
            }
        }
        drawRect(canvas, rect)


        //画波纹

        val path = Path()
        path.addRoundRect(rectStroke, radiusArray, Path.Direction.CW)
        canvas!!.clipPath(path)
        if (rippleRadioSize > 0f && currentAction == MotionEvent.ACTION_DOWN) {
            drawRipple(canvas)
        }


        //画线条
        resetPaintStyle()
        paint.strokeWidth = strokeSize
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        paint.clearShadowLayer()
        drawRect(canvas, rectStroke)


    }

    private fun resetPaintStyle() {
        paint.alpha = 255
        paint.shader = null
    }


    private fun handleDrawRipple() {
        if (rippleAnimator == null) {
            rippleAnimator?.cancel()
        }
        val w1 = rect?.right!! - clickX
        val w2 = clickX - rect?.left!!

        val h1 = clickY - rect?.top!!
        val h2 = rect?.bottom!! - clickY

        val h = max(h1, h2)
        val w = max(w1, w2)

        var max = max(w, h)
        max *= 1.6f
        rippleFinalRadioSize = max
        rippleAnimator = ValueAnimator.ofFloat(max)
        rippleAnimator?.duration = if (isClickRipple) rippleAnimatorDuration else 0
        rippleAnimator?.addUpdateListener { value ->
            rippleRadioSize = value.animatedValue as Float
            invalidate()
            if (rippleRadioSize == max) {
                rippleAnimator = null
            }
        }
        rippleAnimator?.start()
    }


    /**
     * 画点击波纹
     */
    private fun drawRipple(canvas: Canvas?) {
        if (isStartDrawRipple || !isEnabled) {
            rippleRadioSize = 0f
            return
        }
        paint.clearShadowLayer()
        paint.style = Paint.Style.FILL
        val f = rippleRadioSize / rippleFinalRadioSize
        paint.alpha = (f * 255).toInt()


        if (bgPressGradient == null) {
            paint.color = bgPressColor
            paint.shader = null

        } else {
            var shader: Shader? = null
            if (isClickRipple) {
                shader = RadialGradient(
                    clickX,
                    clickY,
                    max(rect?.right!! - rect?.left!!, rect?.bottom!! - rect?.top!!),
                    bgPressGradient?.startColor!!,
                    bgPressGradient?.endColor!!,
                    Shader.TileMode.MIRROR
                )
            } else {
                shader = LinearGradient(
                    rect?.top!!,
                    rect?.left!!,
                    rect?.right!!,
                    rect?.top!!,
                    bgPressGradient?.startColor!!,
                    bgPressGradient?.endColor!!,
                    Shader.TileMode.MIRROR
                )
            }

            paint.shader = shader
            paint.color = paintDefaultColor
        }
        canvas?.drawCircle(
            clickX,
            clickY,
            rippleRadioSize,
            paint
        )

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val shadowWidth = if (isShowShadow) shadowSize else 0
        rect = RectF(
            paddingLeft.toFloat() + shadowWidth,
            paddingTop.toFloat() + shadowWidth,
            measuredWidth.toFloat() - paddingRight - shadowWidth,
            measuredHeight.toFloat() - paddingRight - shadowWidth
        )


        var diff = 0f
//        if (strokeSize != 0f
//            && (bottomLeftRadioSize != 0f
//                    || bottomRightRadioSize != 0f
//                    || topLeftRadioSize != 0f
//                    || topRightRadioSize != 0f)
//        ) {
//            //解决在圆角比较大的时候描边线条会向内偏移
//            diff = strokeSize / 2.45f
//        }
        rectStroke = RectF(
            paddingLeft.toFloat() + diff + shadowWidth,
            paddingTop.toFloat() + diff + shadowWidth,
            measuredWidth.toFloat() - paddingRight - diff - shadowWidth,
            measuredHeight.toFloat() - paddingRight - diff - shadowWidth
        )

    }

    private fun resetRadiusConfig() {
        radiusArray[0] = topLeftRadioSize
        radiusArray[1] = topLeftRadioSize
        radiusArray[2] = topRightRadioSize
        radiusArray[3] = topRightRadioSize
        radiusArray[4] = bottomRightRadioSize
        radiusArray[5] = bottomRightRadioSize
        radiusArray[6] = bottomLeftRadioSize
        radiusArray[7] = bottomLeftRadioSize
    }

    private fun drawRect(canvas: Canvas?, rect: RectF?) {
        val path = Path()
        path.addRoundRect(rect, radiusArray, Path.Direction.CW)
        canvas?.drawPath(path, paint)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
    }

    // 获取更深颜色
    private fun getDarkerColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        // make darker
        hsv[1] = hsv[1] + 0.1f // 饱和度更高
        hsv[2] = hsv[2] - 0.1f // 明度降低
        return Color.HSVToColor(hsv)
    }

    // 获取更浅的颜色
    private fun getBrighterColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hsv[1] = hsv[1] - 0.2f
        hsv[2] = hsv[2] + 0.1f
        return Color.HSVToColor(hsv)
    }


    /**
     * @param color 普通状态下的颜色
     * @param inherit 是否根据普通状态下的颜色来生成press 和 disable 状态的颜色
     */
    fun setBgColor(color: Int, inherit: Boolean = false) {
        this.bgColor = color
        if (inherit) {
            this.bgPressColor = getDarkerColor(this.bgColor)
            this.bgDisabledColor = getBrighterColor(this.bgColor)

        }


        invalidate()
    }

    /**
     * @param color 点击状态下的颜色
     */
    fun setBgPressColor(color: Int) {
        this.bgPressColor = color
        invalidate()
    }

    /**
     * 不可点击下的颜色
     */
    fun setDisableColor(color: Int) {
        this.bgDisabledColor = color

        invalidate()
    }

    /**
     * @param round 四个圆角的大小
     */
    fun setRound(round: Float) {
        setRound(round, round, round, round)
    }

    /**
     * @param topLeftRound  左上角圆角大小
     * @param topRightRound 右上角圆角大小
     * @param bottomLeftRound 左下角圆角大小
     * @param bottomRightRound 右下角院校大小
     */
    fun setRound(topLeftRound: Float, topRightRound: Float, bottomLeftRound: Float, bottomRightRound: Float) {
        this.bottomRightRadioSize = bottomLeftRound
        this.bottomLeftRadioSize = bottomRightRound
        this.topLeftRadioSize = topLeftRound
        this.topRightRadioSize = topRightRound
        resetRadiusConfig()
        invalidate()
    }


    override fun setBackgroundColor(color: Int) {
    }


    override fun setBackground(background: Drawable?) {
        super.setBackground(background)
    }

    override fun setBackgroundResource(resid: Int) {
    }


    override fun setBackgroundTintList(tint: ColorStateList?) {
    }

    override fun setBackgroundTintMode(tintMode: PorterDuff.Mode?) {
    }


    data class GradientEntity(
        val startColor: Int,
        val endColor: Int
    )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (rippleAnimator != null) {
            rippleAnimator?.cancel()
            rippleAnimator = null
        }
    }
}
