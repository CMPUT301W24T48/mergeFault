package com.example.mergefault;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ImagesArrayAdapter extends ArrayAdapter<String>{
    private ArrayList<String> Images;
    private Context context;
    public ImagesArrayAdapter(Context context, ArrayList<String> Images){
        super (context, 0, Images);
        this.context = context;
        this.Images = Images;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_browse_images_content, parent, false);
        }

        String image = getItem(position);
        ImageView images = view.findViewById(R.id.imagebrowseImageView);
        Picasso.get().load(image).into(images);

        return view;
    }
}
