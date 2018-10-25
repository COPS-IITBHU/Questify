package in.shriyansh.streamify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;

/**
 * Created by shriyansh on 7/10/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    private final Context context;

    public DbHelper(Context context) {
        super(context, DbContract.DB_NAME, null, DbContract.DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL(DbContract.Streams.CREATE_TABLE);
         insertInstructionStream(db);
         db.execSQL(DbContract.Notifications.CREATE_TABLE);
         insertInstructionNews(db);
         db.execSQL(DbContract.Events.CREATE_TABLE);
         insertInstructionEvent(db);
         db.execSQL(DbContract.Contents.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DbContract.Streams.DROP_TABLE);
        db.execSQL(DbContract.Notifications.DROP_TABLE);
        db.execSQL(DbContract.Events.DROP_TABLE);
        db.execSQL(DbContract.Contents.DROP_TABLE);
        onCreate(db);
    }

    private void insertInstructionNews(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Notifications.COLUMN_GLOBAL_ID, Constants.INSTRUCTIONS_RECORD_ID);
        values.put(DbContract.Notifications.COLUMN_TITLE,
                context.getResources().getString(R.string.instruction_news_title));
        values.put(DbContract.Notifications.COLUMN_DESCRIPTION,
                context.getResources().getString(R.string.notification_instruction));
        values.put(DbContract.Notifications.COLUMN_AUTHOR,"");
        db.insert(DbContract.Notifications.TABLE_NOTIFICATIONS,null,values);
    }

    private void insertInstructionEvent(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Events.COLUMN_GLOBAL_ID, Constants.INSTRUCTIONS_RECORD_ID);
        values.put(DbContract.Events.COLUMN_TITLE,
                context.getResources().getString(R.string.instruction_events_title));
        values.put(DbContract.Events.COLUMN_DESCRIPTION,
                context.getResources().getString(R.string.events_instruction));
        values.put(DbContract.Events.COLUMN_IMAGE,"");
        values.put(DbContract.Events.COLUMN_STREAM,"");
        values.put(DbContract.Events.COLUMN_TAGS,"");
        db.insert(DbContract.Events.TABLE_EVENTS,null,values);
    }

    private void insertInstructionStream(SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        values.put(DbContract.Streams.COLUMN_GLOBAL_ID, Constants.INSTRUCTIONS_RECORD_ID);
        values.put(DbContract.Streams.COLUMN_TITLE,
                context.getResources().getString(R.string.instruction_stream_title));
        values.put(DbContract.Streams.COLUMN_SUBTITLE,
                context.getResources().getString(R.string.instruction_stream_subtitle));
        values.put(DbContract.Streams.COLUMN_DESCRIPTION,
                context.getResources().getString(R.string.stream_instruction));
        values.put(DbContract.Streams.COLUMN_IMAGE,"");
        values.put(DbContract.Streams.COLUMN_CREATED_AT,
                System.currentTimeMillis() / TimeUtils.MILLIS_IN_SECOND);
        values.put(DbContract.Streams.COLUMN_PARENT_BODIES,"");
        values.put(DbContract.Streams.COLUMN_POSITION_HOLDERS,"");
        values.put(DbContract.Streams.COLUMN_AUTHOR,"");
        values.put(DbContract.Streams.COLUMN_IS_SUBSCRIBED,
                DbContract.Streams.VALUE_NOT_SUBSCRIBED);
        db.insert(DbContract.Streams.TABLE_STREAMS,null,values);
    }
}
