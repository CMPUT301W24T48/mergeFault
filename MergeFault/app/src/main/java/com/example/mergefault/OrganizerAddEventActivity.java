package com.example.mergefault;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class OrganizerAddEventActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AddAddressFragment.AddAddressDialogListener, AddLimitFragment.AddLimitDialogListener {
    private Button editAddressButton;
    private Button editTimeButton;
    private Button editDateButton;
    private Button editLimitButton;
    private Button createEventButton;
    private TextView addressText;
    private TextView limitText;
    private TextView timeText;
    private TextView dayText;
    private ImageView eventPosterImageView;
    private String eventName;
    private String orgName;
    private String location;
    private Calendar dateTime = Calendar.getInstance();
    private Integer attendeeLimit;
    private Uri selectedImage;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;

    @Override
    public void addAddress(String address) {
        addressText.setText("Address: " + address);
        location = address;
    }

    @Override
    public void addLimit(Integer Limit) {
        limitText.setText("Limit: " + Limit.toString());
        attendeeLimit = Limit;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_add_event_details);

        editAddressButton = findViewById(R.id.locationSetButton);
        editTimeButton = findViewById(R.id.timeSetButton);
        editDateButton = findViewById(R.id.datSetButton);
        editLimitButton = findViewById(R.id.attendeeLimitSetButton);
        addressText = findViewById(R.id.locationText);
        limitText = findViewById(R.id.attendeeLimitText);
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        createEventButton = findViewById(R.id.createEventButton);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        editAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddAddressFragment().show(getSupportFragmentManager(), "Add Address");
            }
        });
        editTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timepicker = new TimePickerFragment();
                timepicker.show(getSupportFragmentManager(),"Time Picker");
            }
        });
        editDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datepicker = new DatePickerFragment();
                datepicker.show(getSupportFragmentManager(),"Date Picker");
            }
        });
        editLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddLimitFragment().show(getSupportFragmentManager(), "Add Limit");
            }
        });
        eventPosterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent(new Event(eventName, orgName, location,dateTime,attendeeLimit, selectedImage));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            selectedImage = data.getData();
            eventPosterImageView.setImageURI(selectedImage);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeText = findViewById(R.id.timeText);
        dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTime.set(Calendar.MINUTE, minute);
        String time = DateFormat.getPatternInstance(DateFormat.HOUR24_MINUTE).format(dateTime.getTime());
        timeText.setText("Time: " + time);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dayText = findViewById(R.id.dayText);
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String day = DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateTime.getTime());
        dayText.setText("Day: " + day);
    }

    public void addEvent(Event event){
        HashMap<String, Object> data = new HashMap<>();
        data.put("EventPoster", event.getEventPoster());
        data.put("Location", event.getLocation());
        data.put("DateTime", event.getDateTime().getTime());
        data.put("AttendeeLimit", event.getAttendeeLimit());
        eventRef.add(data);
    }
}
