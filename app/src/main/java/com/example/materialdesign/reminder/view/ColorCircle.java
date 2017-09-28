package com.example.materialdesign.reminder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.Utils;



public class ColorCircle extends View {

    private float radius,conerRadius;
    private Paint paint;
    private Paint conerPaint;
    private int color;
    private int center;
    Context context;
    boolean isConer = false;
    private static final int STROKE_WIDTH = Utils.dpToPx(3);

    public ColorCircle(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ColorCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setColor(int color) {
        paint.setColor(color);
        postInvalidate();
    }

    public void setConer(boolean coner) {
        isConer = coner;
        postInvalidate();
    }

    public void setColorAndConer(int color,boolean isConer){
        paint.setColor(color);
        this.isConer = isConer;
        postInvalidate();
    }

    public void init(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        conerPaint = new Paint(paint);

        TypedArray array = context.obtainStyledAttributes(R.styleable.ColorCircle);
        color = array.getColor(R.styleable.ColorCircle_color, getResources().getColor(R.color.color_circle_gray));

        paint.setColor(color);
        conerPaint.setStyle(Paint.Style.STROKE);
        conerPaint.setStrokeWidth(STROKE_WIDTH);
        conerPaint.setColor(Color.WHITE);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        if(isConer){
            radius = center - STROKE_WIDTH;
            canvas.drawCircle(center,center,radius,paint);
            canvas.drawCircle(center,center,radius,conerPaint);
        }else{
            radius = center;
            canvas.drawCircle(center,center,radius,paint);
        }

//        radius = center ;
//        canvas.drawCircle(center,center,radius,paint);
//        if(isConer){
//            canvas.drawCircle(center,center,radius,conerPaint);
//        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        center = getMeasuredHeight()/2;
        setMeasuredDimension(getMeasuredWidth(),getMeasuredHeight());
    }


}
