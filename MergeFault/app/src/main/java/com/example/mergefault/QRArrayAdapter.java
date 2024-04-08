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

/**
 * ArrayAdapter implementation for displaying QR codes associated with event IDs.
 */
public class QRArrayAdapter extends ArrayAdapter<String>{
    /**
     * The list of event IDs.
     */
    private ArrayList<String> eventIds;
    /**
     * The context in which the adapter is used.
     */
    private Context context;
    /**
     * FirebaseStorage instance for accessing Firebase storage.
     */
    private FirebaseStorage firebaseStorage;
    /**
     * Reference to the QR code for event check-in.
     */
    private StorageReference eventCheckInQRRef;
    /**
     * Reference to the QR code for event promotion.
     */
    private StorageReference eventPromotionQRRef;
    /**
     * Constructs a new QRArrayAdapter.
     *
     * @param context   The context in which the adapter is used.
     * @param eventIds  The list of event IDs to display.
     */
    public QRArrayAdapter(Context context, ArrayList<String> eventIds){
        super (context, 0, eventIds);
        this.context = context;
        this.eventIds = eventIds;
    }

    /**
     * Get the event ID at the specified position.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The event ID at the specified position.
     */
    @Nullable
    @Override
    public String getItem(int position) {
        return eventIds.get(position);
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

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_reuse_content, parent, false);
        }

        String eventId = getItem(position);
        Log.d("eventId", "eventId: " + eventId);

        ImageView checkInQRs = view.findViewById(R.id.qrCheckInView);
        ImageView promotionQRs = view.findViewById(R.id.qrPromotionView);
        TextView eventNameTextView = view.findViewById(R.id.eventIdView);

        eventNameTextView.setText(eventId);

        return view;
    }
}
