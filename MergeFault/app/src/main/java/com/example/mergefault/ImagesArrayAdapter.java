package com.example.mergefault;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
public class ImagesArrayAdapter extends ArrayAdapter<String>{
    private ArrayList<String> Images;
    private Context context;
    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private CollectionReference eventRef;
    public ImagesArrayAdapter(Context context, ArrayList<String> Images){
        super (context, 0, Images);
        this.context = context;
        this.Images = Images;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        db = FirebaseFirestore.getInstance();
        attendeeRef = db.collection("attendees");
        eventRef = db.collection("events");

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_browse_images_content, parent, false);
        }

        String image = getItem(position);
        ImageView images = view.findViewById(R.id.imagebrowseImageView);
        Picasso.get().load(image).into(images);

        TextView deleteImage = view.findViewById(R.id.removeImageTextView);
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", error.toString());
                            return;
                        }
                        if (value != null){
                            //delete pfpimage
                        }
                    }
                });
                eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", error.toString());
                            return;
                        }
                        if (value != null){
                            //delete eventimage
                        }
                    }
                });
            }
        });

        return view;
    }
}
