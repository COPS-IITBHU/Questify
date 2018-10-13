package in.shriyansh.streamify.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.activities.ImageActivity;
import in.shriyansh.streamify.database.DbContract;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;
import in.shriyansh.streamify.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shriyansh on 12/10/15.
 */
public class NotificationsAdapter extends CursorAdapter implements Urls {
    private final Context context;
    private Dialog authorDialog;
    private final DbMethods dbMethods;
    private HashMap<Long, Boolean> selection = new HashMap<>();

    /**
     * Notification adapter constructor.
     *
     * @param context   Activity context
     * @param cursor    Database cursor
     */
    public NotificationsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        dbMethods = new DbMethods(context);
        initAuthorDialog();
    }

    private void initAuthorDialog() {
        authorDialog = new Dialog(context);
        authorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setting custom layout to dialog
        authorDialog.setContentView(R.layout.contact_thumbnail);
        authorDialog.setCancelable(true);
    }

    public void setNewSelection(long id, boolean value) {
        selection.put(id, value);
        notifyDataSetChanged();
    }

    public void removeSelection(long id) {
        selection.remove(id);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selection = new HashMap<>();
        notifyDataSetChanged();
    }

    /**
     * Deletes selected items.
     */
    public void deleteSelected() {
        Iterator it = selection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Boolean)pair.getValue()) {
                // System.out.println(pair.getKey() + " = " + pair.getValue());
                dbMethods.deleteNotifications(DbContract.Notifications._ID + " = ? ",
                        new String[]{pair.getKey() + ""});

            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        changeCursor(dbMethods.queryNotifications(null, null, null,
                DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC ", 0));
        notifyDataSetChanged();
    }

    /**
     * Forwards selected items.
     */
    public void forwardSelected() {
        Iterator it = selection.entrySet().iterator();
        StringBuilder msg = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Boolean)pair.getValue()) {

                Cursor cursor = dbMethods.queryNotifications(null,
                        DbContract.Notifications._ID + " = ? ",
                        new String[]{pair.getKey() + ""},
                        DbContract.Notifications.COLUMN_GLOBAL_ID + " DESC ", 0);
                while (cursor.moveToNext()) {
                    String eventTitle = cursor.getString(cursor.getColumnIndex(
                            DbContract.Notifications.COLUMN_TITLE));
                    String eventDescription = cursor.getString(cursor.getColumnIndex(
                            DbContract.Notifications.COLUMN_DESCRIPTION));

                    String notificationsStreamTitle = "";
                    String notificationAuthorName = "";
                    String notificationAuthorEmail = "";
                    try {
                        JSONObject authorJson = new JSONObject(cursor.getString(
                                cursor.getColumnIndex(DbContract.Notifications.COLUMN_AUTHOR)));
                        notificationAuthorName = authorJson.getString(
                                Constants.JSON_KEY_AUTHOR_NAME);
                        notificationAuthorEmail = authorJson.getString(
                                Constants.JSON_KEY_AUTHOR_EMAIL);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, "Forward via"));

    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        if (cursor.getInt(cursor.getColumnIndex(
                DbContract.Notifications.COLUMN_GLOBAL_ID)) == Constants.INSTRUCTIONS_RECORD_ID) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (getItemViewType(cursor.getPosition()) == 0) {
            v = inflater.inflate(R.layout.instruction_item, parent, false);
        } else {
            v = inflater.inflate(R.layout.news_item, parent, false);
        }

        return v;
    }

    @Override
    public void bindView(View view,final Context context, final Cursor cursor) {

        if (getItemViewType(cursor.getPosition()) == 1) {
            TextView title;
            TextView subtitle;
            TextView description;
            TextView authorName;
            TextView stream;
            TextView tag1;
            TextView ago;
            TextView link;
            ImageView authorImage;

            Boolean result = selection.get(cursor.getLong(cursor.getColumnIndex(
                    DbContract.Notifications._ID)));
            result = result == null ? false : result;
            LinearLayout itemContainer = view.findViewById(R.id.item_container);
            if (result) {
                itemContainer.setBackgroundResource(R.drawable.rounded_edge_teal_light);
            } else {
                itemContainer.setBackgroundResource(R.drawable.rounded_edge_white);
            }

            title = view.findViewById(R.id.news_title);
            subtitle = view.findViewById(R.id.news_subtitle);
            description = view.findViewById(R.id.news_description);
            link = view.findViewById(R.id.news_link);
            stream = view.findViewById(R.id.news_stream);
            tag1 = view.findViewById(R.id.news_tag_1);
            ago = view.findViewById(R.id.news_ago);
            authorName = view.findViewById(R.id.news_author_name);
            authorImage = view.findViewById(R.id.news_author_image);

            fetchAndSetData(cursor, title, subtitle, description, authorName, stream, tag1, ago,
                    link, authorImage);

            authorName.setTag(cursor.getPosition());
            authorImage.setTag(cursor.getPosition());


            final int imageViewsCount = 4;
            ImageView[] contentImages = new ImageView[imageViewsCount];
            contentImages[0] = view.findViewById(R.id.news_image_1);
            contentImages[1] = view.findViewById(R.id.news_image_2);
            contentImages[2] = view.findViewById(R.id.news_image_3);
            contentImages[3] = view.findViewById(R.id.news_image_4);
            ImageView contentVideo = view.findViewById(R.id.news_video);
            RelativeLayout videoBox = view.findViewById(R.id.video_box);
            LinearLayout moreImageBox
                    = view.findViewById(R.id.more_count_container);
            TextView moreTextView = view.findViewById(R.id.more_count);

            moreImageBox.setVisibility(View.GONE);
            moreTextView.setVisibility(View.GONE);
            contentVideo.setVisibility(View.GONE);
            videoBox.setVisibility(View.GONE);

            for (int i = 0;i < imageViewsCount;i++) {
                contentImages[i].setVisibility(View.GONE);
            }

            authorImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAuthorDialog(view, cursor);
                }
            });

            authorName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAuthorDialog(view, cursor);
                }
            });
        } else {
            TextView gotIt;
            TextView titleTv;
            TextView descriptionTv;

            gotIt = view.findViewById(R.id.instruction_done);
            titleTv = view.findViewById(R.id.instruction_title);
            descriptionTv = view.findViewById(R.id.instruction_description);

            fetchAndSetInstructionData(cursor, titleTv, descriptionTv, gotIt);
        }
    }

    /**
     * Fetches and sets instruction data.
     *
     * @param cursor        Database cursor
     * @param titleTv       Title text view
     * @param descriptionTv Description text view
     * @param gotIt         Done text view
     */
    private void fetchAndSetInstructionData(final Cursor cursor, final TextView titleTv,
                                            final TextView descriptionTv, final TextView gotIt) {
        String title;
        String description;

        title = cursor.getString(cursor.getColumnIndex(DbContract.Notifications.COLUMN_TITLE));
        description = cursor.getString(cursor.getColumnIndex(
                DbContract.Notifications.COLUMN_DESCRIPTION));

        plugDataToInstructionsView(titleTv, descriptionTv, gotIt, title, description);
    }

    /**
     * Plugs data into instruction view.
     *
     * @param titleTv       Title text view
     * @param descriptionTv Description text view
     * @param gotIt         Done text view
     * @param title         Title
     * @param description   Description
     */
    private void plugDataToInstructionsView(final TextView titleTv, final TextView descriptionTv,
                                            final TextView gotIt, final String title,
                                            final String description) {
        titleTv.setText(title);
        descriptionTv.setText(description);

        gotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbMethods.deleteNotifications(DbContract.Notifications.COLUMN_GLOBAL_ID
                        + " = ? ",new String[]{Constants.INSTRUCTIONS_RECORD_ID + ""});
                changeCursor(dbMethods.queryNotifications(null,null,
                        null,DbContract.Notifications.COLUMN_GLOBAL_ID
                                + " DESC ",0));
                notifyDataSetChanged();
            }
        });
    }


    private void fetchAndSetData(final Cursor cursor, final TextView title, final TextView subtitle,
                                 final TextView description, final TextView authorName,
                                 final TextView stream, final TextView tag1, final TextView ago,
                                 final TextView link, final ImageView authorImage) {
        String notificationTitle;
        String notificationDescription;
        String notificationAuthorName;
        String notificationAuthorImage;

        notificationTitle = cursor.getString(cursor.getColumnIndex(
                DbContract.Notifications.COLUMN_TITLE));
        notificationDescription = cursor.getString(cursor.getColumnIndex(
                DbContract.Notifications.COLUMN_DESCRIPTION));

//        try {
//            JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
//                    DbContract.Notifications.COLUMN_AUTHOR)));
//            notificationAuthorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
//            notificationAuthorImage = authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL);
//        } catch (JSONException e) {
//            e.printStackTrace();
            notificationAuthorName = "";
            notificationAuthorImage = "";
//        }

        plugDataToView(title, subtitle, description, authorName, stream, tag1, ago, link,
                authorImage, notificationTitle, "", notificationDescription,
                notificationAuthorName, "", "", "",
                notificationAuthorImage);
    }

    private void plugDataToView(final TextView title, final TextView subtitle,
                                final TextView description, final TextView authorName,
                                final TextView stream, final TextView tag1, final TextView ago,
                                final TextView link, final ImageView authorImage,
                                final String notificationTitle, final String notificationSubtitle,
                                final String notificationDescription,
                                final String notificationAuthorName,
                                final String notificationsStreamTitle, final String notificationTag,
                                final String notificationLink, final String notificationAuthorImage) {
        title.setText(notificationTitle);
        subtitle.setText(notificationSubtitle);
        description.setText(notificationDescription);
        authorName.setText(notificationAuthorName);
        stream.setText(notificationsStreamTitle);
        tag1.setText(notificationTag);
        link.setText(notificationLink);
        setImageOnView(context, notificationAuthorImage, authorImage,
                R.drawable.ic_person_black_24dp);

        if (notificationLink.contentEquals("")) {
            link.setVisibility(View.GONE);
        } else {
            link.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows author dialog.
     *
     * @param view List item view
     * @param cursor Database cursor
     */
    private void showAuthorDialog(View view, final Cursor cursor) {
        final TextView name;
        final TextView email;
        final TextView contact;
        final ImageView image;
        String streamAuthorName;
        String streamAuthorEmail;
        String streamAuthorContact;
        String streamAuthorImage;

        int position = (Integer) view.getTag();
        cursor.moveToPosition(position);

        try {
            JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                    DbContract.Notifications.COLUMN_AUTHOR)));
            streamAuthorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
            streamAuthorEmail = authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL);
            streamAuthorContact = authorJson.getString(Constants.JSON_KEY_AUTHOR_CONTACT);
            streamAuthorImage = authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL);
        } catch (JSONException e) {
            e.printStackTrace();
            streamAuthorName = "";
            streamAuthorEmail = "";
            streamAuthorContact = "";
            streamAuthorImage = "";
        }

        name = authorDialog.findViewById(R.id.contact_name);
        email = authorDialog.findViewById(R.id.contact_email);
        contact = authorDialog.findViewById(R.id.contact_contact);
        image = authorDialog.findViewById(R.id.author_image);

        name.setText(streamAuthorName);
        email.setText(streamAuthorEmail);
        contact.setText(streamAuthorContact);
        setImageOnView(context, streamAuthorImage, image, R.drawable.ic_person_black_24dp);

        authorDialog.show();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                            DbContract.Notifications.COLUMN_AUTHOR)));
                    fireImageActivityIntent(context,
                            authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL),
                            authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME),
                            authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL),
                            authorJson.getString(Constants.JSON_KEY_AUTHOR_CONTACT));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Sets image on image view.
     *
     * @param context               Activity context
     * @param imageUrl              Image URL
     * @param imageView             Image view
     * @param placeholderResourceId Placeholder resource id
     */
    private void setImageOnView(final Context context, final String imageUrl,
                                final ImageView imageView, final int placeholderResourceId) {
        Picasso.with(context)
                .load(Uri.parse(Utils.getUsableDropboxUrl(imageUrl)))
                .placeholder(placeholderResourceId)
                .error(placeholderResourceId)
                .into(imageView);
    }

    /**
     *  Fires intent for image immersive view activity.
     *
     * @param context           Activity context
     * @param imageUrl          Image URL
     * @param imageTitle        Image Title
     * @param imageSubtitle     Image Subtitle
     * @param imageDescription  Image Description
     */
    private void fireImageActivityIntent(final Context context, final String imageUrl,
                                         final String imageTitle,
                                         final String imageSubtitle,
                                         final String imageDescription) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_URL, Utils.getUsableDropboxUrl(imageUrl));
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_TITLE, imageTitle);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_SUBTITLE, imageSubtitle);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_DESCRIPTION, imageDescription);
        context.startActivity(intent);
    }
}
