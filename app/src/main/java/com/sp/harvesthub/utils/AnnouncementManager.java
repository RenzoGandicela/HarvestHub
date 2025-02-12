package com.sp.harvesthub.utils;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.harvesthub.models.Announcement;
import com.sp.harvesthub.models.FeaturedHelperClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.ServerValue;

public class AnnouncementManager {
    private DatabaseReference eventsRef;
    private DatabaseReference donationRef;

    public AnnouncementManager() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        eventsRef = db.getReference("Announcements");
        donationRef = db.getReference("DonationDrives");
    }

    public void addEvent(FeaturedHelperClass event) {
        DatabaseReference ref = (event.getEventType() == 1) ? eventsRef : donationRef;
        String eventId = ref.push().getKey();
        if (eventId != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("title", event.getTitle());
            eventData.put("description", event.getDescription());
            eventData.put("location", event.getLocation());
            eventData.put("date", event.getDate());
            eventData.put("time", event.getTime());
            eventData.put("image", event.getImage());
            eventData.put("eventType", event.getEventType());
            eventData.put("timestamp", ServerValue.TIMESTAMP);

            ref.child(eventId).setValue(eventData);
        }
    }

    public void getEvents(int eventType, EventsCallback callback) {
        DatabaseReference ref = (eventType == 1) ? eventsRef : donationRef;

        ref.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<FeaturedHelperClass> events = new ArrayList<>();
                        for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                            try {
                                String title = eventSnapshot.child("title").getValue(String.class);
                                String description = eventSnapshot.child("description").getValue(String.class);
                                String date = eventSnapshot.child("date").getValue(String.class);
                                String time = eventSnapshot.child("time").getValue(String.class);
                                String location = eventSnapshot.child("location").getValue(String.class);
                                String image = eventSnapshot.child("image").getValue(String.class);
                                Integer type = eventSnapshot.child("eventType").getValue(Integer.class);

                                if (type != null && type == eventType) {
                                    events.add(new FeaturedHelperClass(image, title, description,
                                            location, date, time, type));
                                }
                            } catch (Exception e) {
                                Log.e("AnnouncementManager", "Error parsing event: " + e.getMessage());
                            }
                        }
                        callback.onEventsLoaded(events);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public interface EventsCallback {
        void onEventsLoaded(ArrayList<FeaturedHelperClass> events);
        void onError(String error);
    }
}
