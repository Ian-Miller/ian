package ian.a.music;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Ian on 8/22/2017.
 */

public class MusicData {
    public static class MusicDataContract implements BaseColumns {
        public static final String TABLE_NAME = "music";
        public static final String COLUMN_PATH = "music_path";

    }
    public static class OneMusicDataHelper extends SQLiteOpenHelper {
        private static final int VERSION = 2;
        private static final String DATABASE_NAME = "one music database";
        public OneMusicDataHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            final String CREATE_TABLE = "CREATE TABLE " + MusicDataContract.TABLE_NAME + " (" +
                    MusicDataContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MusicDataContract.COLUMN_PATH + " TEXT NOT NULL" +
                    ");";
            db.execSQL(CREATE_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            final String DROP_TABLE = "DROP TABLE IF EXISTS " + MusicDataContract.TABLE_NAME + ";";
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }

    public static class MusicQueueContract implements BaseColumns {
        public static final String TABLE_NAME = "music_play_queue";
        public static final String COLUMN_PATH = "music_path";

    }
    public static class ZeroMusicPlayQueueHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "one music play queue database";
        private static final int VERSION = 1;
        public ZeroMusicPlayQueueHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String CREATE_TABLE = "CREATE TABLE " + MusicQueueContract.TABLE_NAME + " (" +
                    MusicQueueContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                    MusicQueueContract.COLUMN_PATH + " TEXT NOT NULL" +
                    ");";

            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            final String DROP_TABLE = "DROP TABLE IF EXISTS " + MusicQueueContract.TABLE_NAME + ";";
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }
}
