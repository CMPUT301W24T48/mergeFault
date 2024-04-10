package com.example.mergefault;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An ArrayAdapter implementation for managing attendees in various contexts.
 */
public class AttendeeArrayAdapter extends ArrayAdapter<Attendee>{
    /**
     * The list of attendees.
     */
    private ArrayList<Attendee> attendees;
    /**
     * The context in which the adapter is used.
     */
    private Context context;
    /**
     * The user role (e.g., "admin" or "organizer").
     */
    private String user;
    /**
     * Constructs a new AttendeeArrayAdapter.
     *
     * @param context   The context in which the adapter is used.
     * @param attendees The list of attendees.
     * @param user      The user role (e.g., "admin" or "organizer").
     */
    public AttendeeArrayAdapter(Context context, ArrayList<Attendee> attendees, String user){
        super (context, 0, attendees);
        this.context = context;
        this.attendees = attendees;
        this.user = user;
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
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null && user.equals("admin")){
            view = LayoutInflater.from(getContext()).inflate(R.layout.admin_manage_profiles_content, parent, false);
            Attendee attendee = getItem(position);
            TextView attendeeName = view.findViewById(R.id.nameText);
            ImageView attendeePFP = view.findViewById(R.id.pfpImageView);
            attendeeName.setText(attendee.getName());
            if (attendee.getProfImageURL() != null){
                Picasso.get().load(attendee.getProfImageURL()).into(attendeePFP);
            }

            Button removeProfileButton = view.findViewById(R.id.removeProfileButton);
            removeProfileButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("Position", "position: " + position +" size: " + attendees.size());
                    FirebaseFirestore db  = FirebaseFirestore.getInstance();
                    if (attendees.size() != 0){
                        DocumentReference attendeeDocRef = db.collection("attendees").document(attendees.get(position).getAttendeeId());
                        attendeeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("signedInEvents") != null){
                                    List<String> signedInEvents = (List<String>) documentSnapshot.get("signedInEvents");
                                    for (int i = 0; i < signedInEvents.size(); i++) {
                                        db.collection("events").document(signedInEvents.get(i)).collection("attendees").document(attendees.get(position).getAttendeeId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("","deleted profile from all events");
                                            }
                                        });
                                    }
                                }
                                attendeeDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "profile deleted successfully", Toast.LENGTH_SHORT).show();
                                        Log.d("","profile deleted successfully");
                                        Log.d("Position", "position: " + position +" size: " + attendees.size());
                                    }
                                });
                            }
                        });
                    }
                }
            });

        } else if (view == null && user.equals("organizer")) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_attendee_list_content, parent, false);
            Attendee attendee = getItem(position);
            TextView attendeeName = view.findViewById(R.id.nameText);
            ImageView attendeePFP = view.findViewById(R.id.pfpImageView);
            attendeeName.setText(attendee.getName());
            Picasso.get().load(attendee.getProfImageURL()).into(attendeePFP);
            TextView checkInCounter = view.findViewById(R.id.checkInCountText);
            TextView checkedInStatus = view.findViewById(R.id.checkedInText);
            checkInCounter.setText(attendee.getCheckInCount().toString());

            if (attendee.getCheckedIn()) {
                checkedInStatus.setText("Yes");
            } else {
                checkedInStatus.setText("No");
            }

        } else if (Objects.equals(user, "organizer")){
            Attendee attendee = getItem(position);
            TextView attendeeName = view.findViewById(R.id.nameText);
            ImageView attendeePFP = view.findViewById(R.id.pfpImageView);
            attendeeName.setText(attendee.getName());
            Picasso.get().load(attendee.getProfImageURL()).into(attendeePFP);
            TextView checkInCounter = view.findViewById(R.id.checkInCountText);
            TextView checkedInStatus = view.findViewById(R.id.checkedInText);
            checkInCounter.setText(attendee.getCheckInCount().toString());

            if (attendee.getCheckedIn()) {
                checkedInStatus.setText("Yes");
            } else {
                checkedInStatus.setText("No");
            }

        } else if (Objects.equals(user, "admin")) {
            Attendee attendee = getItem(position);
            TextView attendeeName = view.findViewById(R.id.nameText);
            ImageView attendeePFP = view.findViewById(R.id.pfpImageView);
            attendeeName.setText(attendee.getName());
            Picasso.get().load(attendee.getProfImageURL()).into(attendeePFP);
            Button removeProfileButton = view.findViewById(R.id.removeProfileButton);
            removeProfileButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("Position", "position: " + position +" size: " + attendees.size());
                    FirebaseFirestore db  = FirebaseFirestore.getInstance();
                    if (attendees.size() != 0){
                        DocumentReference attendeeDocRef = db.collection("attendees").document(attendees.get(position).getAttendeeId());
                        attendeeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("signedInEvents") != null){
                                    List<String> signedInEvents = (List<String>) documentSnapshot.get("signedInEvents");
                                    for (int i = 0; i < signedInEvents.size(); i++) {
                                        db.collection("events").document(signedInEvents.get(i)).collection("attendees").document(attendees.get(position).getAttendeeId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("","deleted profile from all events");
                                            }
                                        });
                                    }
                                }
                                attendeeDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "profile deleted successfully", Toast.LENGTH_SHORT).show();
                                        Log.d("","profile deleted successfully");
                                        Log.d("Position", "position: " + position +" size: " + attendees.size());
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
        return view;
    }
}
