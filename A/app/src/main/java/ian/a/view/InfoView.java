package ian.a.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Ian on 8/23/2017.
 */

public class InfoView extends AppCompatTextView {

    public interface Info {
        String info();
    }

    Info info;

    public InfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInfo(Info info) {
        if (info == null) {
            return;
        }
        this.info = info;
        setText(info.info());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (info != null) {
            setText(info.info());
            postInvalidateDelayed(1000);
        }
    }
}
