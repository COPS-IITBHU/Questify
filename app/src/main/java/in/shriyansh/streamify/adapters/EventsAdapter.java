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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Adapter for events recyclerview
 * Created by shriyansh on 12/10/15.
 */
public class EventsAdapter extends CursorAdapter implements Urls {

    private final Context context;
    private Dialog authorDialog;
    private final DbMethods dbMethods;
    private HashMap<Long, Boolean> selection = new HashMap<>();

    /**
     * Event Adapter constructor.
     *
     * @param context   Context of parent activity
     * @param cursor    Database cursor
     */
    public EventsAdapter(Context context, Cursor cursor) {
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
     * Deletes selected list items.
     */
    public void deleteSelected() {

        Iterator it = selection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Boolean)pair.getValue()) {
                // System.out.println(pair.getKey() + " = " + pair.getValue());
                dbMethods.deleteEvents(DbContract.Events._ID + " = ? ",
                        new String[]{pair.getKey() + ""});
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        changeCursor(dbMethods.queryEvents(null, null, null,
                DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
        notifyDataSetChanged();
    }

    /**
     * Fires share intent with event(s) details.
     */
    public void forwardSelected() {
        Iterator it = selection.entrySet().iterator();
        StringBuilder msg = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Boolean)pair.getValue()) {

                Cursor cursor = dbMethods.queryEvents(null,
                        DbContract.Events._ID + " = ? ", new String[]{pair.getKey() + ""},
                        DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0);
                while (cursor.moveToNext()) {
                    String eventTitle = cursor.getString(cursor.getColumnIndex(
                            DbContract.Events.COLUMN_TITLE));
                    String eventDescription = cursor.getString(cursor.getColumnIndex(
                            DbContract.Events.COLUMN_DESCRIPTION));

                    String eventStreamTitle = "";
                    try {
                        JSONObject streamJson = new JSONObject(cursor.getString(
                                cursor.getColumnIndex(DbContract.Events.COLUMN_STREAM)));
                        eventStreamTitle = streamJson.getString("title");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String eventLocationName = "";
                    try {
                        JSONObject venueJson = new JSONObject(cursor.getString(
                                cursor.getColumnIndex(DbContract.Events.COLUMN_VENUE)));
                        eventLocationName = venueJson.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String eventAuthorName = "";
                    String eventAuthorEmail = "";
//                    try {
//                        JSONObject authorJson = new JSONObject(cursor.getString(
//                                cursor.getColumnIndex(DbContract.Events.COLUMN_AUTHOR)));
//                        eventAuthorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
//                        eventAuthorEmail = authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                    SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.DB_TIME_FORMAT,
                            Locale.ENGLISH);
                    sdf.setTimeZone(TimeZone.getTimeZone(TimeUtils.TIME_ZONE_INDIA));
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
        if (cursor.getInt(cursor.getColumnIndex(DbContract.Events.COLUMN_GLOBAL_ID))
                == Constants.INSTRUCTIONS_RECORD_ID) {
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
            v = inflater.inflate(R.layout.event_item, parent, false);
        }
        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        if (getItemViewType(cursor.getPosition()) == 1) {
            TextView title;
            TextView subtitle;
            TextView description;
            TextView authorName;
            TextView stream;
            TextView datetime;
            TextView creationTime;
            TextView venue;
            TextView tag1;
            ImageView authorImage;
            ImageView newsImage;

            Boolean result = selection.get(cursor.getLong(cursor.getColumnIndex(
                    DbContract.Events._ID)));
            result = result == null ? false : result;
            LinearLayout itemContainer = view.findViewById(R.id.item_container);
            if (result) {
                itemContainer.setBackgroundResource(R.drawable.rounded_edge_teal_light);
            } else {
                itemContainer.setBackgroundResource(R.drawable.rounded_edge_white);
            }

            title = view.findViewById(R.id.event_title);
            subtitle = view.findViewById(R.id.event_subtitle);
            description = view.findViewById(R.id.event_description);
            datetime = view.findViewById(R.id.event_date);
            creationTime = view.findViewById(R.id.event_creation_time);
            venue = view.findViewById(R.id.event_venue);
            stream = view.findViewById(R.id.event_stream);
            tag1 = view.findViewById(R.id.event_tag1);
            authorName = view.findViewById(R.id.event_author_name);
            newsImage = view.findViewById(R.id.event_image);
            authorImage = view.findViewById(R.id.event_author_image);

            //initUi(title, subtitle, description, datetime, creationTime, venue, stream, tag1,
            //        authorName, newsImage, authorImage, view);


            authorImage.setTag(cursor.getPosition());
            newsImage.setTag(cursor.getPosition());
            authorName.setTag(cursor.getPosition());

            fetchAndSetData(cursor, title, subtitle, description, authorName, stream, tag1,
                    datetime, creationTime, venue, newsImage, authorImage);

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

//        try {
//            JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
//                    DbContract.Events.COLUMN_AUTHOR)));
//            streamAuthorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
//            streamAuthorEmail = authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL);
//            streamAuthorContact = authorJson.getString(Constants.JSON_KEY_AUTHOR_CONTACT);
//            streamAuthorImage = authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL);
//        } catch (JSONException e) {
//            e.printStackTrace();
            streamAuthorName = "";
            streamAuthorEmail = "";
            streamAuthorContact = "";
            streamAuthorImage = "";
//        }

        name = authorDialog.findViewById(R.id.contact_name);
        email = authorDialog.findViewById(R.id.contact_email);
        contact = authorDialog.findViewById(R.id.contact_contact);
        image = authorDialog.findViewById(R.id.author_image);

        name.setText(streamAuthorName);
        email.setText(streamAuthorEmail);
        contact.setText(streamAuthorContact);
        setImageOnView(context, streamAuthorImage, image, R.drawable.ic_person_black_24dp);

        authorDialog.show();

//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
//                            DbContract.Events.COLUMN_AUTHOR)));
//                    fireImageActivityIntent(context,
//                            authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL),
//                            authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME),
//                            authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL),
//                            authorJson.getString(Constants.JSON_KEY_AUTHOR_CONTACT));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
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

// --Commented out by Inspection START (23/06/18, 1:38 PM):
//    /**
//     * @param view
//     */
//    private void initUi(View view) {
//        TextView title = (TextView) view.findViewById(R.id.event_title);
//        TextView subtitle = (TextView) view.findViewById(R.id.event_subtitle);
//        TextView description = (TextView) view.findViewById(R.id.event_description);
//        TextView datetime = (TextView) view.findViewById(R.id.event_date);
//        TextView creationTime = (TextView) view.findViewById(R.id.event_creation_time);
//        TextView venue = (TextView) view.findViewById(R.id.event_venue);
//        TextView stream = (TextView) view.findViewById(R.id.event_stream);
//        TextView tag1 = (TextView) view.findViewById(R.id.event_tag1);
//        TextView authorName = (TextView) view.findViewById(R.id.event_author_name);
//
//        ImageView newsImage = (ImageView) view.findViewById(R.id.event_image);
//        ImageView authorImage = (ImageView) view.findViewById(R.id.event_author_image);
//    }
// --Commented out by Inspection STOP (23/06/18, 1:38 PM)

    /**
     * Fetches and sets instruction data.
     *
     * @param cursor        Database cursor
     * @param titleTv       Title text view
     * @param descriptionTv Description text view
     * @param gotIt         Ddone text view
     */
    private void fetchAndSetInstructionData(final Cursor cursor, final TextView titleTv,
                                            final TextView descriptionTv, final TextView gotIt) {
        String title;
        String description;

        title = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_TITLE));
        description = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_DESCRIPTION));
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
                dbMethods.deleteEvents(DbContract.Events.COLUMN_GLOBAL_ID + " = ? ",
                        new String[]{Constants.INSTRUCTIONS_RECORD_ID + ""});
                changeCursor(dbMethods.queryEvents(null, null, null,
                        DbContract.Events.COLUMN_GLOBAL_ID + " DESC ", 0));
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Fetches List item data and sets it to corresponding views.
     *
     * @param cursor            Database cursor
     * @param title             Title text view
     * @param subtitle          Subtitle text view
     * @param description       Description text view
     * @param authorName        Author name text view
     * @param stream            Stream text view
     * @param tag1              Tag text view
     * @param dateTime          Date time text view
     * @param creationTime      Creation time text view
     * @param venue             Venue text view
     * @param eventImageView    Event Image view
     * @param authorImage       Author Image view
     */
    private void fetchAndSetData(Cursor cursor, final TextView title, final TextView subtitle,
                                 final TextView description, final TextView authorName,
                                 final TextView stream, final TextView tag1,
                                 final TextView dateTime, final TextView creationTime,
                                 final TextView venue, final ImageView eventImageView,
                                 final ImageView authorImage) {
        String eventTitle;
        String eventSubtitle;
        String eventDescription;
        String eventImage;
        String eventAuthorName;
        String eventAuthorImage;
        String eventStreamTitle;
        String eventTag;
        String eventLocationName;
        int eventDate;
        int eventCreation;

        eventTitle = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_TITLE));
        eventDescription = cursor.getString(cursor.getColumnIndex(
                DbContract.Events.COLUMN_DESCRIPTION));
        eventImage = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_IMAGE));

//        try {
//            JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
//                    DbContract.Events.COLUMN_AUTHOR)));
//            eventAuthorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
//            eventAuthorImage = authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL);
//        } catch (JSONException e) {
//            e.printStackTrace();
            eventAuthorName = "";
            eventAuthorImage = "";
//        }

        try {
            JSONObject streamJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                    DbContract.Events.COLUMN_STREAM)));
            eventStreamTitle = streamJson.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
            eventStreamTitle = "";
        }

        try {
            JSONObject tagJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                    DbContract.Events.COLUMN_TAGS)));
            eventTag = tagJson.getString("name") + " ";

        } catch (JSONException e) {
            e.printStackTrace();
            eventTag = "";
        }

        try {
            JSONObject venueJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                    DbContract.Events.COLUMN_VENUE)));
            eventLocationName = venueJson.getString(Constants.JSON_KEY_LOCATION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            eventLocationName = "";
        }

        plugDataToView(title, subtitle, description, authorName, stream, tag1, dateTime,
                creationTime, venue, eventImageView, authorImage, eventTitle, "",
                eventDescription, eventAuthorName, eventStreamTitle, eventTag, Integer.getInteger(System.currentTimeMillis()/1000 + ""),
                Integer.getInteger(System.currentTimeMillis()/1000 + ""), eventLocationName, eventAuthorImage, eventImage);
    }

    /**
     * Plugs Data to list item view.
     *
     * @param title             Event Title text view
     * @param subtitle          Event Subtitle text view
     * @param description       Event Description text view
     * @param authorName        Event Author Name text view
     * @param stream            Event Stream text view
     * @param tag1              Event Tag text view
     * @param dateTime          Event Date time text view
     * @param creationTime      Event Creation time text view
     * @param venue             Event View text view
     * @param eventImageView    Event image image view
     * @param authorImage       Event author image view
     * @param eventTitle        Event title
     * @param eventSubtitle     Event subtitle
     * @param eventDescription  Event description
     * @param eventAuthorName   Event author name
     * @param eventStreamTitle  Event stream title
     * @param eventTag          Event tags
     * @param eventDate         Event date
     * @param creation          Event creation time
     * @param eventLocationName Event location name
     * @param eventAuthorImage  Event author image url
     * @param eventImage        Event image url
     */
    private void plugDataToView(final TextView title, final TextView subtitle,
                                final TextView description, final TextView authorName,
                                final TextView stream, final TextView tag1,
                                final TextView dateTime,
                                final TextView creationTime, final TextView venue,
                                final ImageView eventImageView, final ImageView authorImage,
                                final String eventTitle, final String eventSubtitle,
                                final String eventDescription, final String eventAuthorName,
                                final String eventStreamTitle, final String eventTag,
                                final int eventDate, final int creation,
                                final String eventLocationName, final String eventAuthorImage,
                                final String eventImage) {
        title.setText(eventTitle);
        subtitle.setText(eventSubtitle);
        description.setText(eventDescription);
        authorName.setText(eventAuthorName);
        stream.setText(eventStreamTitle);
        tag1.setText(eventTag);

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.DB_TIME_FORMAT, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone(TimeUtils.TIME_ZONE_INDIA));
        String date = sdf.format(Utils.settleTimeZoneDifference(eventDate)
                * TimeUtils.MILLIS_IN_SECOND);

        dateTime.setText(date.replace("am","AM").replace("pm",
                "PM"));
        creationTime.setText(TimeUtils.ago(Utils.settleTimeZoneDifference(creation)));
        venue.setText(eventLocationName);
        setImageOnView(context, eventAuthorImage, authorImage, R.drawable.ic_person_black_24dp);

        if (eventImage.contentEquals("")) {
            eventImageView.setVisibility(View.GONE);
        } else {
            eventImageView.setVisibility(View.VISIBLE);
            setImageOnView(context, eventImage, eventImageView, R.drawable.placeholder);
        }
    }
}
