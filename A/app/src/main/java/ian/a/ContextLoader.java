package ian.a;

import android.content.Context;
import android.content.Loader;

import java.util.Objects;

/**
 * Created by Ian on 8/26/2017.
 */

public class ContextLoader extends Loader<ContextContent> {

    ContextContent mContextContent;

    public ContextLoader(Context context, ContextContent contextContent) {
        super(context);
        this.mContextContent = Objects.requireNonNull(contextContent,"contextContent can't be null");
    }

    @Override
    protected void onStartLoading() {
        deliverResult(mContextContent);
    }

    @Override
    protected void onForceLoad() {
        startLoading();
    }

    @Override
    protected void onReset() {
        mContextContent.release();
    }
}
