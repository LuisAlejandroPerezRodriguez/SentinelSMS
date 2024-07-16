package com.pucmm.sentinelsms.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class AutoResizeEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val minHeight = minimumHeight
        val maxHeight = (6 * lineHeight) + paddingTop + paddingBottom

        val desiredHeight = Math.max(minHeight, Math.min(maxHeight, measuredHeight))
        setMeasuredDimension(measuredWidth, desiredHeight)
    }
}
