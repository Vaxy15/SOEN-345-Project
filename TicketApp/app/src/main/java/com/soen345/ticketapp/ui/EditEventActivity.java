package com.soen345.ticketapp.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy admin “edit event” screen — uses {@link Event} field names (dateTimeMillis, category, cancelled).
 */
public class EditEventActivity extends AppCompatActivity {

    private EditText etEditEventName;
    private EditText etEditEventDate;
    private EditText etEditEventLocation;
    private EditText etEditAvailableSeats;
    private EditText etEditEventDescription;
    private Button btnUpdateEvent;

    private FirebaseFirestore db;
    private String eventId;
    private long selectedDateMillis = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        etEditEventName = findViewById(R.id.etEditEventName);
        etEditEventDate = findViewById(R.id.etEditEventDate);
        etEditEventLocation = findViewById(R.id.etEditEventLocation);
        etEditAvailableSeats = findViewById(R.id.etEditAvailableSeats);
        etEditEventDescription = findViewById(R.id.etEditEventDescription);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            eventId = getIntent().getStringExtra("eventId");
        }

        etEditEventDate.setOnClickListener(view -> showDatePicker());
        btnUpdateEvent.setOnClickListener(view -> updateEventInFirestore());

        if (eventId != null && !eventId.isEmpty()) {
            loadEventData();
        } else {
            Toast.makeText(this, "Invalid event id", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventData() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::populateFields)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }

    private void populateFields(DocumentSnapshot documentSnapshot) {
        Event event = documentSnapshot.toObject(Event.class);

        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        event.setId(documentSnapshot.getId());

        etEditEventName.setText(event.getTitle());
        etEditEventLocation.setText(event.getLocation());
        etEditAvailableSeats.setText(String.valueOf(event.getAvailableSeats()));

        String desc = documentSnapshot.getString("description");
        etEditEventDescription.setText(desc != null ? desc : "");

        selectedDateMillis = event.getDateTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);

        String dateText = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);

        etEditEventDate.setText(dateText);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (selectedDateMillis != 0L) {
            calendar.setTimeInMillis(selectedDateMillis);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);

                    selectedDateMillis = selectedCalendar.getTimeInMillis();

                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etEditEventDate.setText(selectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void updateEventInFirestore() {
        String title = etEditEventName.getText().toString().trim();
        String location = etEditEventLocation.getText().toString().trim();
        String seatsText = etEditAvailableSeats.getText().toString().trim();
        String description = etEditEventDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etEditEventName.setError("Event name is required");
            etEditEventName.requestFocus();
            return;
        }

        if (selectedDateMillis == 0L) {
            etEditEventDate.setError("Event date is required");
            etEditEventDate.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etEditEventLocation.setError("Location is required");
            etEditEventLocation.requestFocus();
            return;
        }

        if (seatsText.isEmpty()) {
            etEditAvailableSeats.setError("Available seats is required");
            etEditAvailableSeats.requestFocus();
            return;
        }

        int availableSeats;
        try {
            availableSeats = Integer.parseInt(seatsText);
        } catch (NumberFormatException e) {
            etEditAvailableSeats.setError("Enter a valid number");
            etEditAvailableSeats.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("location", location);
        updates.put("dateTimeMillis", selectedDateMillis);
        updates.put("availableSeats", availableSeats);
        updates.put("description", description);

        db.collection("events")
                .document(eventId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
