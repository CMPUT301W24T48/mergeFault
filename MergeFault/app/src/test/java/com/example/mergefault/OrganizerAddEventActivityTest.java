package com.example.mergefault;

import static org.mockito.Mockito.mock;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class OrganizerAddEventActivityTest {
    private CollectionReference mockEventRef;
    private DocumentReference mockDocumentReference;

    @Before
    public void setUp() {
        mockEventRef = mock(CollectionReference.class);
        mockDocumentReference = mock(DocumentReference.class);
    }

    @Test
    public void testAddEvent() {
        // Mock data
        Event event = new Event(/* Provide necessary data */);
        HashMap<String, Object> data = new HashMap<>();

        data.put("Location", event.getLocation());
        data.put("PlaceID", event.getPlaceId());
        data.put("DateTime", event.getDateTime().getTime());
        data.put("AttendeeLimit", event.getAttendeeLimit().toString());
        data.put("EventName", event.getEventName());
        data.put("Description", event.getDescription());
        data.put("GeoLocOn", event.getGeoLocOn());
        data.put("OrganizerID", event.getOrganizerId());
        data.put("EventPoster", event.getEventPoster());

        Mockito.when(mockEventRef.add(Mockito.any())).thenReturn(Mockito.mock(Task.class));
        Mockito.when(mockDocumentReference.getId()).thenReturn("eventId");
        Mockito.when(mockDocumentReference.update("EventID", "eventId")).thenReturn(Mockito.mock(Task.class));


        // Verify that addEvent method calls were made successfully
        // Add more verifications as needed
        Mockito.verify(mockEventRef).add(Mockito.any());
        Mockito.verify(mockDocumentReference).getId();
        Mockito.verify(mockDocumentReference).update("EventID", "eventId");
    }
}
