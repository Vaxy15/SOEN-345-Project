package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvEventName;
    private TextView tvEventDate;
    private TextView tvEventLocation;
    private TextView tvAvailableSeats;
    private TextView tvEventDescription;
    private TextView tvEventStatus;
    private Button btnEditEvent;
    private Button btnCancelEvent;

    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();

        tvEventName = findViewById(R.id.tvEventName);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventStatus = findViewById(R.id.tvEventStatus);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnCancelEvent = findViewById(R.id.btnCancelEvent);

        eventId = getIntent().getStringExtra("event_id");

        btnEditEvent.setOnClickListener(view -> {
            if (eventId != null) {
                Intent intent = new Intent(this, EditEventActivity.class);
                intent.putExtra("event_id", eventId);
                startActivity(intent);
            }
        });

        btnCancelEvent.setOnClickListener(view -> cancelEvent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvent();
    }

    private void loadEvent() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::displayEvent)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }

    private void displayEvent(DocumentSnapshot documentSnapshot) {
        currentEvent = documentSnapshot.toObject(Event.class);

        if (currentEvent == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvEventName.setText(currentEvent.getTitle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentEvent.getEventDateTime().toDate());
        String formattedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);

        tvEventDate.setText("Date: " + formattedDate);
        tvEventLocation.setText("Location: " + currentEvent.getLocation());
        tvAvailableSeats.setText("Available seats: " + currentEvent.getAvailableSeats());
        tvEventDescription.setText("Description: " + currentEvent.getDescription());
        tvEventStatus.setText("Status: " + currentEvent.getStatus());

        boolean isCancelled = "Cancelled".equalsIgnoreCase(currentEvent.getStatus());
        btnCancelEvent.setEnabled(!isCancelled);
    }

    private void cancelEvent() {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Cancelled");

        db.collection("events")
                .document(eventId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event cancelled and customers notified", Toast.LENGTH_SHORT).show();
                    loadEvent();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to cancel event", Toast.LENGTH_SHORT).show()
                );
    }
}