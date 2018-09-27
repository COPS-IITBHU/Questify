package in.shriyansh.streamify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;
import in.shriyansh.streamify.utils.Utils;

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

                Cursor cursor = queryNotifications(null, null,null,
                        DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC",1);
                if (cursor.getCount() == 0) {
                    // Server changed. So I'm no longer sending id's
                    // Gotta Configure id's manually
                    notificationJson.put("id",cursor.getCount() + 1);

                    //insert and count to notify
                    ContentValues values = new ContentValues();
                    values.put(DbContract.Notifications.COLUMN_GLOBAL_ID,
                            notificationJson.getInt("id"));
                    values.put(DbContract.Notifications.COLUMN_TITLE,
                            notificationJson.getString("title"));
                    values.put(DbContract.Notifications.COLUMN_SUBTITLE,
                            notificationJson.getString("subtitle"));
                    values.put(DbContract.Notifications.COLUMN_DESCRIPTION,
                            notificationJson.getString("description"));
                    values.put(DbContract.Notifications.COLUMN_LINK,
                            notificationJson.getString("link"));
                    values.put(DbContract.Notifications.COLUMN_TYPE,
                            notificationJson.getInt("type"));
                    values.put(DbContract.Notifications.COLUMN_TIME,
                            Utils.convertStringTimeToTimestamp(notificationJson.getString(
                                    "time"),Constants.LARAVEL_TIME_FORMAT));
                    values.put(DbContract.Notifications.COLUMN_IMAGE,
                            notificationJson.getString("image"));
                    values.put(DbContract.Notifications.COLUMN_CREATED_AT,
                            Utils.convertStringTimeToTimestamp(notificationJson.getString(
                                    "created_at"),Constants.LARAVEL_TIME_FORMAT));
                    values.put(DbContract.Notifications.COLUMN_STREAM,
                            notificationJson.getString("stream"));
                    values.put(DbContract.Notifications.COLUMN_TAGS,
                            notificationJson.getString("tag"));
                    values.put(DbContract.Notifications.COLUMN_AUTHOR,
                            notificationJson.getString("author"));
                    values.put(DbContract.Notifications.COLUMN_CONTENTS,
                            notificationJson.getString("contents"));
                    values.put(DbContract.Notifications.COLUMN_LIKES,
                            notificationJson.getInt("likes"));
                    values.put(DbContract.Notifications.COLUMN_DISLIKES,
                            notificationJson.getInt("dislikes"));
                    values.put(DbContract.Notifications.COLUMN_USER_LIKE_TYPE,
                            notificationJson.getInt("user_like_type"));

                    insertContents(notificationJson.getJSONArray("contents"),
                            notificationJson.getString("stream"),
                            notificationJson.getInt("id"));

                    insertCount++;
                    db.insert(DbContract.Notifications.TABLE_NOTIFICATIONS,
                            null,values);
                }

                cursor.close();

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
                    // Server changed. So I'm no longer sending id's
                    // Gotta Configure id's manually
                    eventJson.put("id",cursor.getCount() + 1);

                    //insert and count to notify
                    ContentValues values = new ContentValues();
                    values.put(DbContract.Events.COLUMN_GLOBAL_ID,eventJson.getInt("id"));
                    values.put(DbContract.Events.COLUMN_TITLE,eventJson.getString("title"));
                    values.put(DbContract.Events.COLUMN_SUBTITLE,eventJson.getString(
                            "subtitle"));
                    values.put(DbContract.Events.COLUMN_DESCRIPTION,eventJson.getString(
                            "description"));
//                    values.put(DbContract.Events.COLUMN_TIME,
//                            Utils.convertStringTimeToTimestamp(eventJson.getString("time"),
//                                    Constants.LARAVEL_TIME_FORMAT));
                    values.put(DbContract.Events.COLUMN_IMAGE,eventJson.getString("imageURL"));
//                    values.put(DbContract.Events.COLUMN_CREATED_AT,
//                            Utils.convertStringTimeToTimestamp(eventJson.getString(
//                                    "created_at"),Constants.LARAVEL_TIME_FORMAT));
                    values.put(DbContract.Events.COLUMN_STREAM,eventJson.getString("stream"));

                    // Just fill the first tag for now
                    values.put(DbContract.Events.COLUMN_TAGS,eventJson.getJSONArray("tag").getString(0));

                    values.put(DbContract.Events.COLUMN_AUTHOR,eventJson.getString(
                            "author"));
                    values.put(DbContract.Events.COLUMN_VENUE,eventJson.getString(
                            "location"));
                    values.put(DbContract.Events.COLUMN_IS_USER_ATTENDING, eventJson.getInt(
                            "is_user_attending"));
                    values.put(DbContract.Events.COLUMN_ATTENDING_COUNT, eventJson.getInt(
                            "attending_count"));

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

    private void insertContents(JSONArray contentsJsonArray,String stream,long notificationId) {
        String streamName;
        for (int i = 0;i < contentsJsonArray.length();i++) {
            try {

                streamName = (new JSONObject(stream)).getString("title");

                JSONObject contentJson = contentsJsonArray.getJSONObject(i);
                    //insert and count to notify
                    ContentValues values = new ContentValues();
                    values.put(DbContract.Contents.COLUMN_GLOBAL_ID,contentJson.getInt("id"));
                    values.put(DbContract.Contents.COLUMN_PARENT_ID,notificationId);
                    values.put(DbContract.Contents.COLUMN_TITLE,contentJson.getString(
                            "title"));
                    values.put(DbContract.Contents.COLUMN_TEXT,contentJson.getString("text"));
                    values.put(DbContract.Contents.COLUMN_TYPE,contentJson.getInt("type"));
                    values.put(DbContract.Contents.COLUMN_IMAGE,contentJson.getString(
                            "image"));
                    values.put(DbContract.Contents.COLUMN_VIDEO_ID,contentJson.getString(
                            "video_id"));
                    values.put(DbContract.Contents.COLUMN_URL,contentJson.getString("url"));
                    values.put(DbContract.Contents.COLUMN_STREAM, streamName);
                    values.put(DbContract.Contents.COLUMN_CREATED_AT,
                            Utils.convertStringTimeToTimestamp(contentJson.getString(
                                    "created_at"),Constants.LARAVEL_TIME_FORMAT));
                    db.insert(DbContract.Contents.TABLE_CONTENTS,null,values);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        long currentTime = System.currentTimeMillis() / TimeUtils.MILLIS_IN_SECOND;
        long monthsAgo = currentTime - (long)(2 * TimeUtils.SECONDS_IN_MONTH);
        if (db.query(DbContract.Notifications.TABLE_NOTIFICATIONS,null,null,
                null,null,null,null).getCount() > 30) {
            db.delete(DbContract.Notifications.TABLE_NOTIFICATIONS,
                    DbContract.Notifications.COLUMN_CREATED_AT + " < ? ",
                    new String[]{monthsAgo + ""});
        }

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
     * Updates Notifications based on various params.
     *
     * @param values        Values to be updated
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     * @return              Id if updated
     */
    private int updateNotifications(ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(DbContract.Notifications.TABLE_NOTIFICATIONS, values, whereClause,
                whereArgs);
    }

    /**
     * Updates Events based on various params.
     *
     * @param values        Values to be updated
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     * @return              Id if updated
     */
    public int updateEvents(ContentValues values,String whereClause, String[] whereArgs) {
        return db.update(DbContract.Events.TABLE_EVENTS, values, whereClause, whereArgs);
    }

    /**
     * Updates Streams based on various params.
     *
     * @param values        Values to be updated
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     * @return              Id if updated
     */
    private int updateStreams(ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(DbContract.Streams.TABLE_STREAMS,values,whereClause,whereArgs);
    }

    /**
     * Updates Contents based on various params.
     *
     * @param values        Values to be updated
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     * @return              Id if updated
     */
    public int updateContents(ContentValues values,String whereClause, String[] whereArgs) {
        return db.update(DbContract.Contents.TABLE_CONTENTS,values,whereClause,whereArgs);
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
     * Deletes Contents based on various params.
     *
     * @param whereClause   Conditions to filter
     * @param whereArgs     Condition args
     */
    public void deleteContent(String whereClause,String[] whereArgs) {
        db.delete(DbContract.Contents.TABLE_CONTENTS,whereClause,whereArgs);
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
     * Queries the Oldest Stream Id in the database.
     *
     * @return  Id of the Oldest Stream
     */
    public long queryFirstNotificationId() {
        Cursor cursor = queryNotifications(null, null, null,
                DbContract.Notifications.COLUMN_GLOBAL_ID + " ASC ", 1);
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

    /**
     * Increses like count for Notification.
     *
     * @param newsGlobalId  Global id of the notification
     * @param likes         Count of likes to be increased
     * @return              Id of Notification
     */
    public int likeNotification(long newsGlobalId,int likes) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Notifications.COLUMN_USER_LIKE_TYPE,
                DbContract.Notifications.VALUE_LIKE_TYPE_LIKE);
        values.put(DbContract.Notifications.COLUMN_LIKES,likes + 1);
        return  updateNotifications(values, DbContract.Notifications.COLUMN_GLOBAL_ID
                + " = ? ", new String[]{newsGlobalId + ""});
    }

    /**
     * Resets like count for notification.
     *
     * @param newsGlobalId  Global id of the notification
     * @param wasLike       Is it already liked by user
     * @param likes         Count of likes
     * @param wasDislike    Is already disliked by user
     * @param dislikes      Count of dislikes
     * @return              Id of notification
     */
    public int resetLikeNotification(long newsGlobalId,boolean wasLike,int likes,boolean wasDislike,
                             int dislikes) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Notifications.COLUMN_USER_LIKE_TYPE,
                DbContract.Notifications.VALUE_LIKE_TYPE_NEUTRAL);
        if (wasLike) {
            values.put(DbContract.Notifications.COLUMN_LIKES,likes - 1);
        } else if (wasDislike) {
            values.put(DbContract.Notifications.COLUMN_DISLIKES,dislikes - 1);
        }
        return  updateNotifications(values, DbContract.Notifications.COLUMN_GLOBAL_ID
                + " = ? ", new String[]{newsGlobalId + ""});
    }

    /**
     * Decreases like count for Notification.
     *
     * @param newsGlobalId  Global id of the notification
     * @param dislikes      Count of dislikes to be increased
     * @return              Id of Notification
     */
    public int dislikeNotification(long newsGlobalId,int dislikes) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Notifications.COLUMN_USER_LIKE_TYPE,
                DbContract.Notifications.VALUE_LIKE_TYPE_DISLIKE);
        values.put(DbContract.Notifications.COLUMN_DISLIKES,dislikes + 1);
        return  updateNotifications(values, DbContract.Notifications.COLUMN_GLOBAL_ID
                + " = ? ", new String[]{newsGlobalId + ""});
    }

    /**
     * Subscribes the stream.
     *
     * @param streamGlobalId    Global id of the stream
     * @param subscribe         subscribe or unsubscribe
     * @return
     */
    public void subscribeStream(String streamGlobalId, boolean subscribe) {
        ContentValues values = new ContentValues();
        if (subscribe) {
            values.put(DbContract.Streams.COLUMN_IS_SUBSCRIBED,
                    DbContract.Streams.VALUE_SUBSCRIBED);
        } else {
            values.put(DbContract.Streams.COLUMN_IS_SUBSCRIBED,
                    DbContract.Streams.VALUE_NOT_SUBSCRIBED);
        }
        updateStreams(values, DbContract.Streams.COLUMN_GLOBAL_ID
                + " = ? ", new String[]{streamGlobalId});
    }

    /**
     * Update likes of Notification.
     *
     * @param newsGlobalId  Global id of notification
     * @param likes         Count of likes to be updated to
     * @param dislikes      Count of dislikes to be updated to
     */
    public void updateLikes(long newsGlobalId,int likes,int dislikes) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Notifications.COLUMN_LIKES,likes);
        values.put(DbContract.Notifications.COLUMN_DISLIKES,dislikes);
        updateNotifications(values, DbContract.Notifications.COLUMN_GLOBAL_ID
                + " = ? ",new String[]{newsGlobalId + ""});
    }
}
