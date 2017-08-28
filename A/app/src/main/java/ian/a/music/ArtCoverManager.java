package ian.a.music;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ian.a.R;

import static java.lang.Runtime.getRuntime;

/**
 * Created by Ian on 8/23/2017.
 */

public final class ArtCoverManager {
    private static final String TAG = "ArtCoverManager";
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int CAPACITY = 100;

    private Context mContext;
    private Bitmap[] mBitmaps;
    private final ThreadPoolExecutor threadPool;

    private static int NUMBER_OF_AVAILABLE_CORES = getRuntime().availableProcessors();
    private final ArtCoverHandler mHandler;

    private class ArtRunnable implements Runnable {
        private File mFile;
        private int mPosition;
        private Runnable mRunnable;

        ArtRunnable(File file, int position, @Nullable Runnable runnable) {
            mFile = file;
            mPosition = position;
            mRunnable = runnable;
        }

        @Override
        public void run() {
            Bitmap b = fetchArtCover(mFile);
            if (mPosition > mBitmaps.length - 1) {
                return;
            }
            mBitmaps[mPosition] = b;
            Message msg = new Message();
            msg.what = ArtCoverHandler.MSG_BITMAP_FETCHED;
            msg.arg1 = mPosition;
            msg.obj = b;
            mHandler.sendMessage(msg);
            if (mRunnable != null) {
                mRunnable.run();
            }
        }
    }

    private ArtCoverManager(Context context, ArtCoverHandler handler, int size) {
        mContext = context;
        mBitmaps = new Bitmap[size];
        mHandler = handler;
        Integer[] stack = new Integer[size];
        for (int i = 0; i < size; i++) {
            stack[i] = i;
        }
        if (Looper.getMainLooper() != handler.getLooper()) {
            throw new RuntimeException("handler must be in the main thread!");
        }
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        threadPool = new ThreadPoolExecutor(NUMBER_OF_AVAILABLE_CORES,
                NUMBER_OF_AVAILABLE_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                queue);
    }

    public static ArtCoverManager bind(Context context, ArtCoverHandler handler, int size) {
        return new ArtCoverManager(context, handler, size);
    }

    /**
     * run in thread pool
     *
     * @param file
     * @param position
     * @return true if need work in thread pool, main thread otherwise;
     */
    private void artCover(File file, int position) {
        int size = mBitmaps.length;
        if (position > size - 1) {
            throw new RuntimeException("unknown exception occurred");
        }
        ArtRunnable runnable = new ArtRunnable(file, position, null);
        threadPool.execute(runnable);
    }

    public Bitmap getArtCover(File file, int position) {
        int size = mBitmaps.length;
        if (position > size - 1) {
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_crop_original_black_24dp);
        }
        Bitmap b = mBitmaps[position];
        if (b != null && !b.isRecycled()) {
            return b;
        }
//        Runtime r = Runtime.getRuntime();
//        int indicator = 1;
//        long usedBefore = -1;
//        long usedAfter;
//        String info;
//        while (needClearMemory()){
//            if(indicator %  30 == 0) {
//                usedBefore = (r.totalMemory() - r.freeMemory()) / 0x100000;
//            }
//            if(!clearMemory()){
//                break;
//            }
//            if(indicator % 30 == 29 && usedBefore >= 0){
//                usedAfter = (r.totalMemory() - r.freeMemory()) / 0x100000;
//                info = "cleared before memory is: " + usedBefore + "MB\n" +
//                        "cleared after memory is:" + usedAfter + "MB";
//                Log.e(TAG,info);
//            }
//            indicator++;
//        }
        artCover(file, position);
        return null;
    }

//    private boolean needClearMemory(){
//        Runtime r = Runtime.getRuntime();
//        long totalM = r.totalMemory();
//        long freeM = r.freeMemory();
//        long usedM = totalM - freeM;
//        long free = freeM / 0x100000;
//        long used = usedM / 0x100000;
////        Log.e("你好：", "totalM is: " + totalM +
////                        "\nfreeM is: " + freeM +
////                        "\nusedM is: " + usedM +
////                        "\nfreed is " + free +
////                        "\nused is: " + used);
//        return used > 25;
//    }

//    public boolean clearMemory(){
//        Integer position = mStack.removeLast();
//        if(position == null){
//            return false;
//        }
//        if(mBitmaps[position] != null) {
//            mBitmaps[position].recycle();
//            mBitmaps[position] = null;
//        }
//        return true;
//    }

    /**
     * run in thread pool
     *
     * @param file
     * @return
     */
    private Bitmap fetchArtCover(File file) {
        Bitmap b = null;
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getAbsolutePath());
            byte[] data = mmr.getEmbeddedPicture();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 8;
            if (data != null) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mmr != null) {
            mmr.release();
        }
        return b;
    }


    private void postTag(String s) {
        Message msg = new Message();
        msg.what = ArtCoverHandler.MSG_TAG;
        msg.obj = s;
        mHandler.sendMessage(msg);
    }

    public void release(){
        if(threadPool != null){
            try {
                threadPool.shutdownNow();
                threadPool.purge();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public abstract static class ArtCoverHandler extends Handler {
        public static final int MSG_BITMAP_FETCHED = 1;
        public static final int MSG_TAG = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BITMAP_FETCHED:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    onBitmapFetched(bitmap, msg.arg1);
                    break;
                case MSG_TAG:
                    Log.e(TAG, (String) msg.obj);
                    break;
            }
        }

        public abstract void onBitmapFetched(Bitmap bitmap, int position);
    }

    public void dataChanged(int size, boolean insert, int insertPosition, boolean delete, int deletePosition) {
        synchronized (this) {
            int length = mBitmaps.length;
            if (insert & delete) {
                return;
            }
            if (insert) {
                Bitmap[] newBitmaps = new Bitmap[length + 1];
                int cursor = 0;
                int i = 0;
                while (i < length) {
                    if (i == insertPosition) {
                        cursor++;
                    } else {
                        newBitmaps[cursor] = mBitmaps[i];
                    }
                    i++;
                    cursor++;
                }
                mBitmaps = newBitmaps;
                return;
            }
            if (delete) {
                if (length < 1) {
                    return;
                }
                if (deletePosition >= length || deletePosition < 0) {
                    return;
                }
                Bitmap[] newBitmaps = new Bitmap[length - 1];
                if (deletePosition > 0) {
                    System.arraycopy(mBitmaps, 0, newBitmaps, 0, deletePosition - 1);
                }
                System.arraycopy(mBitmaps, deletePosition + 1, newBitmaps, deletePosition, length - deletePosition - 1);
                mBitmaps = newBitmaps;
                return;
            }
            Bitmap[] newBitmaps = new Bitmap[size];
            int copySize = size > length ? length : size;
            System.arraycopy(mBitmaps, 0, newBitmaps, 0, copySize);
            mBitmaps = newBitmaps;
        }
    }

    @Deprecated
    public void switchData(int moving, int p){
        if(moving == p){
            return;
        }
        int size = mBitmaps.length;
        if(size < 2){
            return;
        }
        if(moving < 0 || moving >= size){
            throw new IndexOutOfBoundsException("array size is:" + size + ", index is: " + moving);
        }
        if (p < 0 || p >= size){
            throw new IndexOutOfBoundsException("array size is:" + size + ", index is: " + p);
        }
        Bitmap[] newBitmap = new Bitmap[size];
        int i = 0;
        int cursor = 0;
        Bitmap mov = mBitmaps[moving];

        while(i < size){
            if (i == p){
                newBitmap[i] = mov;
                cursor--;
                i++;
                continue;
            }else if (i == moving){
                cursor++;
            }
            newBitmap[i] = mBitmaps[i + cursor];
            i++;
        }
        mBitmaps = newBitmap;
    }
}


