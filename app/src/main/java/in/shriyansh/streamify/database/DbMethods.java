package in.shriyansh.streamify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Methods for accessing and modifying the database
 * Created by shriyansh on 9/10/15.
 */
public class DbMethods {
    public static final String TAG = DbMethods.class.getSimpleName();
    private final DbHelper dbHelper;
    private SQLiteDatabase db;

    public DbMethods(Context context) {
        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Inserts notification in database.
     *
     * @param notificationsJsonArray    Notification as JSON Array
     * @return                          Insert id
     */
    public long insertNotifications(JSONArray notificationsJsonArray) {
        long insertCount = 0;
        for (int i = 0;i < notificationsJsonArray.length();i++) {
            try {
                JSONObject notificationJson = notificationsJsonArray.getJSONObject(i);

                //insert and count to notify
                ContentValues values = new ContentValues();
                values.put(DbContract.Notifications.COLUMN_GLOBAL_ID,
                        notificationJson.getInt("id"));
                values.put(DbContract.Notifications.COLUMN_TITLE,
                        notificationJson.getString("title"));
                values.put(DbContract.Notifications.COLUMN_DESCRIPTION,
                        notificationJson.getString("description"));
                values.put(DbContract.Notifications.COLUMN_AUTHOR,
                        notificationJson.getString("authoremail"));

                insertCount++;
                db.insert(DbContract.Notifications.TABLE_NOTIFICATIONS,
                        null,values);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return insertCount;
    }

    /**
     * Inserts Events in database.
     *
     * @param eventsJsonArray   Events as JSONArray
     * @return                  Insert id
     */
    public long insertEvents(JSONArray eventsJsonArray) {
        long insertCount = 0;
        for (int i = 0;i < eventsJsonArray.length();i++) {
            try {
                JSONObject eventJson = eventsJsonArray.getJSONObject(i);

                Cursor cursor = queryNotifications(null, null,null,
                        DbContract.Events.COLUMN_GLOBAL_ID + " DESC",1);

                if (cursor.getCount() == 0) {
                    //insert and count to notify
                    ContentValues values = new ContentValues();
                    values.put(DbContract.Events.COLUMN_GLOBAL_ID,eventJson.getInt("id"));
                    values.put(DbContract.Events.COLUMN_TITLE,eventJson.getString("title"));
                    values.put(DbContract.Events.COLUMN_DESCRIPTION,eventJson.getString(
                            "description"));
                    values.put(DbContract.Events.COLUMN_IMAGE,eventJson.getString("imageurl"));

                    // Just 1st stream for now
                    values.put(DbContract.Events.COLUMN_STREAM,eventJson.getJSONArray("stream").getString(0));

                    // Just fill the first tag for now
                    values.put(DbContract.Events.COLUMN_TAGS,eventJson.getJSONArray("tag").getString(0));
                    values.put(DbContract.Events.COLUMN_VENUE,eventJson.getString("location"));

                    insertCount++;
                    db.insert(DbContract.Events.TABLE_EVENTS,null,values);
                }
                cursor.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return insertCount;
    }

    /**
     * Queries Notifications by various query parameters.
     *
     * @param columns       Columns to be queried
     * @param selection     Selection conditions
     * @param selectionArgs Selection condition args
     * @param orderBy       Order specification
     * @param limit         Count Limit of query
     * @return              cursor with result from database
     */
    public Cursor queryNotifications(String[] columns,String selection, String[] selectionArgs,
                                     String orderBy,int limit) {
        if (limit == 0) {
            return db.query(DbContract.Notifications.TABLE_NOTIFICATIONS,columns,selection,
                    selectionArgs,null,null,orderBy);
        } else {
            return db.query(DbContract.Notifications.TABLE_NOTIFICATIONS,columns,selection,
                    selectionArgs,null,null,orderBy,limit + "");
        }

    }

    /**
     * Queries content based on various params.
     *
     * @param columns       Columns to be queried
     * @param selection     Selection condition
     * @param selectionArgs Selection args
     * @param orderBy       Order specification
     * @param limit         Count limit on result
     * @return              Cursor of result
     */
    public Cursor queryContent(String[] columns,String selection, String[] selectionArgs,
                               String orderBy, int limit) {
        db = dbHelper.getWritableDatabase();
        if (limit == 0) {
            return db.query(DbContract.Contents.TABLE_CONTENTS,columns,selection,selectionArgs,
                    null,null,orderBy);
        } else {
            return db.query(DbContract.Contents.TABLE_CONTENTS,columns,selection,selectionArgs,
                    null,null,orderBy, limit + "");
        }

    }

    /**
     * Queries Events based on various params.
     *
     * @param columns       Columns to be queried
     * @param selection     Selection condition
     * @param selectionArgs Selection args
     * @param orderBy       Order specification
     * @param limit         Count limit on result
     * @return              Cursor of result
     */
    public Cursor queryEvents(String[] columns,String selection, String[] selectionArgs,
                              String orderBy,int limit) {
        db = dbHelper.getWritableDatabase();
        long currentTime = System.currentTimeMillis() / TimeUtils.MILLIS_IN_SECOND;
        long monthsAgo = currentTime - (long)(2 * TimeUtils.SECONDS_IN_MONTH);
        if (db.query(DbContract.Events.TABLE_EVENTS,null,null,null,
                null,null,null).getCount() > 30) {
            db.delete(DbContract.Events.TABLE_EVENTS,
                    DbContract.Streams.COLUMN_CREATED_AT + " < ? ",
                    new String[]{monthsAgo + ""});
        }
        if (limit == 0) {
            return db.query(DbContract.Events.TABLE_EVENTS,columns,selection,selectionArgs,
                    null,null,orderBy);
        } else {
            return db.query(DbContract.Events.TABLE_EVENTS,columns,selection,selectionArgs,
                    null,null,orderBy, limit + "");
        }

    }

    /**
     * Queries Streams based on various params.
     *
     * @param columns       Columns to be queried
     * @param selection     Selection condition
     * @param selectionArgs Selection args
     * @param orderBy       Order specification
     * @param limit         Count limit on result
     * @return              Cursor of result
     */
    public Cursor queryStreams(String[] columns,String selection, String[] selectionArgs,
                               String orderBy, int limit) {
        db = dbHelper.getWritableDatabase();
        if (limit == 0) {
            return db.query(DbContract.Streams.TABLE_STREAMS,columns,selection,selectionArgs,
                    null,null,orderBy);
        } else {
            return db.query(DbContract.Streams.TABLE_STREAMS,columns,selection,selectionArgs,
                    null,null,orderBy, limit + "");
        }
    }

    /**
     * Deletes Notifications based on various params.
     *
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     */
    public void deleteNotifications(String whereClause,String[] whereArgs) {
        db.delete(DbContract.Notifications.TABLE_NOTIFICATIONS,whereClause,whereArgs);
    }

    /**
     * Deletes Events based on various params.
     *
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     */
    public void deleteEvents(String whereClause,String[] whereArgs) {
        db.delete(DbContract.Events.TABLE_EVENTS,whereClause,whereArgs);
    }

    /**
     * Queries the latest Notification Id in the database.
     *
     * @return  Id of the latest Notification
     */
    public long queryLastNotificationId() {
        Cursor cursor = queryNotifications(null,
                DbContract.Notifications.COLUMN_GLOBAL_ID + " <> ? ",
                new String[]{Constants.INSTRUCTIONS_RECORD_ID + ""},
                DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC ", 1);
        long globalId = -1;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                globalId = cursor.getInt(cursor.getColumnIndex(
                        DbContract.Notifications.COLUMN_GLOBAL_ID));
            }
            cursor.close();
        }
        return globalId;
    }

    /**
     * Queries the latest Event Id in the database.
     *
     * @return  Id of the latest Event
     */
    public long queryLastEventId() {
        Cursor cursor = queryEvents(null, DbContract.Events.COLUMN_GLOBAL_ID
                + " <> ? ", new String[]{Constants.INSTRUCTIONS_RECORD_ID + ""},
                DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 1);
        long globalId = -1;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                globalId = cursor.getInt(cursor.getColumnIndex(DbContract.Events.COLUMN_GLOBAL_ID));
            }
            cursor.close();
        }
        return globalId;
    }
}
