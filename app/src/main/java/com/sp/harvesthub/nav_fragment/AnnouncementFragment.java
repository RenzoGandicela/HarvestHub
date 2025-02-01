package com.sp.harvesthub.nav_fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.FeaturedAdapter;
import com.sp.harvesthub.models.Announcement;
import com.sp.harvesthub.models.FeaturedHelperClass;
import com.sp.harvesthub.utils.AnnouncementManager;
import com.sp.harvesthub.utils.FirebaseHelper;

import java.util.ArrayList;

public class AnnouncementFragment extends Fragment implements FeaturedAdapter.OnEventClickListener {
    private static final String TAG = "AnnouncementFragment";
    private RecyclerView featuredRecycler, featuredRecyclerType2;
    private FeaturedAdapter featuredAdapter, donationAdapter;
    private Button addEventButton, submitEventButton, addEventButtonType2, submitEventButtonType2;
    private LinearLayout eventFormLayout, eventFormLayoutType2;
    private EditText eventTitle, eventDescription, eventDetails, eventLocation, eventImage;
    private EditText eventTitleType2, eventDescriptionType2, eventDetails2, eventLocationType2, eventImage2;
    private ArrayList<FeaturedHelperClass> eventList, donationList;
    private DatabaseReference eventsRef, donationRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventList = new ArrayList<>();
        donationList = new ArrayList<>();
        eventsRef = FirebaseDatabase.getInstance().getReference("Announcements");
        donationRef = FirebaseDatabase.getInstance().getReference("DonationDrives");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        initializeViews(view);
        setupRecyclerViews();
        setupListeners();
        checkUserRole();
        loadEvents();
        return view;
    }

    private void initializeViews(View view) {
        // Initialize RecyclerViews
        featuredRecycler = view.findViewById(R.id.featured_recycler);
        featuredRecyclerType2 = view.findViewById(R.id.featured_recycler_type2);

        // Initialize Buttons and Forms
        addEventButton = view.findViewById(R.id.add_event_button);
        addEventButtonType2 = view.findViewById(R.id.add_event_button_type2);
        submitEventButton = view.findViewById(R.id.submit_event_button);
        submitEventButtonType2 = view.findViewById(R.id.submit_event_button_type2);
        eventFormLayout = view.findViewById(R.id.event_form_layout);
        eventFormLayoutType2 = view.findViewById(R.id.event_form_layout_type2);

        // Initialize EditTexts for regular events
        eventTitle = view.findViewById(R.id.event_title);
        eventDescription = view.findViewById(R.id.event_description);
        eventDetails = view.findViewById(R.id.event_details);
        eventLocation = view.findViewById(R.id.event_location);
        eventImage = view.findViewById(R.id.event_image);

        // Initialize EditTexts for donation events
        eventTitleType2 = view.findViewById(R.id.event_title_type2);
        eventDescriptionType2 = view.findViewById(R.id.event_description_type2);
        eventDetails2 = view.findViewById(R.id.event_details2);
        eventLocationType2 = view.findViewById(R.id.event_location_type2);
        eventImage2 = view.findViewById(R.id.event_image2);

        // Initially hide forms
        eventFormLayout.setVisibility(View.GONE);
        eventFormLayoutType2.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        // Setup regular events RecyclerView
        featuredRecycler.setLayoutManager(new LinearLayoutManager(getContext(), 
            LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter = new FeaturedAdapter(requireContext(), eventList);
        featuredRecycler.setAdapter(featuredAdapter);

        // Setup donation events RecyclerView
        featuredRecyclerType2.setLayoutManager(new LinearLayoutManager(getContext(), 
            LinearLayoutManager.HORIZONTAL, false));
        donationAdapter = new FeaturedAdapter(requireContext(), donationList);
        featuredRecyclerType2.setAdapter(donationAdapter);
    }

    private void setupListeners() {
        addEventButton.setOnClickListener(v -> toggleEventForm(true, 1));
        addEventButtonType2.setOnClickListener(v -> toggleEventForm(true, 2));
        submitEventButton.setOnClickListener(v -> submitEvent(1));
        submitEventButtonType2.setOnClickListener(v -> submitEvent(2));
    }

    private void toggleEventForm(boolean show, int type) {
        if (type == 1) {
            eventFormLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            addEventButton.setText(show ? "Cancel" : "Add Event");
        } else {
            eventFormLayoutType2.setVisibility(show ? View.VISIBLE : View.GONE);
            addEventButtonType2.setText(show ? "Cancel" : "Add Donation Drive");
        }
    }

    private void submitEvent(int type) {
        String title, description, details, location, image;
        DatabaseReference ref = (type == 1) ? eventsRef : donationRef;
        
        if (type == 1) {
            title = eventTitle.getText().toString().trim();
            description = eventDescription.getText().toString().trim();
            details = eventDetails.getText().toString().trim();
            location = eventLocation.getText().toString().trim();
            image = eventImage.getText().toString().trim();
        } else {
            title = eventTitleType2.getText().toString().trim();
            description = eventDescriptionType2.getText().toString().trim();
            details = eventDetails2.getText().toString().trim();
            location = eventLocationType2.getText().toString().trim();
            image = eventImage2.getText().toString().trim();
        }

        if (title.isEmpty() || description.isEmpty() || details.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FeaturedHelperClass event = new FeaturedHelperClass(image, title, description, location, details, type);
        String eventId = ref.push().getKey();
        
        if (eventId != null) {
            ref.child(eventId).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event added successfully!", Toast.LENGTH_SHORT).show();
                    toggleEventForm(false, type);
                    clearForm();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), 
                    "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadEvents() {
        // Load regular events
        eventsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    try {
                        String title = eventSnapshot.child("title").getValue(String.class);
                        String description = eventSnapshot.child("description").getValue(String.class);
                        String details = eventSnapshot.child("details").getValue(String.class);
                        String location = eventSnapshot.child("location").getValue(String.class);
                        String image = eventSnapshot.child("image").getValue(String.class);
                        Integer eventType = eventSnapshot.child("eventType").getValue(Integer.class);

                        if (eventType != null && eventType == 1) {
                            eventList.add(new FeaturedHelperClass(image, title, description, location, details, eventType));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing regular event: " + e.getMessage());
                    }
                }
                
                if (eventList.isEmpty()) {
                    addDefaultAnnouncements();
                }
                
                featuredAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading regular events: " + error.getMessage());
                addDefaultAnnouncements();
                featuredAdapter.notifyDataSetChanged();
            }
        });

        // Load donation drives
        donationRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                donationList.clear();
                
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    try {
                        String title = eventSnapshot.child("title").getValue(String.class);
                        String description = eventSnapshot.child("description").getValue(String.class);
                        String details = eventSnapshot.child("details").getValue(String.class);
                        String location = eventSnapshot.child("location").getValue(String.class);
                        String image = eventSnapshot.child("image").getValue(String.class);
                        Integer eventType = eventSnapshot.child("eventType").getValue(Integer.class);

                        if (eventType != null && eventType == 2) {
                            donationList.add(new FeaturedHelperClass(image, title, description, location, details, eventType));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing donation event: " + e.getMessage());
                    }
                }
                
                donationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading donation events: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load donation events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDefaultAnnouncements() {
        // Regular event
        eventList.add(new FeaturedHelperClass(
            "food_sharing", 
            "Food Sharing Event @Dover",
            "Join us for a vibrant food-sharing event at Dover Community Hub!",
            "\uD83D\uDCCD Dover Community Hub",
            "\uD83D\uDCC5 Date: 10/10/2025\n⏰ Time: 6:00 PM - 9:00 PM",
            1  // Type 1 event
        ));

        // Donation drive
        donationList.add(new FeaturedHelperClass(
            "donation_drive", 
            "Food Donation Drive",
            "Help us collect food items for families in need",
            "\uD83D\uDCCD Tampines Hub",
            "\uD83D\uDCC5 Date: 15/10/2025\n⏰ Time: 9:00 AM - 12:00 PM",
            2  // Type 2 event
        ));
    }

    private void checkUserRole() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        
        userRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if ("seller".equalsIgnoreCase(role)) {
                    // Show add event buttons for sellers
                    addEventButton.setVisibility(View.VISIBLE);
                    addEventButtonType2.setVisibility(View.VISIBLE);
                } else {
                    // Hide for regular users
                    addEventButton.setVisibility(View.GONE);
                    addEventButtonType2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user role: " + error.getMessage());
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        featuredRecycler.setAdapter(null);
        featuredRecyclerType2.setAdapter(null);
        featuredAdapter = null;
        donationAdapter = null;
    }

    @Override
    public void onRemindClick(FeaturedHelperClass event) {
        try {
            // Create bundle with event info
            Bundle args = new Bundle();
            args.putString("eventTitle", event.getTitle());
            args.putString("eventDate", event.getDetails()); // Already in correct format
            args.putString("description", event.getDescription());
            args.putString("location", event.getLocation());
            
            // Navigate to Calendar Fragment
            CalendarFragment calendarFragment = new CalendarFragment();
            calendarFragment.setArguments(args);
            
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, calendarFragment)
                .addToBackStack(null)
                .commit();
        } catch (Exception e) {
            Log.e(TAG, "Error setting reminder: " + e.getMessage());
            Toast.makeText(requireContext(), "Error setting reminder", Toast.LENGTH_SHORT).show();
        }
    }
}
