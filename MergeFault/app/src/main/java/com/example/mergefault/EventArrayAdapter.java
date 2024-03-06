package com.example.mergefault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;

    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.attendee_signup_events_content, parent, false);
        }

        Event event = getItem(position);

        TextView eventName = view.findViewById(R.id.eventNameTextView);
        TextView orgName = view.findViewById(R.id.organizerText);

        eventName.setText(event.getEventName());
        orgName.setText(event.getOrgName());

        return view;

    }
}
