package ian.a.music;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import ian.a.music.ArtCoverManager.ArtCoverHandler;
import ian.a.R;

/**
 * Created by Ian on 8/26/2017.
 */

public abstract class MusicAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements LoaderManager.LoaderCallbacks<ArtCoverManager>{
    private Activity mActivity;
    private ArrayList<File> mData;
    private ArtCoverManager mACM;
    MusicPlayer mMusicPlayer;

    private final ArtCoverHandler mACH = new ArtCoverHandler() {
        @Override
        public void onBitmapFetched(Bitmap bitmap, int position) {
            if (bitmap != null) {
                notifyItemChanged(position);
            }
        }
    };

    public MusicAdapter(Activity activity, ArrayList<File> data) {
        mData = data;
        update(activity, true);
    }

    public Activity getActivity(){
        return mActivity;
    }

    protected void setActivity(Activity activity){
        mActivity = activity;
    }

    public void setMusicPlayer(MusicPlayer player){
        mMusicPlayer = player;
    }

    public void play(File file){
        if(mMusicPlayer != null){
            mMusicPlayer.play(file);
        }
    }

    public void addToQueue(File file){
        if(mMusicPlayer != null){
            mMusicPlayer.addToQueue(file);
        }
    }

    public void update(Activity activity, boolean fromNull) {
        if (activity != null) {
            mActivity = activity;
            if(!fromNull){
                mActivity.getLoaderManager().initLoader(getArtCoverManagerCode(), null, this);
            }
            updateContext(activity, fromNull);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mActivity.getLayoutInflater().inflate(getItemViewParent(), parent, false);
        return getViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        onBindViewHolder(holder, mData, position);
    }

    public void swapData(ArrayList<File> data,boolean insert, int insertPosition, boolean delete, int deletePosition) {
        int oldSize = 0;
        if(data == null){
            return;
        }
        if (mData != data) {
            if(mData == null){
                mData = data;
            } else {
                oldSize = mData.size();
                mData.clear();
                mData.addAll(data);
            }
        }
        if(mACM != null) {
            mACM.dataChanged(data.size(),insert,insertPosition,delete,deletePosition);
        }
        if(insert & delete){
            return;
        }
        if (insert){
            if(insertPosition >= oldSize || insertPosition < 0){
                notifyDataSetChanged();
            } else {
                notifyItemInserted(insertPosition);
            }
            return;
        }
        if(delete){
            if(deletePosition < 0 || deletePosition >= oldSize){
                notifyDataSetChanged();
            } else {
                notifyItemRemoved(deletePosition);
            }
            return;
        }
        notifyDataSetChanged();
    }

    @Deprecated
    public void switchData(int moving, int p){
        if(moving == p){
            return;
        }
        int size = mData.size();
        if(size < 2){
            return;
        }
        if(moving < 0 || moving >= size){
            throw new IndexOutOfBoundsException("array size is:" + size + ", index is: " + moving);
        }
        if (p < 0 || p >= size){
            throw new IndexOutOfBoundsException("array size is:" + size + ", index is: " + p);
        }
        if(mACM != null){
            mACM.switchData(moving,p);
        }
        File mov = mData.get(moving);
        mData.remove(moving);
        mData.add(p,mov);

        int start = moving > p ? p : moving;
        int end = start == moving ? p : moving;
        notifyItemRangeChanged(start,end - start + 1);
    }

    public void setMusicCover(ImageView view, File file, int position){
        if(mACM == null){
            update(mActivity, false);
            return;
        }
        Bitmap bitmap = mACM.getArtCover(file, position);
//        Log.e(bitmap == null ? "null" : "not null","position is: " + position + ", file is: " + file.getName());
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        } else {
            view.setImageResource(R.drawable.ic_crop_original_black_24dp);
        }
    }

    public void release(){
        if(mACM != null){
            mACM.release();
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Loader<ArtCoverManager> onCreateLoader(int id, Bundle args) {
        return new ArtCoverManagerLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArtCoverManager> loader, ArtCoverManager data) {
        mACM = data;
    }
    @Override
    public void onLoaderReset(Loader<ArtCoverManager> loader) {

    }

    protected abstract void updateContext(Context context, boolean fromNull);
    protected abstract void onBindViewHolder(VH holder, ArrayList<File> data,int position);
    protected abstract @LayoutRes int getItemViewParent();
    protected abstract VH getViewHolder(View view);
    protected abstract int getArtCoverManagerCode();

    public interface MusicPlayer{
        void play(File file);
        void addToQueue(File file);
    }
    private static class ArtCoverManagerLoader extends Loader<ArtCoverManager> {
        MusicAdapter mAdapter;
        ArtCoverHandler mACH;
        ArtCoverManager mAM;
        ArtCoverManagerLoader(MusicAdapter adapter) {
            super(adapter.mActivity);
            mAdapter = adapter;
            mACH = new ArtCoverHandler() {
                @Override
                public void onBitmapFetched(Bitmap bitmap, int position) {
                    if (bitmap != null) {
                        mAdapter.notifyItemChanged(position);
                    }
                }
            };
            mAM = ArtCoverManager.bind(mAdapter.mActivity,mACH,100);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onForceLoad() {
            deliverResult(mAM);
        }
    }

}
