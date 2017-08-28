package ian.a.invalid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ian.a.R;

public class ScrollView extends ViewGroup {

    ViewGroup mRoot;
    View mSample;
    private static FrameLayout sDefaultRootView;

    static final LayoutParams wrap_content = new ViewGroup.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);

    public ScrollView(Context context) {
        this(context,null);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mSample = new View(context,attrs,defStyleAttr,defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.my_style);
        String type = a.getString(0);
        if(type.equals("LinearLayout")){
            mSample = new LinearLayout(context,attrs,defStyleAttr,defStyleRes);
        }
        a.recycle();

        init(context);
    }

    private void init(Context context){
        sDefaultRootView = new FrameLayout(context);
        if(mSample instanceof ViewGroup){
            mRoot = (ViewGroup) mSample;
        } else{
            mRoot = sDefaultRootView;
        }
        mRoot.setLayoutParams(wrap_content);
        mRoot.setBackgroundColor(0x44444444);
        super.addView(mRoot,0,wrap_content);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mRoot.measure(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mRoot.layout(0, 0, r-l, b-t);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public void setType(Context context, ViewGroup sample){
        int count = mRoot.getChildCount();
        View[] children = new View[count];
        for(int i = 0; i < count; i++){
            children[i] = mRoot.getChildAt(i);
        }
        mRoot = sample;
        mRoot.setLayoutParams(wrap_content);
        for (int i = 0; i < count; i++){
            mRoot.addView(children[i],i);
        }

    }

    @Override
    public void addView(View child) {
        mRoot.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        mRoot.addView(child, index);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        mRoot.addView(child, index, params);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        mRoot.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        mRoot.addView(child, width, height);
    }

    @Override
    public void removeView(View view) {
        mRoot.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        mRoot.removeViewAt(index);
    }

    @Override
    public void removeAllViews() {
        mRoot.removeAllViews();
    }

    @Override
    public void removeViews(int start, int count) {
        mRoot.removeViews(start, count);
    }

    @Override
    public void onViewRemoved(View child) {
        if(Build.VERSION.SDK_INT >= 23) {
            mRoot.onViewRemoved(child);
        }
    }
}
