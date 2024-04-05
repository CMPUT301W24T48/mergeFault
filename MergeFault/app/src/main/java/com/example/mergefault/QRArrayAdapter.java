package com.example.mergefault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class QRArrayAdapter extends ArrayAdapter<Event>{
    private ArrayList<Event> events;
    private Context context;
    public QRArrayAdapter(Context context, ArrayList<Event> events){
        super (context, 0, events);
        this.context = context;
        this.events = events;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_reuse_content, parent, false);
        }

        Event event = getItem(position);

        ImageView QRs = view.findViewById(R.id.qrImageView);
        TextView eventNameTextView = view.findViewById(R.id.eventNameTextView);

        Picasso.get().load(event.getEventPoster()).into(QRs);
        eventNameTextView.setText(event.getEventName());

        return view;
    }
}
