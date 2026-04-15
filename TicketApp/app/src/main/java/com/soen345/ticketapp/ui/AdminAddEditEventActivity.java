package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;

import java.util.HashMap;
import java.util.Map;

public class AdminAddEditEventActivity extends AppCompatActivity {

    private EditText etTitle, etLocation, etTime, etSeats;
    private Spinner  spinnerCategory;
    private Button   btnSave, btnBack;

    private FirebaseFirestore db;
    private String            eventId; // null = new event, non-null = edit

    private static final String[] CATEGORIES =
        {"Movie", "Concert", "Sports", "Travel", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_event);

        db      = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        etTitle    = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        etTime     = findViewById(R.id.etTime);
        etSeats    = findViewById(R.id.etSeats);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave    = findViewById(R.id.btnSave);
        btnBack    = findViewById(R.id.btnBack);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        if (eventId != null) {
            setTitle("Edit Event");
            loadExistingEvent();
        } else {
            setTitle("Add New Event");
        }

        btnSave.setOnClickListener(v -> saveEvent());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadExistingEvent() {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    etTitle.setText(doc.getString("title"));
                    etLocation.setText(doc.getString("location"));
                    etTime.setText(doc.getString("time"));
                    Long seats = doc.getLong("availableSeats");
                    etSeats.setText(seats != null ? String.valueOf(seats) : "");

                    String cat = doc.getString("category");
                    for (int i = 0; i < CATEGORIES.length; i++) {
                        if (CATEGORIES[i].equals(cat)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            });
    }

    private void saveEvent() {
        String title    = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String seatsStr = etSeats.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(title))    { etTitle.setError("Required");    return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Required"); return; }
        if (TextUtils.isEmpty(time))     { etTime.setError("Required");     return; }
        if (TextUtils.isEmpty(seatsStr)) { etSeats.setError("Required");    return; }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
        } catch (NumberFormatException e) {
            etSeats.setError("Must be a number");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title",          title);
        data.put("location",       location);
        data.put("time",           time);
        data.put("availableSeats", seats);
        data.put("category",       category);
        data.put("cancelled",      false);

        btnSave.setEnabled(false);

        if (eventId == null) {
            // New event
            db.collection("events").add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Event added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        } else {
            // Edit existing
            db.collection("events").document(eventId).update(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }
}
