package com.sp.splashscreen2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;

public class Dashboard extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText editNote;
    private TextView displayNote;
    private Button saveNote, deleteNote;

    // Temporary in-memory storage for notes
    private HashMap<String, String> notesMap = new HashMap<>();
    private String selectedDate = "";
    private DatabaseReference calendarRef; //Firebase database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        calendarView = findViewById(R.id.calendarView);
        editNote = findViewById(R.id.editNote);
        displayNote = findViewById(R.id.displayNote);
        saveNote = findViewById(R.id.saveNote);
        deleteNote = findViewById(R.id.deleteNote);

        // Initialize Firebase references
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to use the calendar.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid(); // Get the current user's unique ID
        calendarRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("calendar");

        // Set the initial selected date
        selectedDate = getFormattedDate(calendarView.getDate());
        loadNotes();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("eventDetails")) {
            String eventDetails = intent.getStringExtra("eventDetails");
            editNote.setText(eventDetails); }// Pre-fill the EditText with the event details

        // Listener for calendar date changes
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            loadNoteForSelectedDate();
        });

        // Save note for the selected date
        saveNote.setOnClickListener(v -> {
            String note = editNote.getText().toString().trim();
            if (!note.isEmpty()) {

                // Save the note to Firebase under the selected date
                calendarRef.child(selectedDate).push().setValue(note)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Dashboard.this, "Note saved!", Toast.LENGTH_SHORT).show();
                                editNote.setText("");
                                loadNotes(); // Reload all notes to update UI
                            } else {
                                Toast.makeText(Dashboard.this, "Failed to save note.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Delete note for the selected date
        deleteNote.setOnClickListener(v -> {
            calendarRef.child(selectedDate).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Dashboard.this, "Notes deleted!", Toast.LENGTH_SHORT).show();
                    loadNotes(); // Reload notes to update UI
                } else {
                    Toast.makeText(Dashboard.this, "Failed to delete notes.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(Dashboard.this, "Failed to load notes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
}