package ian.a.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ian on 8/22/2017.
 */

public class Music {
    public static final String MUSIC_ACTION_PLAY = "music play";
    public static final String MUSIC_ACTION_PLAY_NEED_REFRESH = "music play need refresh";
    public static final String MUSIC_ACTION_COVER_SHOULD_UPDATE = "music action cover should update";
    public static final String MUSIC_ACTION_PLAY_NEXT = "a play next";
    public static final String MUSIC_ACTION_PLAY_PREVIOUS = "a play previous";
    public static final String MUSIC_ACTION_IF_PLAYING = "music if playing";

    /**
     * update big cover.
     */
    public static final String A_ACTION_UPDATE_UI = "a update ui";
    /**
     * update play button.
     */
    public static final String A_ACTION_UPDATE_STATE = "a update state";
    public static final String A_ACTION_FINISH = "a finish";
    public static final String A_ACTION_UI_PLAY = "a ui play";
    public static final String A_ACTION_UI_PAUSE = "a ui pause";

    public static final String MUSIC_REFRESH = "music refresh";

    public static final String MUSIC_URI = "music uri";

    public static ArrayList<File> getMusics(Context context){
        ArrayList<File> musics = new ArrayList<>();
        MusicData.OneMusicDataHelper helper = new MusicData.OneMusicDataHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(MusicData.MusicDataContract.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if(!cursor.moveToFirst()){
            return musics;
        }
        String path;
        do{
            path = cursor.getString(cursor.getColumnIndex(MusicData.MusicDataContract.COLUMN_PATH));
            musics.add(new File(path));
        }while(cursor.moveToNext());
        db.close();
        cursor.close();
        return musics;
    }

    public static ArrayList<File> getMusicQueue(Context context){
        ArrayList<File> queue = new ArrayList<>();

        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(MusicData.MusicQueueContract.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if(!cursor.moveToFirst()){
            return queue;
        }
        do {
            String path = cursor.getString(cursor.getColumnIndex(MusicData.MusicQueueContract.COLUMN_PATH));
            File file = new File(path);
            queue.add(file);
        } while (cursor.moveToNext());
        db.close();
        cursor.close();
        return queue;
    }

    public static void addToMusicQueue(Context context, File file){
        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String path = file.getAbsolutePath();
        ContentValues cv = new ContentValues();
        cv.put(MusicData.MusicQueueContract.COLUMN_PATH, path);
        db.insert(MusicData.MusicQueueContract.TABLE_NAME,
                null,
                cv);
        db.close();
    }

    public static void deleteFromQueue(Context context, File file){
        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        final String WHERE_CLAUSE = MusicData.MusicQueueContract.COLUMN_PATH + "=?";
        final String[] ARGS = {file.getAbsolutePath()};
        db.delete(MusicData.MusicQueueContract.TABLE_NAME,WHERE_CLAUSE,ARGS);
        db.close();
    }

    public static void updateQueue(Context context,ArrayList<File> files){
        if(files == null){
            return;
        }
        int size = files.size();
        if(size == 0){
            return;
        }
        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        final String tableName = MusicData.MusicQueueContract.TABLE_NAME;
        final String column = MusicData.MusicQueueContract.COLUMN_PATH;
        db.delete(tableName,null,null);
        File child;
        for (int i = 0; i < size; i++){
            child = files.get(i);
            ContentValues cv = new ContentValues();
            cv.put(column,child.getAbsolutePath());
            db.insert(tableName,column,cv);
        }
        db.close();
    }

    public static void switchInQueue(Context context, File moving, File f){
        final String tableName = MusicData.MusicQueueContract.TABLE_NAME;
        final String column = MusicData.MusicQueueContract.COLUMN_PATH;
        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = column + "=?";
        String[] whereArgs = {moving.getName()};
        db.delete(tableName,whereClause,whereArgs);
        ContentValues cv = new ContentValues();
        cv.put(column,f.getAbsolutePath());

        db.insert(tableName,column,cv);
        db.close();
    }
}
