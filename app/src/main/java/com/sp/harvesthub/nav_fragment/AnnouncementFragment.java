package com.sp.harvesthub.nav_fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.splashscreen2.HelperClasses.FeaturedAdapter;
import com.sp.splashscreen2.HelperClasses.FeaturedHelperClass;
import com.sp.splashscreen2.HelperClasses.OtherAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Announcements extends Fragment {

    private RecyclerView featuredRecycler, featuredRecyclerType2;
    private FeaturedAdapter featuredAdapter;
    private OtherAdapter otherAdapter;
    private Button addEventButton, submitEventButton, addEventButtonType2, submitEventButtonType2;
    private LinearLayout eventFormLayout, eventFormLayoutType2;
    private EditText eventTitle, eventDescription, eventDetails, eventLocation, eventImage;
    private EditText eventImage2, eventTitleType2, eventDescriptionType2, eventDetails2, eventLocationType2;

    private FirebaseAuth mAuth;
    private DatabaseReference eventsRef;
    private DatabaseReference userRef;
    private DatabaseReference donationRef;

    // Global variable initialization
    private ArrayList<FeaturedHelperClass> eventList = new ArrayList<>();
    private ArrayList<FeaturedHelperClass> otherEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        // Initialize UI elements
        featuredRecycler = findViewById(R.id.featured_recycler);
        featuredRecyclerType2 = findViewById(R.id.featured_recycler_type2);
        addEventButton = findViewById(R.id.add_event_button);
        addEventButtonType2 = findViewById(R.id.add_event_button_type2);
        submitEventButton = findViewById(R.id.submit_event_button);
        submitEventButtonType2 = findViewById(R.id.submit_event_button_type2);
        eventFormLayout = findViewById(R.id.event_form_layout);
        eventFormLayoutType2 = findViewById(R.id.event_form_layout_type2);
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDetails = findViewById(R.id.event_details);
        eventLocation = findViewById(R.id.event_location);
        eventImage = findViewById(R.id.event_image);
        eventImage2 = findViewById(R.id.event_image2);
        eventTitleType2 = findViewById(R.id.event_title_type2);
        eventDescriptionType2 = findViewById(R.id.event_description_type2);
        eventDetails2 = findViewById(R.id.event_details2);
        eventLocationType2 = findViewById(R.id.event_location_type2);

        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("Announcements");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        donationRef = FirebaseDatabase.getInstance().getReference("DonationDrives");

        // Declare lists as final
        final ArrayList<FeaturedHelperClass> eventList = new ArrayList<>();
        final ArrayList<FeaturedHelperClass> otherEventList = new ArrayList<>();  // Added this line for type 2 events

        // Initially hide the event form
        eventFormLayout.setVisibility(View.GONE);
        eventFormLayoutType2.setVisibility(View.GONE); // Hide the second event form initially

        addEventButton.setOnClickListener(v -> toggleEventForm(true, 1));
        addEventButtonType2.setOnClickListener(v -> toggleEventForm(true, 2));

        // Check user role and set UI accordingly
        checkUserRole();

        // Populate RecyclerView
        setupRecyclerView();  // Standard event list
        setupRecyclerViewType2();  // Added this to set up the second recycler view for type 2 events

        // Submit event button logic
        submitEventButton.setOnClickListener(v -> submitNewEvent());
        submitEventButtonType2.setOnClickListener(v -> submitNewEvent());

        // Listen for new events in Firebase
        listenForEventUpdates();
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();
        userRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Handle the role data
                String role = snapshot.getValue(String.class);

                switch (role.toLowerCase()) {
                    case "user":
                        addEventButton.setVisibility(View.GONE); // Hide "Add Event" button for regular users
                        addEventButtonType2.setVisibility(View.GONE); // Hide "Add Event" button for regular users
                        break;
                    case "seller":
                        addEventButton.setVisibility(View.VISIBLE); // Show "Add Event" button for sellers
                        addEventButton.setOnClickListener(v -> toggleEventForm(true, 1));
                        addEventButtonType2.setVisibility(View.VISIBLE); // Show "Add Event" button for sellers
                        addEventButtonType2.setOnClickListener(v -> toggleEventForm(true, 2));
                        break;
                    default:
                        Toast.makeText(Announcements.this, "Unknown role. Access restricted.", Toast.LENGTH_SHORT).show();
                        addEventButton.setVisibility(View.GONE);
                        addEventButtonType2.setVisibility(View.GONE);
                        return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                Toast.makeText(Announcements.this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEventForm(boolean show, int eventType) {
        if (eventType == 1) {
            eventFormLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        } else if (eventType == 2) {
            eventFormLayoutType2.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (show) {
            addEventButton.setText("Cancel");
            addEventButton.setOnClickListener(v -> toggleEventForm(false, 1));
            addEventButtonType2.setText("Cancel");
            addEventButtonType2.setOnClickListener(v -> toggleEventForm(false, 2));
        } else {
            addEventButton.setText("Add Event");
            addEventButton.setOnClickListener(v -> toggleEventForm(true, 1));
            addEventButtonType2.setText("Add Event");
            addEventButtonType2.setOnClickListener(v -> toggleEventForm(true, 2));
        }
    }

    private void submitNewEvent() {
        String title, description, details, location, image;
        int eventType;  // Default to type 1, you can change based on logic

        if (eventFormLayout.getVisibility() == View.VISIBLE) { // Type 1 form is visible
            eventType = 1; // Type 1
            title = eventTitle.getText().toString().trim();
            description = eventDescription.getText().toString().trim();
            details = eventDetails.getText().toString().trim();
            location = eventLocation.getText().toString().trim();
            image = eventImage.getText().toString().trim();

            // Validation
            if (title.isEmpty() || description.isEmpty() || details.isEmpty() || location.isEmpty() || image.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new event in the eventList
            String eventId = eventsRef.push().getKey();
            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("description", description);
            event.put("details", details);
            event.put("location", location);
            event.put("image", image); // Placeholder for image link
            event.put("eventType", eventType); // Add the event type field

            if (eventId != null) {
                eventsRef.child(eventId).setValue(event).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                        toggleEventForm(false, eventType);
                        clearForm();
                        eventList.add(new FeaturedHelperClass(image, title, description, location, details, eventType));
                        featuredAdapter.notifyDataSetChanged(); // Notify adapter of the new event
                    } else {
                        Toast.makeText(this, "Failed to add event.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (eventFormLayoutType2.getVisibility() == View.VISIBLE) { // Type 2 form is visible
            // Get values for type 2 form
            eventType = 2; // Type 2
            title = eventTitleType2.getText().toString().trim();
            description = eventDescriptionType2.getText().toString().trim();
            details = eventDetails2.getText().toString().trim();
            location = eventLocationType2.getText().toString().trim();
            image = eventImage2.getText().toString().trim();

            // Validation
            if (title.isEmpty() || description.isEmpty() || details.isEmpty() || location.isEmpty() || image.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new event in the otherEventList
            String eventId = eventsRef.push().getKey();
            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("description", description);
            event.put("details", details);
            event.put("location", location);
            event.put("image", image); // Placeholder for image link
            event.put("eventType", eventType); // Add the event type field

            if (eventId != null) {
                eventsRef.child(eventId).setValue(event).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                        toggleEventForm(false, eventType);
                        clearForm();
                        otherEventList.add(new FeaturedHelperClass(image, title, description, location, details, eventType));
                        otherAdapter.notifyDataSetChanged(); // Notify adapter of the new event
                    } else {
                        Toast.makeText(this, "Failed to add event.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void clearForm() {
        eventTitle.setText("");
        eventDescription.setText("");
        eventDetails.setText("");
        eventLocation.setText("");
        eventImage.setText("");
        eventTitleType2.setText("");
        eventDescriptionType2.setText("");
        eventDetails2.setText("");
        eventLocationType2.setText("");
        eventImage2.setText("");
    }

    private void setupRecyclerView() {
        featuredRecycler.setLayoutManager(new LinearLayoutManager(this));
        featuredAdapter = new FeaturedAdapter(eventList);
        featuredRecycler.setAdapter(featuredAdapter);
    }

    private void setupRecyclerViewType2() {
        featuredRecyclerType2.setLayoutManager(new LinearLayoutManager(this));
        otherAdapter = new OtherAdapter(otherEventList);
        featuredRecyclerType2.setAdapter(otherAdapter);
    }

    private void listenForEventUpdates() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                otherEventList.clear();

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    FeaturedHelperClass event = eventSnapshot.getValue(FeaturedHelperClass.class);
                    if (event != null) {
                        if (event.getEventType() == 1) {
                            eventList.add(event); // Add to the eventList for type 1 events
                        } else if (event.getEventType() == 2) {
                            otherEventList.add(event); // Add to the otherEventList for type 2 events
                        }
                    }
                }

                // Notify adapters of the changes
                featuredAdapter.notifyDataSetChanged();
                otherAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Announcements.this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
