package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Reservation;
import com.soen345.ticketapp.notify.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvLocation, tvTime, tvSeats;
    private Button   btnReserve, btnCancelReservation, btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth      auth;

    private String eventId, eventTitle, location;
    private long   dateTimeMillis;
    private int    seats;
    private String existingReservationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvTitle              = findViewById(R.id.tvTitle);
        tvLocation           = findViewById(R.id.tvLocation);
        tvTime               = findViewById(R.id.tvTime);
        tvSeats              = findViewById(R.id.tvSeats);
        btnReserve           = findViewById(R.id.btnReserve);
        btnCancelReservation = findViewById(R.id.btnCancelReservation);
        btnBack              = findViewById(R.id.btnBack);

        eventId        = getIntent().getStringExtra("eventId");
        eventTitle     = getIntent().getStringExtra("eventTitle");
        location       = getIntent().getStringExtra("location");
        dateTimeMillis = getIntent().getLongExtra("dateTimeMillis", 0L);
        seats          = getIntent().getIntExtra("seats", 0);

        String formatted = new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                .format(new Date(dateTimeMillis));

        tvTitle.setText(eventTitle);
        tvLocation.setText(location);
        tvTime.setText(formatted);
        tvSeats.setText(String.valueOf(seats));

        btnBack.setOnClickListener(v -> finish());
        btnReserve.setOnClickListener(v -> reserveTicket());
        btnCancelReservation.setOnClickListener(v -> cancelReservation());

        checkExistingReservation();
    }

    private void checkExistingReservation() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        db.collection("reservations")
                .whereEqualTo("userId", uid)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        existingReservationId = snapshot.getDocuments().get(0).getId();
                        btnReserve.setVisibility(View.GONE);
                        btnCancelReservation.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void reserveTicket() {
        if (seats <= 0) {
            Toast.makeText(this, "No seats available", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        btnReserve.setEnabled(false);

        Reservation r = new Reservation();
        r.setUserId(uid);
        r.setEventId(eventId);
        r.setEventTitle(eventTitle);
        r.setEventLocation(location);
        r.setEventDateTimeMillis(dateTimeMillis);
        r.setCreatedAt(System.currentTimeMillis());

        db.collection("reservations").add(r)
                .addOnSuccessListener(ref -> {
                    existingReservationId = ref.getId();
                    db.collection("events").document(eventId)
                            .update("availableSeats", seats - 1);

                    NotificationHelper.sendConfirmation(this, eventTitle, location,
                            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    .format(new Date(dateTimeMillis)));

                    seats--;
                    tvSeats.setText("Available Seats: " + seats);
                    btnReserve.setVisibility(View.GONE);
                    btnCancelReservation.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Ticket reserved!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    btnReserve.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cancelReservation() {
        if (existingReservationId == null) return;

        db.collection("reservations").document(existingReservationId)
                .delete()
                .addOnSuccessListener(v -> {
                    db.collection("events").document(eventId)
                            .update("availableSeats", seats + 1);
                    seats++;
                    tvSeats.setText("Available Seats: " + seats);
                    existingReservationId = null;
                    btnCancelReservation.setVisibility(View.GONE);
                    btnReserve.setVisibility(View.VISIBLE);
                    btnReserve.setEnabled(true);
                    Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}