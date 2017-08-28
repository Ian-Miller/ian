package ian.a.zero;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import ian.a.music.MusicData;

/**
 * Created by Ian on 8/22/2017.
 */

public class Zero {

    /**
     * do in work thread.
     * @param context
     * @return
     */
    public static ArrayList<File> queryMusicPlayQueue(Context context){
        ArrayList<File> musics = new ArrayList<>();
        MusicData.ZeroMusicPlayQueueHelper helper = new MusicData.ZeroMusicPlayQueueHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(MusicData.MusicQueueContract.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        if(!cursor.moveToFirst()){
            return musics;
        }
        do{
            String fileName = cursor.getString(cursor.getColumnIndex(MusicData.MusicQueueContract.COLUMN_PATH));
            if(fileName == null){
                continue;
            }
            File file = new File(fileName);
            musics.add(file);
        } while (!cursor.moveToNext());
        cursor.close();
        return musics;
    }
}
