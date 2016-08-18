package cz.library.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Administrator on 2016/8/16.
 */
public class MaterialProgressView extends View {
    private MaterialProgressDrawable materialDrawable;
    private Animation scaleAnimation;
    private float scale = 1f;

    public MaterialProgressView(Context context) {
        this(context,null, 0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        materialDrawable = new MaterialProgressDrawable(context, this);
        materialDrawable.setBackgroundColor(Color.WHITE);
        materialDrawable.setCallback(this);
    }

    public void setDrawableAlpha(int alpha){
        materialDrawable.setAlpha(alpha);
        invalidate();
    }

    public MaterialProgressDrawable getMaterialDrawable(){
        return materialDrawable;
    }

    @Override
    public void invalidate() {
        if(hasWindowFocus()) super.invalidate();
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (drawable == materialDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = materialDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = materialDrawable.getIntrinsicHeight();
        materialDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int saveCount = canvas.save();
        Rect rect = materialDrawable.getBounds();
        int l = getPaddingLeft() + (getMeasuredWidth() - materialDrawable.getIntrinsicWidth()) / 2;
        canvas.translate(l, getPaddingTop());
        canvas.scale(scale, scale, rect.exactCenterX(), rect.exactCenterY());
        materialDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
