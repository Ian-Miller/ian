package ian.a;

import android.content.Context;

/**
 * Created by Ian on 8/26/2017.
 */

public abstract class ContextContent {
    private Context mContext;
    public ContextContent(Context context){
        update(context, true);
    }

    public void update(Context context, boolean fromNull){
        if(context != null){
            mContext = context;
            updateContext(context, fromNull);
        }
    };

    protected abstract void updateContext(Context context, boolean fromNull);
    public abstract void release();
}
