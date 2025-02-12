package com.sp.harvesthub.activities;

<<<<<<< HEAD
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
=======
import static android.content.ContentValues.TAG;
import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
<<<<<<< HEAD

import java.util.HashMap;
=======
import com.sp.harvesthub.models.CalendarNote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb

public class DashboardActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText editNote;
<<<<<<< HEAD
    private TextView displayNote, eventDetailsTxt;
    private Button saveNote, deleteNote;

    // Temporary in-memory storage for notes
    private HashMap<String, String> notesMap = new HashMap<>();
    private String selectedDate = "";
    private DatabaseReference calendarRef; //Firebase database reference
=======
    private TextView displayNote;
    private Button saveNote, deleteNote;
    private String selectedDate = "";
    private DatabaseReference calendarRef;
    private HashMap<String, String> notesMap = new HashMap<>();
    private boolean isInitialLoad = true;

    private String currentNoteText = "";
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

<<<<<<< HEAD
        // Initialize views
        initializeViews();
        
        // Initialize Firebase
        setupFirebase();
        
        // Handle intent extras (event details from announcements)
        handleIntentExtras();
        
        // Set up calendar and note functionality
        setupCalendarAndNotes();
    }

    private void initializeViews() {
=======
        // Request calendar permission
        requestCalendarPermission();

        // Initialize Firebase reference
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            calendarRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid())
                    .child("calendar");

            listenForNewNotes();
        } else {
            Toast.makeText(getApplicationContext(), "Please login to use calendar features", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
        calendarView = findViewById(R.id.calendarView);
        editNote = findViewById(R.id.editNote);
        displayNote = findViewById(R.id.displayNote);
        saveNote = findViewById(R.id.saveNote);
        deleteNote = findViewById(R.id.deleteNote);
<<<<<<< HEAD
        eventDetailsTxt = findViewById(R.id.eventDetailsTxt);
    }

    private void setupFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to use the calendar.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();
        calendarRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("calendar");
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("eventDetails")) {
            String eventTitle = intent.getStringExtra("eventTitle");
            String eventDescription = intent.getStringExtra("eventDescription");
            String eventDetails = intent.getStringExtra("eventDetails");

            if (eventTitle == null) eventTitle = "No Title";
            if (eventDescription == null) eventDescription = "No Description";
            if (eventDetails == null) eventDetails = "No Details";

            String preFilledText = "Title: " + eventTitle + "\n\nDescription: " + eventDescription + "\n\nDetails: " + eventDetails;

            editNote.setText(preFilledText);
        }
    }

    private void setupCalendarAndNotes() {
        // Set initial selected date
        selectedDate = getFormattedDate(calendarView.getDate());
        loadNotes();

        // Calendar date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            loadNoteForSelectedDate();
        });

        // Save note button click listener
        saveNote.setOnClickListener(v -> {
            String note = editNote.getText().toString().trim();
            if (!note.isEmpty()) {
                calendarRef.child(selectedDate).push().setValue(note)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(DashboardActivity.this, "Note saved!", Toast.LENGTH_SHORT).show();
                                editNote.setText("");
                                loadNotes();
                            } else {
                                Toast.makeText(DashboardActivity.this, "Failed to save note.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Delete note button click listener
        deleteNote.setOnClickListener(v -> {
            calendarRef.child(selectedDate).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DashboardActivity.this, "Notes deleted!", Toast.LENGTH_SHORT).show();
                            loadNotes();
                        } else {
                            Toast.makeText(DashboardActivity.this, "Failed to delete notes.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadNotes() {
        // Fetch all notes from Firebase and update the UI
        calendarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesMap.clear();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey(); // Date
                    StringBuilder notesForDate = new StringBuilder();
                    for (DataSnapshot noteSnapshot : dateSnapshot.getChildren()) {
                        String note = noteSnapshot.getValue(String.class);
                        notesForDate.append(note).append("\n");
                    }
                    notesMap.put(date, notesForDate.toString().trim());
                }
                //highlightDatesWithNotes(); // Highlight dates with notes
                loadNoteForSelectedDate(); // Load notes for the currently selected date
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load notes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNoteForSelectedDate() {
        // Load notes for the currently selected date
        String note = notesMap.get(selectedDate);
        if (note != null && !note.isEmpty()) {
            displayNote.setText(note);
        } else {
            displayNote.setText("No notes for this date.");
        }
    }

    private String getFormattedDate(long date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }
=======


        // Set up calendar date change listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            if (!isInitialLoad) {  // Only respond to user interactions
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                loadNoteForDate(selectedDate);

                editNote.setText(currentNoteText);
            }
        });

        // Set up save button
        saveNote.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                String noteText = editNote.getText().toString().trim();
                CalendarNote note = new CalendarNote(noteText);
                String noteId = calendarRef.child(selectedDate).push().getKey();
                if (noteId != null) {
                    saveNote(selectedDate, noteId, note);
                    displayNote.setText(noteText); // Save note to displayNote field
                    currentNoteText = noteText;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up delete button
        deleteNote.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                deleteNote(selectedDate);
            }
        });

        // Handle incoming event data
        Bundle args = getIntent().getExtras();
        if (args != null) {
            String eventTitle = args.getString("eventTitle");
            String eventDate = args.getString("eventDate"); // Format: "2/1/2025"
            String eventLocation = args.getString("eventLocation");
            String eventTime = args.getString("eventTime");

            if (eventDate != null) {
                try {
                    // Parse the date (M/D/YYYY)
                    String[] dateParts = eventDate.split("-");
                    if (dateParts.length != 3) {
                        Log.e(TAG, "Invalid date format: " + eventDate);
                        Toast.makeText(getApplicationContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int year = Integer.parseInt(dateParts[0].trim());
                    int month = Integer.parseInt(dateParts[1].trim()) - 1; // Calendar months are 0-based
                    int day = Integer.parseInt(dateParts[2].trim());


                    // Set calendar date
                    Calendar calendar = Calendar.getInstance();
                    if (year <= 0 || month < 0 || day <= 0) {
                        calendar.setTimeInMillis(System.currentTimeMillis()); // Use current date
                    } else {
                        calendar.set(year, month, day); // Set to parsed event date
                    }
                    calendarView.setDate(calendar.getTimeInMillis());

                    // Set selected date
                    selectedDate = year + "-" + (month + 1) + "-" + day;

                    // Prepare and set note text
                    String noteText = String.format(
                            "🎉 Title: %s\n\n📍 Location: %s\n\n📅 Date: %s\n\n⏰ Time: %s",
                            eventTitle,
                            eventLocation,
                            eventDate,
                            eventTime
                    );
                    editNote.setText(noteText);
                    displayNote.setText(noteText);

                    currentNoteText = noteText;

                    // Auto-save the note
                    //String noteId = calendarRef.child(selectedDate).push().getKey();
                    //if (noteId != null) {
                    //CalendarNote note = new CalendarNote(noteText);
                    //saveNote(selectedDate, noteId, note);
                    //}

                } catch (Exception e) {
                    Log.e(TAG, "Error setting calendar date: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Error setting calendar date", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void addEventToLocalCalendar(String title, String description, String location, int year, int month, int day) {
        ContentResolver contentResolver = getApplication().getContentResolver();

        // Set event start and end time
        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month, day, 10, 0); // Default start time 10:00 AM

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, 11, 0); // Default end time 11:00 AM

        String[] projection = {"_id", "calendar_displayName"};
        Cursor calCursor = getApplication().getContentResolver()
                .query(CalendarContract.Calendars.CONTENT_URI, projection, CalendarContract.Calendars.VISIBLE + " = 1 AND "  + CalendarContract.Calendars.IS_PRIMARY + "=1", null, CalendarContract.Calendars._ID + " ASC");
        if (calCursor.getCount() <= 0){
            calCursor = getApplication().getContentResolver()
                    .query(CalendarContract.Calendars.CONTENT_URI, projection, CalendarContract.Calendars.VISIBLE + " = 1", null, CalendarContract.Calendars._ID + " ASC");
        }

        calCursor.moveToFirst();
        long calID = calCursor.getLong(0);
        calCursor.close();

        ContentValues eventValues = new ContentValues();
        eventValues.put(CalendarContract.Events.CALENDAR_ID, calID); // Default calendar
        eventValues.put(CalendarContract.Events.TITLE, title);
        eventValues.put(CalendarContract.Events.DESCRIPTION, description);
        eventValues.put(CalendarContract.Events.EVENT_LOCATION, location);
        eventValues.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        eventValues.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        // Insert event
        Uri eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
        if (eventUri != null) {
            Toast.makeText(getApplicationContext(), "Event added to local calendar!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }



    private void saveNote(String dateKey, String noteId, CalendarNote note) {
        if (calendarRef == null) {
            Toast.makeText(getApplicationContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        calendarRef.child(dateKey).child(noteId).setValue(note.getNote())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show();
                    loadNoteForDate(dateKey);
                    addNoteToCalendar(dateKey, note.getNote()); // Add note to local calendar
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving note: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to save note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addNoteToCalendar(String dateKey, String noteText) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(dateKey));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        long startMillis = cal.getTimeInMillis();
        long endMillis = startMillis + (60 * 60 * 1000); // 1-hour event

        ContentResolver cr = getApplication().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, noteText);
        values.put(CalendarContract.Events.DESCRIPTION, noteText);
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // Default calendar ID
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                Toast.makeText(getApplicationContext(), "Note added to calendar!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to add note to calendar", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestCalendarPermission();
        }
    }


    private void requestCalendarPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 101);
        }
    }




    private void loadNoteForDate(String dateKey) {
        if (calendarRef != null) {
            calendarRef.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    StringBuilder notes = new StringBuilder();
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        String note = noteSnapshot.getValue(String.class);
                        if (note != null) {
                            notes.append(note).append("\n\n");
                        }
                    }

                    String allNotes = notes.toString().trim();
                    if (!allNotes.isEmpty()) {
                        displayNote.setText(allNotes);
                        editNote.setText(allNotes);
                    } else {
                        displayNote.setText("No notes for this date.");
                        editNote.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error loading note: " + error.getMessage());
                }
            });
        }
    }

    private void deleteNote(String dateKey) {
        if (calendarRef != null) {
            calendarRef.child(dateKey).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Note deleted successfully!", Toast.LENGTH_SHORT).show();
                        displayNote.setText("No notes for this date.");
                        editNote.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting note: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Failed to delete note", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void listenForNewNotes() {
        if (calendarRef != null) {
            calendarRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot noteSnapshot : dateSnapshot.getChildren()) {
                            String note = noteSnapshot.getValue(String.class);
                            if (note != null && !notesMap.containsKey(noteSnapshot.getKey())) {
                                notesMap.put(noteSnapshot.getKey(), note);
                                //showNotification("New Calendar Note", note);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }
    }

    /*private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "calendar_channel")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "calendar_channel",
                    "Calendar Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, builder.build());
    } */

    @Override
    public void onResume() {
        super.onResume();
        isInitialLoad = false;  // Reset the flag when fragment resumes
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any listeners or resources here if needed
    }


>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
}