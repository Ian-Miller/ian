package ian.a.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Ian on 8/22/2017.
 */

public class ProgressBar extends View {

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private Paint mPaint;
    private int paintColor = 0x000000FF;
    private static final long TIME = 2000;
    private static final long GAP = 100;
    private long startTime = 0;
    private long time = 0;

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(0xFF2962FF);
        setVisibility(GONE);
    }

    private int progress = 0;


    public void setProgress(int p) {

        if (p > 0 && getVisibility() == GONE) {
            setVisibility(VISIBLE);
        }
        if (p > progress && p < 101) {
            progress = p;
            invalidate();
        }
    }

    public void clearProgress() {
        progress = 0;
        startTime = 0;
        time = 0;
        setVisibility(GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateTime();
        updateColor();
        mPaint.setColor(paintColor);
        postInvalidateDelayed(GAP);
        int length = progress * getWidth() / 100;
        canvas.drawRect(0, 0, length, getHeight(), mPaint);
    }
    private void updateTime(){
        long now = System.currentTimeMillis();
        if(startTime == 0){
            startTime = now;
        }
        time = now - startTime;
        if(time > TIME){
            time %= TIME;
        }
    }

    private void updateColor(){
        double w = 2 * Math.PI / TIME;
        double A = 0xFF / 3;
        double B = 0xFF - A;
        int alpha = (int) getFValue(A, w, B);

        paintColor = (paintColor & 0x00FFFFFF) | (alpha << 24);
//        Log.v("now alpha is", "" + alpha);
    }
    private double getFValue(double A, double w, double B){
        double ret = A * Math.sin(w * time) + B;
        if(ret > 0xff){
            ret = 0xFF;
        }
        if(ret < 0){
            ret = 0;
        }
        return ret;
    }
    private static int getRandomNumForColor(){
        return (int)(Math.random() * 0xFF);
    }
    private @ColorInt int getRandomColor(){
        int r = getRandomNumForColor();
        int g = getRandomNumForColor();
        int b = getRandomNumForColor();
        return Color.rgb(r,g,b);
    }
}
