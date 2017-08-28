package ian.a.one;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ian.a.music.MusicAdapter;
import ian.a.R;

/**
 * Created by Ian on 8/20/2017.
 */

public class OneAdapter extends MusicAdapter<OneAdapter.ViewHolder>{

    public OneAdapter(Activity activity, ArrayList<File> data) {
        super(activity, data);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, ArrayList<File> data, int position) {
        final File file = data.get(position);
        holder.mDescriptionView.setText(file.getName());

        holder.mDescriptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToQueue(file);
                play(file);
            }
        });
        holder.mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToQueue(file);
            }
        });

        setMusicCover(holder.mIconView,file,position);
    }

    @Override
    protected void updateContext(Context context, boolean fromNull) {
        //// TODO: 8/26/2017
    }

    @Override
    protected int getItemViewParent() {
        return R.layout.one_recyclerview_item;
    }

    @Override
    protected ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected int getArtCoverManagerCode() {
        return 1001;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mItemView;
        ImageView mIconView;
        TextView mDescriptionView;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mIconView = (ImageView) itemView.findViewById(R.id.one_icon);
            mDescriptionView = (TextView) itemView.findViewById(R.id.one_description);
        }
    }
}
