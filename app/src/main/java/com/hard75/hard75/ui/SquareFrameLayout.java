package com.hard75.hard75.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/** Квадратный контейнер: высота = ширина. */
public class SquareFrameLayout extends FrameLayout {
    public SquareFrameLayout(Context context) { super(context); }
    public SquareFrameLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // делаем высоту равной ширине
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
