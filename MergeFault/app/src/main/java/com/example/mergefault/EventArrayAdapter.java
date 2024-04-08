package com.example.mergefault;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * An ArrayAdapter implementation for displaying a list of events.
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {
    /**
     * The list of events.
     */
    private ArrayList<Event> events;
    /**
     * The context in which the adapter is used.
     */
    private Context context;
    /**
     * Constructs a new EventArrayAdapter.
     *
     * @param context The context in which the adapter is used.
     * @param events  The list of events to display.
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.attendee_signup_events_content, parent, false);
        }

        Event event = getItem(position);

        TextView eventName = view.findViewById(R.id.eventNameTextView);
        TextView date = view.findViewById(R.id.organizerText);

        eventName.setText(event.getEventName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd hh:mm a z");
        String dateString = simpleDateFormat.format(event.getDateTime().getTime());
        date.setText(dateString);

        return view;

    }
}
