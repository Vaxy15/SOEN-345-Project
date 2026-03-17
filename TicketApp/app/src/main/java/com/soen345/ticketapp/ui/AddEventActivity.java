package com.soen345.ticketapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Event");
        }

        db = FirebaseFirestore.getInstance();

        EditText etEventName = findViewById(R.id.etEventName);
        EditText etEventDate = findViewById(R.id.etEventDate);
        EditText etEventLocation = findViewById(R.id.etEventLocation);
        EditText etAvailableSeats = findViewById(R.id.etAvailableSeats);
        EditText etEventDescription = findViewById(R.id.etEventDescription);
        Button btnSaveEvent = findViewById(R.id.btnSaveEvent);

        etEventDate.setFocusable(false);
        etEventDate.setClickable(true);

        etEventDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddEventActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                selectedDay,
                                selectedMonth + 1,
                                selectedYear
                        );
                        etEventDate.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            // interdit les dates passées
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        btnSaveEvent.setOnClickListener(v -> {
            String title = etEventName.getText().toString().trim();
            String dateText = etEventDate.getText().toString().trim();
            String location = etEventLocation.getText().toString().trim();
            String seatsText = etAvailableSeats.getText().toString().trim();
            String description = etEventDescription.getText().toString().trim();

            if (title.isEmpty() || dateText.isEmpty() || location.isEmpty() || seatsText.isEmpty() || description.isEmpty()) {
                Toast.makeText(AddEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int availableSeats;
            try {
                availableSeats = Integer.parseInt(seatsText);
            } catch (NumberFormatException e) {
                Toast.makeText(AddEventActivity.this, "Available seats must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date eventDate;

            try {
                eventDate = sdf.parse(dateText);
            } catch (ParseException e) {
                Toast.makeText(AddEventActivity.this, "Please select a valid date", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("location", location);
            event.put("availableSeats", availableSeats);
            event.put("description", description);
            event.put("status", "active");
            event.put("createdBy", "admin123");
            event.put("eventDateTime", new Timestamp(eventDate));

            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddEventActivity.this, "Event added successfully", Toast.LENGTH_SHORT).show();

                        etEventName.setText("");
                        etEventDate.setText("");
                        etEventLocation.setText("");
                        etAvailableSeats.setText("");
                        etEventDescription.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AddEventActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}