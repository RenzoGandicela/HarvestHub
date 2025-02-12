package com.sp.harvesthub.nav_fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.Manifest;
//import com.sp.harvesthub.Manifest;
import com.sp.harvesthub.R;
import com.sp.harvesthub.models.CalendarNote;

import java.util.Calendar;
import java.util.HashMap;

public class CalendarFragment extends Fragment {
    private static final String TAG = "CalendarFragment";

    private CalendarView calendarView;
    private EditText editNote;
    private TextView displayNote;
    private Button saveNote, deleteNote;
    private String selectedDate = "";
    private DatabaseReference calendarRef;
    private HashMap<String, String> notesMap = new HashMap<>();
    private boolean isInitialLoad = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Toast.makeText(requireContext(), "Please login to use calendar features", Toast.LENGTH_LONG).show();
            return;
        }
    }


    private void addEventToLocalCalendar(String title, String description, String location, int year, int month, int day) {
        ContentResolver contentResolver = requireContext().getContentResolver();

        // Set event start and end time
        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month, day, 10, 0); // Default start time 10:00 AM

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, 11, 0); // Default end time 11:00 AM

        String[] projection = {"_id", "calendar_displayName"};
        Cursor calCursor = requireActivity().getContentResolver()
                .query(CalendarContract.Calendars.CONTENT_URI, projection, CalendarContract.Calendars.VISIBLE + " = 1 AND "  + CalendarContract.Calendars.IS_PRIMARY + "=1", null, CalendarContract.Calendars._ID + " ASC");
        if (calCursor.getCount() <= 0){
            calCursor = requireActivity().getContentResolver()
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
            Toast.makeText(requireContext(), "Event added to local calendar!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize views
        calendarView = view.findViewById(R.id.calendarView);
        editNote = view.findViewById(R.id.editNote);
        displayNote = view.findViewById(R.id.displayNote);
        saveNote = view.findViewById(R.id.saveNote);
        deleteNote = view.findViewById(R.id.deleteNote);

        // Set up calendar date change listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            if (!isInitialLoad) {  // Only respond to user interactions
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                loadNoteForDate(selectedDate);
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
                }
            } else {
                Toast.makeText(requireContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up delete button
        deleteNote.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                deleteNote(selectedDate);
            }
        });

        // Handle incoming event data
        Bundle args = getArguments();
        if (args != null) {
            String eventTitle = args.getString("eventTitle");
            String eventDate = args.getString("eventDate"); // Format: "2/1/2025"
            String description = args.getString("description");
            String location = args.getString("location");

            if (eventDate != null) {
                try {
                    // Parse the date (M/D/YYYY)
                    String[] dateParts = eventDate.split("/");
                    int month = Integer.parseInt(dateParts[0].trim()) - 1; // Calendar months are 0-based
                    int day = Integer.parseInt(dateParts[1].trim());
                    int year = Integer.parseInt(dateParts[2].trim());

                    // Set calendar date
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);
                    calendarView.setDate(calendar.getTimeInMillis());

                    // Set selected date
                    selectedDate = year + "-" + (month + 1) + "-" + day;

                    // Prepare and set note text
                    String noteText = String.format("%s\n\nLocation: %s\nDescription: %s",
                            eventTitle,
                            location,
                            description);
                    editNote.setText(noteText);
                    displayNote.setText(noteText);

                    // Auto-save the note
                    String noteId = calendarRef.child(selectedDate).push().getKey();
                    if (noteId != null) {
                        CalendarNote note = new CalendarNote(noteText);
                        saveNote(selectedDate, noteId, note);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error setting calendar date: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error setting calendar date", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return view;
    }

    private void saveNote(String dateKey, String noteId, CalendarNote note) {
        if (calendarRef == null) {
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        calendarRef.child(dateKey).child(noteId).setValue(note.getNote())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Note saved successfully!", Toast.LENGTH_SHORT).show();
                    loadNoteForDate(dateKey);
                    addNoteToCalendar(dateKey, note.getNote()); // Add note to local calendar
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving note: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to save note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        ContentResolver cr = requireContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, noteText);
        values.put(CalendarContract.Events.DESCRIPTION, noteText);
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // Default calendar ID
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                Toast.makeText(requireContext(), "Note added to calendar!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to add note to calendar", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestCalendarPermission();
        }
    }


    private void requestCalendarPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, 101);
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
                        Toast.makeText(requireContext(), "Note deleted successfully!", Toast.LENGTH_SHORT).show();
                        displayNote.setText("No notes for this date.");
                        editNote.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting note: " + e.getMessage());
                        Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any listeners if needed
    }
}