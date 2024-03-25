package com.example.mergefault;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * This is the activity where the organizer creates an event
 */
public class OrganizerAddEventActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AddLimitFragment.AddLimitDialogListener , AddDescriptionFragment.AddDescriptionDialogListener {
    private Button editAddressButton;
    private Button editTimeButton;
    private Button editDateButton;
    private Button editLimitButton;
    private Button createEventButton;
    private Button descriptionButton;
    private SwitchCompat geoLocSwitch;
    private TextView addressText;
    private TextView limitText;
    private TextView timeText;
    private TextView dayText;
    private ImageView eventPosterImageView;
    private TextView descriptionText;
    private EditText eventNameEditText;
    private String eventName;
    private String organizerId;
    private String location;
    private String placeId;
    private String description;
    private String eventId;
    private Calendar dateTime = Calendar.getInstance();
    private Integer attendeeLimit;
    private Uri selectedImage;
    private Uri downloadUrl;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;

    /**
     * This function adds an String address to the corresponding textview and also saves it to location and placeId
     * @param address
     * This is the String given by the organizer through a textview
     * @param selectedPlaceId
     * This is the String of the placeId given by google places
     */
    public void addAddress(String address, String selectedPlaceId) {
        addressText.setText("Address: " + address);
        location = address;
        placeId = selectedPlaceId;
    }
    /**
     * This function adds an Integer limit to the corresponding textview and also saves it to attendeeLimit
     * @param limit
     * This is the Integer given by the organizer through a textview
     */
    @Override
    public void addLimit(Integer limit) {
        limitText.setText("Limit: " + limit.toString());
        attendeeLimit = limit;
    }
    /**
     * This function adds an String description to the corresponding textview and also saves it to description
     * @param description
     * This is the String given by the organizer through a textview
     */
    @Override
    public void addDescription(String description) {
        descriptionText.setText("Description: " + description);
        this.description = description;
    }

    /**
     * This is the function that runs at the start of the activity
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_add_event_details);

        editAddressButton = findViewById(R.id.locationSetButton);
        editTimeButton = findViewById(R.id.timeSetButton);
        editDateButton = findViewById(R.id.datSetButton);
        editLimitButton = findViewById(R.id.attendeeLimitSetButton);
        descriptionButton = findViewById(R.id.descriptionSetButton);
        geoLocSwitch = findViewById(R.id.switch1);
        addressText = findViewById(R.id.locationText);
        limitText = findViewById(R.id.attendeeLimitText);
        descriptionText = findViewById(R.id.descriptionText);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        createEventButton = findViewById(R.id.createEventButton);

        Intent recieverIntent = getIntent();
        organizerId = recieverIntent.getStringExtra("OrganizerID");

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
            Log.e("Places test", "No api key");
            finish();
            return;
        }

        // Initialize the SDK
        Places.initialize(getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);

        editAddressButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the address button, it opens a new AddAddressFragment
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(OrganizerAddEventActivity.this);
                startAutocomplete.launch(intent);

            }
        });
        editTimeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the time button, it opens a new TimePickerFragment
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                DialogFragment timepicker = new TimePickerFragment();
                timepicker.show(getSupportFragmentManager(),"Time Picker");
            }
        });
        editDateButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the date button, it opens a new DatePickerFragment
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                DialogFragment datepicker = new DatePickerFragment();
                datepicker.show(getSupportFragmentManager(),"Date Picker");
            }
        });
        editLimitButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the limit button, it opens a new AddLimitFragment
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                new AddLimitFragment().show(getSupportFragmentManager(), "Add Limit");
            }
        });
        eventPosterImageView.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the event poster imageview, it calls startActivityForResult
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                startPickingImage.launch("image/*");
            }
        });
        descriptionButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the description button, it opens a new AddDescriptionFragment
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                new AddDescriptionFragment().show(getSupportFragmentManager(), "Add Description");
            }
        });
        createEventButton.setOnClickListener(new View.OnClickListener() {
            /**
             * this is on on click listener for the create event button, it collects all the given info and creates a calls addEvent with a new created event
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
              
                eventName = eventNameEditText.getText().toString();
                addEvent(new Event(eventName, organizerId, location,dateTime,attendeeLimit, selectedImage, description, geoLocSwitch.isChecked(),eventId, placeId));
            }
        });
    }
    /**
     * This function switches activity to the next one which is OrganizerNewOrReuseQR and passes the eventId through intent
     * @param eventId
     * This is the event id given by a randomly generated firestore id
     */
    public void switchActivities(String eventId){
        Intent intent = new Intent(OrganizerAddEventActivity.this, OrganizerNewOrReuseQR.class);
        intent.putExtra("EventId", eventId);
        startActivity(intent);
    }

    /**
     * This function opens the Autocomplete activity and calls addAddress with the selected placeName and placeId
     */
    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Place place = Autocomplete.getPlaceFromIntent(intent);
                        addAddress(place.getName(),place.getId());
                        Log.d("places", "Place: " + place.getName() + place.getId());
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // The user canceled the operation.
                    Log.d("places", "User canceled autocomplete");
                }
            });
    /**
     * This function opens the image picker and stores the imageUri into selected Image as well as sets the eventPosterImageView to the selected image
     */
    private final ActivityResultLauncher<String> startPickingImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        selectedImage = uri;
                        eventPosterImageView.setImageURI(selectedImage);
                    }
                }
            }
    );
    /**
     * This opens when the TimePickerFragment sets a time, then it saves the time in dateTime and sets the time textview to the new set time
     * @param view the view associated with this listener
     * @param hourOfDay the hour that was set
     * @param minute the minute that was set
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeText = findViewById(R.id.timeText);
        dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTime.set(Calendar.MINUTE, minute);
        String time = DateFormat.getPatternInstance(DateFormat.HOUR24_MINUTE).format(dateTime.getTime());
        timeText.setText("Time: " + time);
    }

    /**
     * This opens when the DatePickerFragment sets a date, then it saves the date in dateTime and sets the date textview to the new set date
     * @param view the picker associated with the dialog
     * @param year the selected year
     * @param month the selected month (0-11 for compatibility with
     *              {@link Calendar#MONTH})
     * @param dayOfMonth the selected day of the month (1-31, depending on
     *                   month)
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dayText = findViewById(R.id.dayText);
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String day = DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateTime.getTime());
        dayText.setText("Day: " + day);
    }

    /**
     * This function gets called by addEvent after the event has been added to the firebase and the eventId is gathered, it is then used to get the download url for the eventPoster with the format of (eventId.jpg) and adds the download url to the firebase, after all that is complete it then switches activities by passing on the eventId to the qrCode screen
     * @param event
     * This is the event passed by the addEvent method
     * @param documentReference
     * This is the documentReference to event on the firebase
     */
    public void getDownloadUrl(Event event, DocumentReference documentReference){
        StorageReference eventPosterRef = storageRef.child( event.getEventID() + ".jpg");
        Log.d("eventPoster", "eventPoster: "+ event.getEventPoster());
        UploadTask uploadTask = eventPosterRef.putFile(event.getEventPoster());

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return eventPosterRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    event.setEventPoster(downloadUrl);
                    documentReference.update("EventPoster", downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                        /**
                         * Switches activity when the event is done updating
                         * @param unused
                         * Unused parameter
                         */
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("eventIdBefore", "eventid: " + event.getEventID());
                            Log.d("eventPoster", "eventPoster: " + event.getEventPoster());
                            switchActivities(eventId);
                        }
                    });
                }
            }
        });
    }

    /**
     * This function adds an event on to the firebase
     * @param event
     * This is the event given by the on click listener for the create button
     */
    public void addEvent(Event event){
        HashMap<String, Object> data = new HashMap<>();
        Log.d("eventPoster", "eventPoster: "+ event.getEventPoster());

        data.put("Location", event.getLocation());
        data.put("PlaceID", event.getPlaceId());
        data.put("DateTime", event.getDateTime().getTime());
        data.put("AttendeeLimit", event.getAttendeeLimit().toString());
        data.put("EventName", event.getEventName());
        data.put("Description", event.getDescription());
        data.put("GeoLocOn",event.getGeoLocOn());
        data.put("OrganizerID", event.getOrganizerId());

        eventRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            /**
             * This opens when the data is successfully added on the firebase, this saves the eventId of the newly created event from firebase, then adds it into a new field on the firebase as well as passing the eventId to the method getDownloadUrl()
             * @param documentReference
             * This is a reference to the newly added event on the firebase
             */
            @Override
            public void onSuccess(DocumentReference documentReference) {
                eventId = documentReference.getId().toString();
                documentReference.update("EventID", eventId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    /**
                     * Waits for the update to finish then calling getDownloadUrl()
                     * @param unused
                     * Unused parameter
                     */
                    @Override
                    public void onSuccess(Void unused) {
                        event.setEventID(eventId);
                        getDownloadUrl(event, documentReference);
                    }
                });
            }
        });
    }
}
