package in.shriyansh.streamify.activities;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.database.DbContract;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;
import in.shriyansh.streamify.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class EventDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, Urls {

    private FloatingActionButton fab;
    private CollapsingToolbarLayout toolBarLayout;
    private Toolbar toolbar;

    private TextView subtitleSteamTv;
    private TextView descriptionTv;
    private TextView authorNameTv;
    private TextView authorPostTv;
    private TextView datetimeTv;
    private TextView agoTv;
    private TextView locationNameTv;
    private TextView locationAddressTv;
    private TextView locationDescriptionTv;
    private ImageView authorImage;
    private ImageView bgHeader;
    private ImageView directionsImageView;

    private MapFragment mapFragment;
    private DbMethods dbMethods;

    private double lat = 0;
    private double lng = 0;
    private final double zoom = 16;
    private String locationName = "";
    private String locationAddress = "";
    private String locationDescription = "";
    public static final String INTENT_EXTRA_KEY_EVENT_ID = "event_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        initUi();
        initToolbar();

        dbMethods = new DbMethods(this);

        initMapFragment();

        Intent intent = getIntent();
        final long eventId = intent.getLongExtra(INTENT_EXTRA_KEY_EVENT_ID, 0);
        fetchAndSetDataOnViews(eventId);

        mapFragment.getMapAsync(this);
    }

    private void fetchAndSetDataOnViews(final long eventId) {
        final Cursor cursor = dbMethods.queryEvents(null, DbContract.Events._ID
                + " = ? ", new String[]{eventId + ""}, null, 0);
        while (cursor.moveToNext()) {
            String description;
            String eventImageUrl;
            String title;
            String subtitle;
            String stream;
            String authorName;
            String authorEmail;
            String authorImageUrl;
            String authorContact;

            title = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_TITLE));
            description = cursor.getString(cursor.getColumnIndex(
                    DbContract.Events.COLUMN_DESCRIPTION));
            eventImageUrl = cursor.getString(cursor.getColumnIndex(DbContract.Events.COLUMN_IMAGE));

            try {
                JSONObject streamJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                        DbContract.Events.COLUMN_STREAM)));
                stream = streamJson.getString(Constants.JSON_KEY_STREAM_TITLE);
            } catch (JSONException e) {
                e.printStackTrace();
                stream = "";
            }

            try {
                JSONObject venueJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
                        DbContract.Events.COLUMN_VENUE)));
                locationName = venueJson.getString(Constants.JSON_KEY_LOCATION_NAME);
                locationAddress = venueJson.getString(Constants.JSON_KEY_LOCATION_ADDRESS);
                locationDescription = venueJson.getString(Constants.JSON_KEY_LOCATION_DESCRIPTION);
                lat = venueJson.getDouble(Constants.JSON_KEY_LOCATION_LATITUDE);
                lng = venueJson.getDouble(Constants.JSON_KEY_LOCATION_LONGITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            try {
//                JSONObject authorJson = new JSONObject(cursor.getString(cursor.getColumnIndex(
//                        DbContract.Events.COLUMN_AUTHOR)));
//                authorName = authorJson.getString(Constants.JSON_KEY_AUTHOR_NAME);
//                authorEmail = authorJson.getString(Constants.JSON_KEY_AUTHOR_EMAIL);
//                authorImageUrl = authorJson.getString(Constants.JSON_KEY_AUTHOR_IMAGE_URL);
//                authorContact = authorJson.getString(Constants.JSON_KEY_AUTHOR_CONTACT);
//            } catch (JSONException e) {
//                e.printStackTrace();
                authorName = "";
                authorEmail = "";
                authorImageUrl = "";
                authorContact = "";
//            }

            plugDataOnViews(title, "", stream, description, eventImageUrl, Integer.getInteger(System.currentTimeMillis()/1000+""),
                    authorName, authorEmail, authorImageUrl, authorContact, lat, lng, locationName,
                    locationAddress, locationDescription);
        }

    }

    private void plugDataOnViews(final String title, final String subtitle, final String stream,
                                 final String description, final String eventImageUrl,
                                 final int dateTime, final String authorName,
                                 final String authorEmail, final String authorImageUrl,
                                 final String authorContact, final double lat, final double lng,
                                 final String locationName, final String locationAddress,
                                 final String locationDescription) {
        setEventHeaderView(title, subtitle, stream, description, eventImageUrl);
        setEventTimeView(dateTime, title, subtitle, stream, locationName);
        setLocationView(locationName, locationAddress, locationDescription, lat, lng);
        setAuthorView(authorName, authorEmail, authorContact, authorImageUrl);
    }

    private void initUi() {
        bgHeader = findViewById(R.id.bgheader);
        subtitleSteamTv = findViewById(R.id.subtitle_stream);
        descriptionTv = findViewById(R.id.description);
        authorImage = findViewById(R.id.event_author_image);
        authorNameTv = findViewById(R.id.author_name);
        authorPostTv = findViewById(R.id.author_post);
        datetimeTv = findViewById(R.id.datetime);
        agoTv = findViewById(R.id.ago);
        locationNameTv = findViewById(R.id.location_name);
        locationAddressTv = findViewById(R.id.location_address);
        locationDescriptionTv = findViewById(R.id.location_description);
        directionsImageView = findViewById(R.id.directions_image);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        toolBarLayout = findViewById(R.id.toolbar_layout);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBarLayout.setTitle(getTitle());
    }

    private void initMapFragment() {
        mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();
    }

    private void setEventTimeView(final int dateTime, final String title, final String subtitle,
                                  final String stream, final String locationName) {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.DB_TIME_FORMAT, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone(TimeUtils.TIME_ZONE_INDIA));
        String date = sdf.format(
                Utils.settleTimeZoneDifference(dateTime) * TimeUtils.MILLIS_IN_SECOND);
        datetimeTv.setText(date.replace("am", "AM")
                .replace("pm", "PM"));
        agoTv.setText(TimeUtils.ago(dateTime));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireCalenderIntent(dateTime, title, subtitle, stream, locationName);
            }
        });
    }

    private void setEventHeaderView(final String title, final String subtitle, final String stream,
                                    final String description, final String eventImageUrl) {
        toolBarLayout.setTitle(title);
        toolBarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        subtitleSteamTv.setText(subtitle + " by " + stream);
        descriptionTv.setText(description);
        bgHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireImageActivityIntent(eventImageUrl, title, subtitle, description);
            }
        });

        setImageOnView(eventImageUrl, bgHeader, R.drawable.placeholder);
    }

    private void setAuthorView(final String authorName, final String authorEmail,
                               final String authorContact, final String authorImageUrl) {
        authorNameTv.setText(authorName);
        authorPostTv.setText(authorEmail);
        setImageOnView(authorImageUrl, authorImage, R.drawable.ic_person_black_24dp);

        authorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAuthorDialog(authorName, authorEmail, authorContact, authorImageUrl);
            }
        });
        authorNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAuthorDialog(authorName, authorEmail, authorContact, authorImageUrl);
            }
        });
    }

    private void setLocationView(final String locationName, final String locationAddress,
                                 final String locationDescription, final double lat,
                                 final double lng) {
        locationNameTv.setText(locationName);
        locationAddressTv.setText(locationAddress);
        locationDescriptionTv.setText(locationDescription);

        directionsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireDirectionsIntent(lat, lng);
            }
        });
    }

    private void fireDirectionsIntent(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    /**
     * Shows google map for specified coordinates with marker.
     *
     * @param googleMap google map instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(locationName).snippet(locationAddress).position(new LatLng(lat, lng)));
        marker.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), (float) zoom));
        googleMap.getUiSettings().setMapToolbarEnabled(true);
    }

    /**
     * Fires intent for saving event to calendar.
     *
     * @param datetime datetime
     */
    private void fireCalenderIntent(final int datetime, final String title, final String subtitle,
                                    final String stream, final String locationName) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_BEGIN_TIME,
                datetime * TimeUtils.MILLIS_IN_SECOND);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_END_TIME,
                datetime * TimeUtils.MILLIS_IN_SECOND);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_ALL_DAY, false);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_TITLE, title);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_DESCRIPTION,
                subtitle + " by " + stream);
        intent.putExtra(Constants.INTENT_EXTRA_KEY_CALENDAR_EVENT_LOCATION, locationName);
        startActivityForResult(intent, 0);
    }

    /**
     * Shows author dialog.
     *
     * @param streamAuthorName      Author name
     * @param streamAuthorEmail     Author email
     * @param streamAuthorContact   Author contact
     * @param streamAuthorImage     Author image URL
     */
    private void showAuthorDialog(final String streamAuthorName, final String streamAuthorEmail,
                                  final String streamAuthorContact,
                                  final String streamAuthorImage) {
        Dialog authorDialog;
        authorDialog = new Dialog(EventDetailsActivity.this);
        authorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        authorDialog.setContentView(R.layout.contact_thumbnail);
        authorDialog.setCancelable(true);
        final TextView name;
        final TextView email;
        final TextView contact;
        final ImageView image;

        image = authorDialog.findViewById(R.id.author_image);
        name = authorDialog.findViewById(R.id.contact_name);
        email = authorDialog.findViewById(R.id.contact_email);
        contact = authorDialog.findViewById(R.id.contact_contact);

        name.setText(streamAuthorName);
        email.setText(streamAuthorEmail);
        contact.setText(streamAuthorContact);
        setImageOnView(streamAuthorImage, image, R.drawable.ic_person_black_24dp);

        authorDialog.show();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireImageActivityIntent(streamAuthorImage, streamAuthorName, streamAuthorEmail,
                        streamAuthorContact);
            }
        });
    }

    private void fireImageActivityIntent(final String imageUrl,
                                         final String imageTitle,
                                         final String imageSubtitle,
                                         final String imageDescription) {
        Intent intent = new Intent(EventDetailsActivity.this, ImageActivity.class);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_URL, Utils.getUsableDropboxUrl(imageUrl));
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_TITLE, imageTitle);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_SUBTITLE, imageSubtitle);
        intent.putExtra(ImageActivity.INTENT_KEY_CONTENT_DESCRIPTION, imageDescription);
        startActivity(intent);
    }

    private void setImageOnView(final String imageUrl, final ImageView imageView,
                                final int placeholderResourceId) {
        Picasso.with(this)
                .load(Uri.parse(Utils.getUsableDropboxUrl(imageUrl)))
                .placeholder(placeholderResourceId)
                .error(placeholderResourceId)
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
