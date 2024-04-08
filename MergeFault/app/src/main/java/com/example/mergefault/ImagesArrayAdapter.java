package com.example.mergefault;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ImagesArrayAdapter extends ArrayAdapter<String[]>{
    private ArrayList<String[]> Images;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    public ImagesArrayAdapter(Context context, ArrayList<String[]> Images){
        super (context, 0, Images);
        this.context = context;
        this.Images = Images;
    }

    @Nullable
    @Override
    public String[] getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_browse_images_content, parent, false);
        }
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        ImageView images = view.findViewById(R.id.imagebrowseImageView);
        Button removeButton = view.findViewById(R.id.removeImageButton);

        String[] image = getItem(position);

        Picasso.get().load(image[0]).into(images);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(image[1], "AttendeeProfile")) {
                    String attendeeId = image[2];
                    DocumentReference attendeeRef = db.collection("attendees").document(attendeeId);
                    attendeeRef.update("AttendeeProfile", null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "Image successfully deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String eventId = image[2];
                    DocumentReference eventRef = db.collection("events").document(eventId);
                    StorageReference eventPosterRef = firebaseStorage.getReference().child("eventPosters/" + eventId + ".jpg");
                    eventPosterRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            eventRef.update("EventPoster", null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getContext(), "Image successfully deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        });

        return view;
    }
}
