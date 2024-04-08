package com.example.mergefault;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class QRArrayAdapter extends ArrayAdapter<String>{
    private ArrayList<String> eventIds;
    private Context context;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventCheckInQRRef;
    private StorageReference eventPromotionQRRef;
    public QRArrayAdapter(Context context, ArrayList<String> eventIds){
        super (context, 0, eventIds);
        this.context = context;
        this.eventIds = eventIds;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return eventIds.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_reuse_content, parent, false);
        }

        String eventId = getItem(position);
        Log.d("eventId", "eventId: " + eventId);

        ImageView checkInQRs = view.findViewById(R.id.qrCheckInView);
        ImageView promotionQRs = view.findViewById(R.id.qrPromotionView);
        TextView eventNameTextView = view.findViewById(R.id.eventIdView);


        /*
        eventCheckInQRRef = firebaseStorage.getReference().child( "QRCodes").child("CheckIn/" + eventId + ".png");
        eventCheckInQRRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(checkInQRs);
            }
        });
        eventPromotionQRRef = firebaseStorage.getReference().child( "QRCodes").child("Promotion/" + eventId + ".png");
        eventPromotionQRRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(promotionQRs);
            }
        });

         */
        eventNameTextView.setText(eventId);

        return view;
    }
}
