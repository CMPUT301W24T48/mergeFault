package com.example.mergefault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
public class AttendeeImagesArrayAdapter extends ArrayAdapter<String>{
    private ArrayList<String> attendeeImages;
    private Context context;
    public AttendeeImagesArrayAdapter(Context context, ArrayList<String> attendeeImages){
        super (context, 0, attendeeImages);
        this.context = context;
        this.attendeeImages = attendeeImages;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_browse_images_content, parent, false);
        }

        String attendeeImages = getItem(position);
        ImageView attendeeImage = view.findViewById(R.id.imagebrowseImageView);
        Picasso.get().load(attendeeImages).into(attendeeImage);

        return view;
    }
}
