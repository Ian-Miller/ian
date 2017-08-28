package ian.a;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import ian.a.music.ArtCoverManager;
import ian.a.music.Music;
import ian.a.music.MusicAdapter;
import ian.a.music.MusicData;
import ian.a.music.MusicService;
import ian.a.one.OneAdapter;
import ian.a.view.InfoView;
import ian.a.view.ProgressBar;
import ian.a.zero.ZeroAdapter;

import static ian.a.R.id.one_search;
import static ian.a.R.layout.a;
import static ian.a.R.layout.one;
import static ian.a.R.layout.zero;

public class A extends AppCompatActivity implements LoaderManager.LoaderCallbacks, MusicAdapter.MusicPlayer {
    private static final String TAG = "ian.a.A";

    Content mContent;
    static Toast mToast = null;

    boolean needRelease = false;

    ViewPager mViewPager;
    TabLayout mTabLayout;
    ProgressBar mProgressBar;

    File currentPlaying = null;

    ArtCoverTask artCoverTask;

    private class ArtCoverTask extends AsyncTask<File, Void, Bitmap> {

        private boolean running = false;

        @Override
        protected Bitmap doInBackground(File... params) {
            running = true;
            if (params != null && params.length > 0) {

                Bitmap bitmap = null;
                File file = params[0];
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(file.getAbsolutePath());
                    byte[] data = mmr.getEmbeddedPicture();
                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, op);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                playBarCoverButton.setImageBitmap(bitmap);
            } else {
                playBarCoverButton.setImageResource(R.drawable.ic_crop_original_black_24dp);
            }
            running = false;
        }

        public boolean isRunning() {
            return running;
        }
    }

    private static class RefreshMusicDataAsyncLoader extends AsyncTaskLoader<ArrayList<File>> {
        static final String TAG = "MusicDataAsyncLoader";
        Context mContext;

        RefreshMusicDataAsyncLoader(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public ArrayList<File> loadInBackground() {
            return Music.getMusics(mContext);
        }
    }

    private static class RefreshMusicQueueAsyncLoader extends AsyncTaskLoader<ArrayList<File>> {
        Context mContext;

        public RefreshMusicQueueAsyncLoader(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }


        @Override
        public ArrayList<File> loadInBackground() {
            return Music.getMusicQueue(mContext);
        }

        @Override
        public void deliverResult(ArrayList<File> data) {
            super.deliverResult(data);
        }
    }

    View.OnClickListener play = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            music_play(null);
        }
    };

    View.OnClickListener previous = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send(null, Music.MUSIC_ACTION_PLAY_PREVIOUS);
        }
    };

    View.OnClickListener next = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send(null, Music.MUSIC_ACTION_PLAY_NEXT);
        }
    };

    void toast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(a);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        playBarCoverButton = (ImageView) findViewById(R.id.play_bar_cover_button);
        playBarPlayButton = (ImageButton) findViewById(R.id.play_bar_play_button);
        playBarPreviousButton = (ImageButton) findViewById(R.id.play_bar_previous_button);
        playBarNextButton = (ImageButton) findViewById(R.id.play_bar_next_button);

//        playAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(this,R.animator.one_play_vector_animator);
//        Drawable drawable = playBarPlayButton.getDrawable();
//        playAnimator.setTarget(drawable);

        playBarPlayButton.setOnClickListener(play);
        playBarPreviousButton.setOnClickListener(previous);
        playBarNextButton.setOnClickListener(next);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        LoaderManager manager = getLoaderManager();
        manager.initLoader(0, null, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Music.A_ACTION_UPDATE_UI);
        filter.addAction(Music.A_ACTION_UPDATE_STATE);
        filter.addAction(Music.A_ACTION_FINISH);
        filter.addAction(Music.A_ACTION_UI_PAUSE);
        filter.addAction(Music.A_ACTION_UI_PLAY);
        registerReceiver(mMSBR, filter);

        startService(new Intent(this, MusicService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        send(null, Music.MUSIC_ACTION_IF_PLAYING);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMSBR);
        release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mContent != null){
            if(mContent.onBackPressed()){
                return;
            }
        }
        needRelease = true;
        super.onBackPressed();
    }

    private void release(){
        if(needRelease){
            if(mContent != null){
                mContent.release();
                mContent = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Content.REQUEST_CODE) {
            int index = -1;
            for (int i = 0, l = permissions.length; i < l; i++) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    index = i;
                    break;
                }
            }
            if (index != -1 && grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                if (mContent != null && !mContent.isReleased()) {
                    mContent.startOneTask();
                }
            }
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "onCreateLoader(int id, Bundle args)");
        switch (id) {
            case 0:
                return new ContextLoader(this, new Content(this));
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onLoadFinished(Loader loader, Object data) {
        if (loader instanceof ContextLoader) {
            mContent = (Content) data;
            mContent.updateContext(this,false);
            return;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        loader.reset();
    }

    ImageView playBarCoverButton;
    ImageButton playBarPlayButton;
    ObjectAnimator playAnimator;
    ImageButton playBarPreviousButton;
    ImageButton playBarNextButton;

    @Override
    public void play(File file) {
        send(file, Music.MUSIC_ACTION_PLAY);
    }

    public void setPause(){
        if(playAnimator.isStarted()){
            playAnimator.reverse();
        } else {
            playAnimator.start();
        }
    }
    public void setPlay(){
        if(playAnimator.isStarted()){

        } else {
            playAnimator.start();
        }
    }

    @Override
    public void addToQueue(File file) {
        if (mContent != null) {
            mContent.addToQueue(file);
        }
    }

    private void literateFile(ArrayList<File> out, File file, Progress progress) {
        if (!file.isDirectory()) {
            String name = file.getName();
            int l = name.length();
            String end = l >= 4 ? name.substring(l - 4) : "";
            if (end.equals(".mp3")) {
                out.add(file);
                return;
            }
            end = l > 4 ? name.substring(l - 5) : "";
            if (end.equals(".flac")) {
                out.add(file);
                return;
            }
            return;
        }
        try {
            File[] children = file.listFiles();
            progress.in(children.length);
            int l = children.length;
            for (File child : children) {
                progress.stepForward();
                literateFile(out, child, progress);
            }
            progress.out();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Progress {
        private ArrayList<Floor> floors = new ArrayList<>();
        private int currentFloor = -1;

        public Progress() {

        }

        public interface Callback {
            void postUpdate(int value);
        }

        Callback mCallback;

        public void setCallback(Callback c) {
            mCallback = c;
        }

        private void postUpdate() {
            if (mCallback != null) {
                mCallback.postUpdate(getProgress());
            }
        }

        private boolean done() {
            if (floors.size() == 0) {
                return false;
            }
            Floor floor = floors.get(0);
            int l = floor.length;
            int i = floor.index;
            return i == l - 1;
        }

        public int getProgress() {
            float p = 0;
            float total = 100;
            int end = currentFloor;
            if (done()) {
                return 100;
            }
            for (int i = 0; i <= end; i++) {
                Floor cf = floors.get(i);
                if (cf.length == 0) {
                    break;
                }
                p += total * (cf.index + 1) / cf.length;
                total /= cf.length;
            }
            return (int) p;
        }

        public void in(int length) {
            int totalFloor = floors.size();
            if (currentFloor == totalFloor - 1) {
                floors.add(new Floor(length));
            }
            currentFloor++;
            floors.get(currentFloor).index = 0;
            postUpdate();
        }

        public void out() {
            if (currentFloor == -1) {
                throw new RuntimeException("can't step out, already in the first floor!");
            }
            floors.get(currentFloor).index = 0;
            currentFloor--;

        }

        public void stepForward() {
            floors.get(currentFloor).stepForward();
            postUpdate();
        }

        private class Floor {
            private static final String TAG = "Floor";
            private int index = 0;
            private int length = 0;

            private Floor() throws Exception {
                throw new Exception();
            }

            Floor(int l) {
                if (length < 0) {
                    throw new RuntimeException("floor length can't less than 0");
                }
                index = 0;
                length = l;

            }

            void stepForward() {
                if (index >= length - 1) {
                    return;
                }
                index++;
            }

            void stepBack() {
                if (index == 0) {
                    Log.e(TAG, "can't step out, already in the first place");
                    return;
                }
                index--;
            }
        }

    }

    private class MusicServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Music.A_ACTION_UPDATE_UI:
                    File file = (File) intent.getSerializableExtra(Music.MUSIC_URI);
                    if (file == null) {
                        playBarCoverButton.setImageResource(R.drawable.ic_crop_original_black_24dp);
                        break;
                    }
                    currentPlaying = file;
                    if (artCoverTask != null && artCoverTask.isRunning()) {
                        artCoverTask.cancel(true);
                    }
                    artCoverTask = new ArtCoverTask();
                    artCoverTask.execute(file);
                    break;
                case Music.A_ACTION_FINISH:
                    finish();
                    break;
                case Music.A_ACTION_UI_PLAY:
//                    setPlay();
                    playBarPlayButton.setImageResource(R.drawable.ic_play_circle_outline_black_72dp);
                    break;
                case Music.A_ACTION_UI_PAUSE:
//                    setPause();
                    playBarPlayButton.setImageResource(R.drawable.ic_pause_circle_outline_black_72dp);
                    break;
            }
        }
    }

    MusicServiceBroadcastReceiver mMSBR = new MusicServiceBroadcastReceiver();

    private void send(File file, String action) {
        Intent i = new Intent(action);
        i.putExtra(Music.MUSIC_URI, file);
        sendBroadcast(i);
    }

    public void music_play(File file) {
        send(file, Music.MUSIC_ACTION_PLAY);
    }

    private boolean checkPermission(String permission) {
        int p = ActivityCompat.checkSelfPermission(A.this, permission);
        return p == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_UI_HIDDEN:
                Log.e(TAG, "TRIM_MEMORY_UI_HIDDEN: " + level);
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
            case TRIM_MEMORY_RUNNING_LOW:
            case TRIM_MEMORY_RUNNING_MODERATE:
                Log.e(TAG, "TRIM_MEMORY_RUNNING: " + level);
                break;
            case TRIM_MEMORY_BACKGROUND:
            case TRIM_MEMORY_COMPLETE:
            case TRIM_MEMORY_MODERATE:
                Log.e(TAG, "TRIM_MEMORY: " + level);
                break;
            default:
                Log.v(TAG, "BAD_MEMORY: " + level);
        }
    }

    private static final class Content extends ContextContent implements LoaderManager.LoaderCallbacks {
        boolean released = false;

        A A;

        static final int REQUEST_CODE = 333;

        private static final int ONE_FLOATING_BUTTON_SHOW_MASK = 0x001;

        private int mFlag = 0;

        ArrayList<File> mMusics = new ArrayList<>();
        ArrayList<File> mMusicsQueue = new ArrayList<>();
        ArrayList<File> tempData;

        AAdapter mAAdapter;
        ZeroAdapter zeroAdapter;
        OneAdapter oneAdapter;
        OneShowZoneAdapter oneShowZoneAdapter;

        View[] sPages;

        RecyclerView zeroContent;
        RecyclerView oneContent;
        RecyclerView oneShowZoneRecyclerView;
        FrameLayout oneShowZone;
        EditText searchBar;
        FloatingActionButton searchView;
        InfoView infoView1;
        InfoView infoView2;
        InfoView infoView3;

        private static final long DURATION = 500;
        ObjectAnimator mFloatingButtonAnimator;

        FetchMusicTask fetchMusicTask;
        FetchMusicQueueTask fetchMusicQueueTask;
        OneTask oneTask;
        ZeroStoreQueueTask zeroStoreQueueTask;
        OneStoreDataTask one_task_storeData;

        InfoView.Info info1;
        InfoView.Info info2;
        InfoView.Info info3;

        View.OnClickListener one_search_listener;
        TextWatcher one_textWatcher;
        View.OnFocusChangeListener searchBarFocusChangeListener;

        public Content(A a) {
            super(a);
        }

        public boolean onBackPressed(){
            if((mFlag & ONE_FLOATING_BUTTON_SHOW_MASK) == ONE_FLOATING_BUTTON_SHOW_MASK){
                searchZone(null,false);
                return true;
            }
            return false;
        }

        @Override
        public void updateContext(Context context, boolean fromNull) {
            if(!(context instanceof A)){
                return;
            }
            if(context != A){
                A = (A) context;
                defaultAssignment();
                updatePages();
            }
            int colorAccent = ContextCompat.getColor(A, R.color.colorAccent);
            if (fromNull) {
                fetchMusic();
                fetchMusicQueue();
                zeroAdapter = new ZeroAdapter(A, mMusicsQueue);
                oneAdapter = new OneAdapter(A, mMusics);
                defaultAssignment();
            } else {
                mAAdapter = new AAdapter(sPages);
                A.mViewPager.setAdapter(mAAdapter);
                A.mTabLayout.setupWithViewPager(A.mViewPager);
                A.mTabLayout.setTabTextColors(0xffffffff, colorAccent);
                A.mTabLayout.setSelectedTabIndicatorColor(colorAccent);

                zeroAdapter.update(A,false);
                zeroContent.setAdapter(zeroAdapter);
                zeroAdapter.swapData(mMusicsQueue,false,-1,false,-1);
                zeroAdapter.setMusicPlayer(A);
                ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.RIGHT);
                        return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE,ItemTouchHelper.RIGHT);
                    }
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
//                        int moving = viewHolder.getAdapterPosition();
//                        int p = target.getAdapterPosition();
//                        switchInQueue(moving,p);
//                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        deleteFromQueue(mMusicsQueue.get(position));
                    }
                };
                ItemTouchHelper helper = new ItemTouchHelper(callback);
                helper.attachToRecyclerView(zeroContent);

                oneAdapter.update(A,false);
                oneContent.setAdapter(oneAdapter);
                oneAdapter.swapData(mMusics,false,-1,false,-1);
                oneAdapter.setMusicPlayer(A);
            }
        }
        private void defaultAssignment(){
            one_search_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (oneTask != null && oneTask.isRunning()) {
                        return;
                    }
                    if (A.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (oneTask != null && oneTask.isRunning()) {
                            return;
                        }
                        oneTask = new OneTask();
                        oneTask.execute();
                    } else {
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(A, permission, REQUEST_CODE);
                    }
                }
            };
            info1 = new InfoView.Info() {
                @Override
                public String info() {
                    Runtime r = Runtime.getRuntime();
                    long totalM = r.totalMemory();
                    long freeM = r.freeMemory();
                    long usedM = totalM - freeM;
                    long total = totalM / 0x100000;
                    long free = freeM / 0x100000;
                    long used = usedM / 0x100000;
                    String info = "free memory: " + free + "MB, " +
                            "total memory: " + total + "MB, " +
                            "used memory: " + used + "MB";
                    return info;
                }
            };
            info2 = new InfoView.Info() {
                @Override
                public String info() {
                    String currentThreadName = Thread.currentThread().getName();
                    String info = "current thread: " + currentThreadName;
                    return info;
                }
            };
            info3 = new InfoView.Info() {
                @Override
                public String info() {
                    Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
                    int size = map.size();
                    Set<Thread> keySet = map.keySet();
                    Object[] objs = keySet.toArray();
                    StringBuilder info = new StringBuilder();
                    String a = "threads count is: " + size + "\n" + objs.getClass().getName();
                    info.append(a);
                    try {
                        for (Object t : objs) {
                            String s = ((Thread) t).getName() + "\n";
                            info.append(s);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return info.toString();
                }
            };
            one_textWatcher = new TextWatcher() {
                ArrayList<File> temp = new ArrayList<>();
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp.clear();
                    ArrayList<File> musics = mMusics;
                    for (File file : musics) {
                        String name = file.getName();
                        name = name.toLowerCase();
                        if (compare(name, s.toString().toLowerCase())) {
                            if (temp.contains(file)) {
                                continue;
                            }
                            temp.add(file);
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    int length = s.length();
                    searchZone(temp, length != 0);
                }

                boolean compare(String parent, String child) {
                    if (parent.length() == 0 || parent.length() < child.length()) {
                        return false;
                    }
                    while (parent.length() > 0 && child.length() > 0) {
                        if (child.charAt(0) == parent.charAt(0)) {
                            child = child.substring(1);
                        }
                        parent = parent.substring(1);
                        if (child.length() < 1) {
                            return true;
                        }
                        if (parent.length() < 1) {
                            return false;
                        }
                    }
                    return child.length() == 0;
                }
            };
            searchBarFocusChangeListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus){
                        searchZone(null, false);
                    }
                }
            };
        }

        private void updatePages() {
            final int pageCount = 4;
            sPages = new View[pageCount];

            updatePage0();
            updatePage1();
            updatePage2();
            updatePage3();


            released = false;
        }

        private void updatePage0() {
            sPages[0] = A.getLayoutInflater().inflate(zero, A.mViewPager, false);
            zeroContent = (RecyclerView) sPages[0].findViewById(R.id.zero_recyclerview);
            zeroContent.setLayoutManager(new LinearLayoutManager(A));
            sPages[0].setTag("play queue");
        }

        private void updatePage1() {
            sPages[1] = A.getLayoutInflater().inflate(one, A.mViewPager, false);
            sPages[1].setTag("music");

            searchView = (FloatingActionButton) sPages[1].findViewById(one_search);
            oneContent = (RecyclerView) sPages[1].findViewById(R.id.one_content);
            oneShowZone = (FrameLayout) sPages[1].findViewById(R.id.one_search_zone_root);
            oneShowZoneRecyclerView = (RecyclerView) sPages[1].findViewById(R.id.one_search_show);
            searchBar = (EditText) sPages[1].findViewById(R.id.one_search_input);

            oneShowZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchZone(null, false);
                }
            });
            searchBar.setHighlightColor(ContextCompat.getColor(A, R.color.colorAccent));
            oneContent.setHasFixedSize(true);
            oneContent.setLayoutManager(new LinearLayoutManager(A));
            oneShowZoneRecyclerView.setLayoutManager(new LinearLayoutManager(A));
            searchView.setOnClickListener(one_search_listener);
            searchBar.addTextChangedListener(one_textWatcher);

            searchBar.setOnFocusChangeListener(searchBarFocusChangeListener);

            tempData = new ArrayList<>();
            oneShowZoneAdapter = new OneShowZoneAdapter(A,tempData);
            oneShowZoneRecyclerView.setAdapter(oneShowZoneAdapter);
            oneShowZoneAdapter.setMusicPlayer(A);

            initialBehaviour((CoordinatorLayout) sPages[1]);
            A.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        private void updatePage2() {
            sPages[2] = A.getLayoutInflater().inflate(R.layout.two, A.mViewPager, false);
            sPages[2].setTag("info");

            infoView1 = (InfoView) sPages[2].findViewById(R.id.two_1);
            infoView1.setInfo(info1);
            infoView2 = (InfoView) sPages[2].findViewById(R.id.two_2);
            infoView2.setInfo(info2);
            infoView3 = (InfoView) sPages[2].findViewById(R.id.two_3);
            infoView3.setInfo(info3);
        }

        private void updatePage3(){
            sPages[3] = A.getLayoutInflater().inflate(R.layout.three,A.mViewPager,false);
            sPages[3].setTag("try and test everything");
            View v = sPages[3].findViewById(R.id.three_test);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)v.getBackground();
                    drawable.start();

                }
            });



        }

        private void initialBehaviour(CoordinatorLayout parent) {
            final FloatingActionButton chi = (FloatingActionButton) parent.findViewById(R.id.one_search);
            mFloatingButtonAnimator = ObjectAnimator.ofFloat(chi, "translationY", 0, 1024);

            mFloatingButtonAnimator.setDuration(DURATION);

            ViewGroup.LayoutParams oldParams = chi.getLayoutParams();
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(oldParams);
            params.setMargins(0, 0, 128, 128);
            params.gravity = Gravity.END | Gravity.BOTTOM;
            CoordinatorLayout.Behavior<FloatingActionButton> beh1 = new CoordinatorLayout.Behavior<FloatingActionButton>() {
                @Override
                public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
                    return directTargetChild instanceof RecyclerView;
                }

                @Override
                public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
                    if (target instanceof RecyclerView) {
                        if (!mFloatingButtonAnimator.isStarted()) {
                            float f = child.getTranslationY();
                            if (f != 0) {
                                return;
                            }
                            mFloatingButtonAnimator.start();
                            return;
                        }
                        long time = mFloatingButtonAnimator.getCurrentPlayTime();
                        mFloatingButtonAnimator.start();
                        mFloatingButtonAnimator.setCurrentPlayTime(DURATION - time);

                    }
                }

                @Override
                public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target) {
                    if (target instanceof RecyclerView) {
                        if (!mFloatingButtonAnimator.isStarted()) {
                            mFloatingButtonAnimator.reverse();
                            return;
                        }
                        long time = mFloatingButtonAnimator.getCurrentPlayTime();
                        mFloatingButtonAnimator.setCurrentPlayTime(time);
                        mFloatingButtonAnimator.reverse();
                    }
                }
            };
            params.setBehavior(beh1);
            chi.setLayoutParams(params);
        }

        private void searchZone(ArrayList<File> data, boolean appear){
            FrameLayout view = oneShowZone;
            if(appear){
                view.setVisibility(View.VISIBLE);
                oneShowZoneAdapter.swapData(data,false,-1,false,-1);
                searchView.hide();
                mFlag |= ONE_FLOATING_BUTTON_SHOW_MASK;
            } else {
                oneShowZoneAdapter.notifyDataSetChanged();
                view.setVisibility(View.GONE);
                searchView.show();
                InputMethodManager imm = (InputMethodManager) A.getSystemService(Context.INPUT_METHOD_SERVICE);
                IBinder iBinder = searchBar.getWindowToken();
                imm.hideSoftInputFromWindow(iBinder,0);
                searchBar.clearFocus();
                mFlag &= ~ONE_FLOATING_BUTTON_SHOW_MASK;
            }
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            switch (id) {
                case 1:
                    return new RefreshMusicDataAsyncLoader(A);
                case 2:
                    return new RefreshMusicQueueAsyncLoader(A);
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onLoadFinished(Loader loader, Object data) {
            if (loader instanceof RefreshMusicDataAsyncLoader) {
                oneAdapter.swapData(mMusics,false,-1,false,-1);
            } else if (loader instanceof RefreshMusicQueueAsyncLoader) {
                mMusicsQueue = data == null ? new ArrayList<File>() : (ArrayList<File>) data;
                zeroAdapter.swapData(mMusicsQueue,false,-1,false,-1);
                mMusics = (ArrayList<File>) data;
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {
            loader.reset();
        }

        private ArrayList<File> one_search(Progress p) {
            ArrayList<File> out = new ArrayList<>();
            File root = Environment.getExternalStorageDirectory();
            Log.e(TAG, root.getAbsolutePath());
            A.literateFile(out, root, p);
            return out;
        }

        public void addToQueue(File file) {
            if (!mMusicsQueue.contains(file)) {
                Music.addToMusicQueue(A, file);
                A.toast("adding to play queue successful");
                mMusicsQueue.add(file);
                zeroAdapter.swapData(mMusicsQueue,true,-1,false,-1);
                Intent i = new Intent(Music.MUSIC_REFRESH);
                A.sendBroadcast(i);
                return;
            }
            A.toast("music already in play queue");
        }

        public void deleteFromQueue(File file){
            Music.deleteFromQueue(A,file);
            A.send(null,Music.MUSIC_REFRESH);
            int position = mMusicsQueue.indexOf(file);
            mMusicsQueue.remove(file);
            zeroAdapter.swapData(mMusicsQueue,false,-1,true,position);
        }

        @Deprecated
        public void switchInQueue(int moving, int p){
            if(zeroAdapter != null){
                zeroAdapter.switchData(moving,p);
            }
            startOneZeroTask();
        }

        private void startOneTask() {
            if (oneTask != null && oneTask.isRunning()) {
                return;
            }
            oneTask = new OneTask();
            oneTask.execute();
        }

        private void startOneZeroTask(){
            if(zeroStoreQueueTask != null && zeroStoreQueueTask.isRunning()){
                zeroStoreQueueTask.cancel(true);
            }
            zeroStoreQueueTask = new ZeroStoreQueueTask();
            zeroStoreQueueTask.execute();
        }

        private void startOneStoreTask() {
            if (one_task_storeData != null && one_task_storeData.isRunning) {
                return;
            }
            one_task_storeData = new OneStoreDataTask();
            one_task_storeData.execute();
        }

        public boolean isReleased() {
            return released;
        }

        @Override
        public void release() {
            released = true;
            if (oneTask != null) {
                oneTask.cancel();
            }
            oneTask = null;
            if(oneAdapter != null){
                oneAdapter.release();
            }
            if(zeroAdapter != null){
                zeroAdapter.release();
            }
        }

        private class FetchMusicTask extends AsyncTask<Void, Void, ArrayList<File>>{
            boolean running = false;
            boolean isRunning(){
                return running;
            }
            @Override
            protected ArrayList<File> doInBackground(Void... params) {
                return Music.getMusics(A);
            }
            @Override
            protected void onPostExecute(ArrayList<File> files) {
                mMusics = files;
                oneAdapter.swapData(mMusics,false,-1,false,-1);
            }
        }
        private class FetchMusicQueueTask extends AsyncTask<Void, Void, ArrayList<File>>{
            boolean running = false;
            boolean isRunning(){
                return running;
            }
            @Override
            protected ArrayList<File> doInBackground(Void... params) {
                return Music.getMusicQueue(A);
            }
            @Override
            protected void onPostExecute(ArrayList<File> files) {
                mMusicsQueue = files;
                zeroAdapter.swapData(mMusicsQueue,false,-1,false,-1);
            }
        }

        void fetchMusic(){
            if(fetchMusicTask != null && fetchMusicTask.isRunning()){
                return;
            }
            fetchMusicTask = new FetchMusicTask();
            fetchMusicTask.execute();
        }
        void fetchMusicQueue(){
            if(fetchMusicQueueTask != null && fetchMusicTask.isRunning()){
                return;
            }
            fetchMusicQueueTask = new FetchMusicQueueTask();
            fetchMusicQueueTask.execute();
        }

        private class OneTask extends AsyncTask<Void, Integer, ArrayList<File>> implements Progress.Callback {

            @Override
            protected ArrayList<File> doInBackground(Void... params) {
                try {
                    running = true;
                    Progress p = new Progress();
                    p.setCallback(this);
                    return one_search(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return mMusics;
            }

            @Override
            public void postUpdate(int value) {
                publishProgress(value);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                A.mProgressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(ArrayList<File> data) {
                if (data != mMusics) {
                    mMusics.clear();
                    mMusics.addAll(data);
                    oneAdapter.swapData(mMusics,false,-1,false,-1);
                }
                A.mProgressBar.clearProgress();
                startOneStoreTask();
                running = false;
            }

            private boolean running = false;

            public boolean isRunning() {
                return running;
            }

            public void cancel() {
                if (isRunning()) {
                    cancel(true);
                }
            }
        }

        private class OneStoreDataTask extends AsyncTask<Void, Void, Void> {
            boolean isRunning = false;

            @Override
            protected Void doInBackground(Void... params) {
                isRunning = true;
                ArrayList<File> data = mMusics;
                MusicData.OneMusicDataHelper helper = new MusicData.OneMusicDataHelper(A);
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(MusicData.MusicDataContract.TABLE_NAME, null, null);
                for (File file : data) {
                    db.insert(MusicData.MusicDataContract.TABLE_NAME, MusicData.MusicDataContract.COLUMN_PATH, getContentValues(file));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void data) {
                isRunning = false;
            }

            private ContentValues getContentValues(File file) {
                ContentValues cv = new ContentValues();
                cv.put(MusicData.MusicDataContract.COLUMN_PATH, file.getAbsolutePath());
                return cv;
            }
        }

        private class ZeroStoreQueueTask extends AsyncTask<Void,Void,Void> {
            boolean running = false;
            @Override
            protected Void doInBackground(Void... params) {
                running = true;
                try{
                    Music.updateQueue(A,mMusicsQueue);
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                A.send(null,Music.MUSIC_REFRESH);
                running = false;
            }

            boolean isRunning(){
                return running;
            }
        }

        private static class OneShowZoneAdapter extends MusicAdapter<OneShowZoneAdapter.ViewHolder>{
            public OneShowZoneAdapter(Activity activity, ArrayList<File> data) {
                super(activity, data);
            }

            @Override
            public void update(Activity activity, boolean fromNull) {
                if (activity == null){
                    return;
                }
                setActivity(activity);
                updateContext(activity, fromNull);
            }

            @Override
            public void updateContext(Context context, boolean fromNull) {

            }
            @Override
            protected void onBindViewHolder(ViewHolder holder, ArrayList<File> data, int position) {
                final File file = data.get(position);
                String name = file.getName();
                holder.descriptionView.setText(name);
                holder.descriptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToQueue(file);
                        play(file);
                    }
                });
            }
            @Override
            protected int getItemViewParent() {
                return R.layout.one_recyclerview_search_item;
            }
            @Override
            protected ViewHolder getViewHolder(View view) {
                return new ViewHolder(view);
            }
            @Override
            protected int getArtCoverManagerCode() {
                return -1;
            }

            @Override
            public Loader<ArtCoverManager> onCreateLoader(int id, Bundle args) {
                return null;
            }

            @Override
            public void onLoadFinished(Loader<ArtCoverManager> loader, ArtCoverManager data) {

            }

            @Override
            public void onLoaderReset(Loader<ArtCoverManager> loader) {

            }

            class ViewHolder extends RecyclerView.ViewHolder{
                View mItemView;
                TextView descriptionView;
                public ViewHolder(View itemView) {
                    super(itemView);
                    mItemView = itemView;
                    descriptionView = (TextView) itemView.findViewById(R.id.one_search_item_description);
                }
            }
        }

    }
}
