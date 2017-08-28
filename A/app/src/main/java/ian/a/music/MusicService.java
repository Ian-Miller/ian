package ian.a.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ian.a.R;

public class MusicService extends Service {

    private static final String TAG = "OneMusicService";
    MediaPlayer mMediaPlayer;
    ArrayList<File> mMusics = new ArrayList<>();
    File currentPlaying;

    private boolean paused = false;
    private boolean processing = false;

    Toast mToast;

    BroadcastReceiver mBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Music.MUSIC_ACTION_PLAY)) {
                if (processing) {
                    toast("slow down!");
                    return;
                }
                processing = true;
                File file = (File) intent.getSerializableExtra(Music.MUSIC_URI);
                if (paused && (file == null || currentPlaying.equals(file))) {
                    resume();
                    return;
                }
                if (mMediaPlayer.isPlaying() && (file == null || file.equals(currentPlaying))) {
                    pause(true);
                    return;
                }
                if (file == null && mMusics != null && mMusics.size() > 0) {
                    file = mMusics.get(0);
                }
                currentPlaying = file;
                play();
                return;
            }
            if (action.equals(Music.MUSIC_REFRESH)) {
                initialMusics();
                return;
            }
            if (action.equals(Music.MUSIC_ACTION_PLAY_NEXT)) {
                processing = true;
                playNext();
                return;
            }
            if (action.equals(Music.MUSIC_ACTION_PLAY_PREVIOUS)) {
                processing = true;
                playPrevious();
                return;
            }
            if (action.equals(Music.MUSIC_ACTION_IF_PLAYING)) {
                send(currentPlaying, Music.A_ACTION_UPDATE_UI);
                if (mMediaPlayer.isPlaying()) {
                    send(null, Music.A_ACTION_UI_PAUSE);
                } else {
                    send(null, Music.A_ACTION_UI_PLAY);
                }
                return;
            }

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    AudioManager am;

    private boolean requestAudioFocus() {
        int response = am.requestAudioFocus(amListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return response == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    AudioManager.OnAudioFocusChangeListener amListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mMediaPlayer.isPlaying()) {
                        return;
                    }
                    if (paused) {
                        resume();
                    } else {
                        play();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                default:
                    pause(false);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        initialMusics();
        mMediaPlayer = new MediaPlayer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Music.MUSIC_ACTION_PLAY);
        filter.addAction(Music.MUSIC_REFRESH);
        filter.addAction(Music.MUSIC_ACTION_COVER_SHOULD_UPDATE);
        filter.addAction(Music.MUSIC_ACTION_PLAY_NEED_REFRESH);
        filter.addAction(Music.MUSIC_ACTION_PLAY_NEXT);
        filter.addAction(Music.MUSIC_ACTION_PLAY_PREVIOUS);
        filter.addAction(Music.MUSIC_ACTION_IF_PLAYING);
        registerReceiver(mBr, filter);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                requestAudioFocus();
                foreGroundNotification(currentPlaying.getName(),currentCover);
                mMediaPlayer.start();
                paused = false;
            }
        });
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void toast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    private PendingIntent pendingIntent() {
        Intent intent = new Intent(Music.MUSIC_ACTION_PLAY);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    void foreGroundNotification(String title, Bitmap bitmap) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent()).
                setContentTitle(title).
                setSmallIcon(R.drawable.flash_screen).
                setLargeIcon(bitmap);
        Notification n = builder.build();
        startForeground(SERVICE_ID, n);
    }

    void stopForeGroundNotification() {
        stopForeground(false);
    }


    Bitmap currentCover;

    class CurrentCoverTask extends AsyncTask<Void, Void, Bitmap> {
        private boolean running = false;

        public boolean isRunning() {
            return running;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            running = true;
            Bitmap bitmap = null;
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(currentPlaying.getAbsolutePath());
                byte[] data = mmr.getEmbeddedPicture();
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            currentCover = bitmap;
            running = false;
        }
    }

    private CurrentCoverTask currentCoverTask;

    private class InitialMusicTask extends AsyncTask<Void, Void, ArrayList<File>> {

        private boolean running = false;

        @Override
        protected ArrayList<File> doInBackground(Void... params) {
            running = true;
            return Music.getMusicQueue(MusicService.this);

        }

        @Override
        protected void onPostExecute(ArrayList<File> data) {
            mMusics = data;
            if (mMusics.size() == 0 && mMediaPlayer.isPlaying()) {
                mMediaPlayer.reset();
                paused = false;
                send(null, Music.A_ACTION_UPDATE_UI);
                send(null, Music.A_ACTION_UI_PLAY);
                processing = false;
                running = false;
                return;
            }
            if (mMediaPlayer.isPlaying() && !mMusics.contains(currentPlaying)) {
                playNext();
            }
            running = false;
        }

        public boolean isRunning() {
            return running;
        }
    }


    InitialMusicTask task;

    private void initialMusics() {
        if (task != null && task.isRunning()) {
            return;
        }
        task = new InitialMusicTask();
        task.execute();
    }

    private static final int SERVICE_ID = 345;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBr);
        mMediaPlayer.release();
        am.abandonAudioFocus(amListener);
        stopForeground(true);
        send(null, Music.A_ACTION_FINISH);
        super.onDestroy();
    }

    @Deprecated
    void play(@NonNull File file) throws Exception {
        if (file.equals(currentPlaying) && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            send(file, Music.A_ACTION_UPDATE_UI);
            send(null, Music.A_ACTION_UI_PAUSE);
            return;
        }
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(this, Uri.fromFile(file));
        mMediaPlayer.prepare();
        mMediaPlayer.start();
        if (mMediaPlayer.isPlaying()) {
            currentPlaying = file;
            send(currentPlaying, Music.A_ACTION_UPDATE_UI);
            send(null, Music.A_ACTION_UI_PAUSE);
        }
        runCurrentCoverTask();
    }

    //play from the start
    private void play() {
        try {
            if (currentPlaying == null) {
                Toast.makeText(this, "no music in the queue", Toast.LENGTH_SHORT).show();
                processing = false;
                return;
            }
            runCurrentCoverTask();
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(currentPlaying.getAbsolutePath());
            prepare();
            send(currentPlaying, Music.A_ACTION_UPDATE_UI);
            send(currentPlaying, Music.A_ACTION_UI_PAUSE);
        } catch (Exception e) {
            Toast.makeText(MusicService.this, "can't play this file!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            processing = false;
        }
    }

    void pause(boolean abandonFocus) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            paused = currentPlaying != null;
            if (abandonFocus) {
                am.abandonAudioFocus(amListener);
                stopForeGroundNotification();
            }
            send(null, Music.A_ACTION_UI_PLAY);
        }
        processing = false;
    }

    // must be paused
    private void resume() {
        if (paused) {
            requestAudioFocus();
            foreGroundNotification(currentPlaying.getName(),currentCover);
            mMediaPlayer.start();
            paused = false;
            send(currentPlaying, Music.A_ACTION_UI_PAUSE);
        }
        processing = false;
    }

    private void prepare() {
        if (prepareTask != null && prepareTask.isRunning()) {
            prepareTask.cancel(true);
        }
        prepareTask = new PrePareMediaTask();
        prepareTask.execute();
    }

    PrePareMediaTask prepareTask;

    private class PrePareMediaTask extends AsyncTask<Void, Void, Boolean> {
        private boolean running = false;

        public boolean isRunning() {
            return running;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            running = true;
            try {
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
                mMediaPlayer.reset();
                return true;
            } finally {
                processing = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean error) {
            if (error != null && error) {
                play();
            }
            running = false;
        }
    }

    private void send(File file, String action) {
        Intent i = new Intent(action);
        i.putExtra(Music.MUSIC_URI, file);
        sendBroadcast(i);
    }

    private void runCurrentCoverTask() {
        if (currentCoverTask != null && currentCoverTask.isRunning()) {
            return;
        }
        currentCoverTask = new CurrentCoverTask();
        currentCoverTask.execute();
    }

    private void playNext0() throws Exception {

        int position = mMusics.indexOf(currentPlaying);
        position = (position + 1) >= mMusics.size() ? 0 : position + 1;
        currentPlaying = mMusics.get(position);
        play();
    }

    public void playNext() {
        for (int i = 0, l = mMusics.size(); i < l; i++) {
            try {
                playNext0();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            return;
        }
        paused = false;
        currentPlaying = null;
        Log.e(TAG, "All files can't be played!");
        processing = false;
    }

    private void playPrevious0() throws Exception {
        int position = mMusics.indexOf(currentPlaying);
        position = (position - 1) < 0 ? mMusics.size() - 1 : position - 1;
        currentPlaying = mMusics.get(position);
        play();
    }

    public void playPrevious() {
        for (int i = 0, l = mMusics.size(); i < l; i++) {
            try {
                playPrevious0();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            return;
        }
        paused = false;
        currentPlaying = null;
        processing = false;
        Log.e(TAG, "All files can't be played!");
    }


    public void test() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5, null);
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
}
