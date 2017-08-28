package ian.a.zero;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ian.a.R;
import ian.a.music.MusicAdapter;

/**
 * Created by Ian on 8/22/2017.
 */

public class ZeroAdapter extends MusicAdapter<ZeroAdapter.ViewHolder> {
    public ZeroAdapter(Activity activity, ArrayList<File> data) {
        super(activity, data);
    }

    @Override
    protected void updateContext(Context context, boolean fromNull) {

    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, ArrayList<File> data, int position) {
        final File file = data.get(position);
        holder.mDescriptionView.setText(file.getName());
        holder.mDescriptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(file);
            }
        });
//        Log.e("position: " + position,"file: " + file.getName());
        setMusicCover(holder.mIconView,file, position);
    }

    @Override
    protected int getItemViewParent() {
        return R.layout.zero_recyclerview_item;
    }

    @Override
    protected ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected int getArtCoverManagerCode() {
        return 1000;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mItemView;
        ImageView mIconView;
        TextView mDescriptionView;
        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mIconView = (ImageView) itemView.findViewById(R.id.zero_icon);
            mDescriptionView = (TextView) itemView.findViewById(R.id.zero_description);
        }
    }
}
