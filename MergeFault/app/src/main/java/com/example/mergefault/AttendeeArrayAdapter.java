package com.example.mergefault;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
public class AttendeeArrayAdapter extends ArrayAdapter<Attendee>{
    private ArrayList<Attendee> attendees;
    private Context context;
    public AttendeeArrayAdapter(Context context, ArrayList<Attendee> attendees){
        super (context, 0, attendees);
        this.context = context;
        this.attendees = attendees;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_manage_profiles_content, parent, false);
        }
        Attendee attendee = getItem(position);
        TextView attendeeName = view.findViewById(R.id.nameText);
        ImageView attendeePFP = view.findViewById(R.id.pfpImageView);
        attendeeName.setText(attendee.getName());
        Picasso.get().load(attendee.getProfImageURL()).into(attendeePFP);
        return view;
    }
}
