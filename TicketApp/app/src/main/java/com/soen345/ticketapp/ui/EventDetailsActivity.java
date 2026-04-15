package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.data.BookingRepository;
import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.notify.ConfirmationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvLocation, tvTime, tvSeats;
    private Button btnReserve, btnCancelReservation, btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private BookingRepository bookingRepository;

    private String eventId;
    private String eventTitle;
    private String location;
    private String time;
    private long dateTimeMillis;
    private int seats;
    private boolean cancelled;

    private String existingReservationId;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bookingRepository = new BookingRepository();

        tvTitle = findViewById(R.id.tvTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvTime = findViewById(R.id.tvTime);
        tvSeats = findViewById(R.id.tvSeats);
        btnReserve = findViewById(R.id.btnReserve);
        btnCancelReservation = findViewById(R.id.btnCancelReservation);
        btnBack = findViewById(R.id.btnBack);

        eventId = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");
        location = getIntent().getStringExtra("location");
        time = getIntent().getStringExtra("time");
        dateTimeMillis = getIntent().getLongExtra("dateTimeMillis", 0L);
        seats = getIntent().getIntExtra("seats", 0);
        cancelled = getIntent().getBooleanExtra("cancelled", false);

        // Render immediately from intent extras so the screen is usable in tests
        // and as a fallback if Firestore is unavailable.
        renderEvent();
        updateActionButtons();

        btnBack.setOnClickListener(v -> finish());
        btnReserve.setOnClickListener(v -> reserveTicket());
        btnCancelReservation.setOnClickListener(v -> cancelReservation());

        // Keep the activity alive even if the document is missing.
        // Only skip remote refresh when there is no usable event id.
        if (eventId != null && !eventId.trim().isEmpty()) {
            loadEventDetails();
            checkExistingReservation();
        }
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener(doc -> {
                // Do not finish the activity if the document is missing.
                // Tests and some navigation paths rely on the passed intent extras.
                if (!doc.exists()) {
                    updateActionButtons();
                    return;
                }

                Event e = doc.toObject(Event.class);
                if (e != null) {
                    if (e.getTitle() != null) {
                        eventTitle = e.getTitle();
                    }
                    if (e.getLocation() != null) {
                        location = e.getLocation();
                    }
                    time = e.getTime();
                    dateTimeMillis = e.getDateTimeMillis();
                    seats = e.getAvailableSeats();
                    cancelled = e.isCancelled();
                }

                renderEvent();
                updateActionButtons();
            })
            .addOnFailureListener(e -> {
                // Keep the current screen rendered from extras instead of closing it.
                updateActionButtons();
            });
    }

    private void checkExistingReservation() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || eventId == null || eventId.trim().isEmpty()) {
            existingReservationId = null;
            updateActionButtons();
            return;
        }

        db.collection("reservations")
            .whereEqualTo("userId", user.getUid())
            .whereEqualTo("eventId", eventId)
            .limit(1)
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.isEmpty()) {
                    existingReservationId = snapshot.getDocuments().get(0).getId();
                } else {
                    existingReservationId = null;
                }
                updateActionButtons();
            })
            .addOnFailureListener(e -> updateActionButtons());
    }

    private void reserveTicket() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cancelled) {
            Toast.makeText(this, "This event was cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (seats <= 0) {
            Toast.makeText(this, "No seats available", Toast.LENGTH_SHORT).show();
            return;
        }

        busy = true;
        updateActionButtons();

        bookingRepository.reserveSeat(eventId, user.getUid())
            .addOnSuccessListener(reservationId -> {
                existingReservationId = reservationId;
                busy = false;

                loadEventDetails();
                checkExistingReservation();
                sendConfirmationUsingSavedPreference();

                Toast.makeText(this, "Ticket reserved!", Toast.LENGTH_LONG).show();
            })
            .addOnFailureListener(e -> {
                busy = false;
                loadEventDetails();
                checkExistingReservation();

                String message = e.getMessage() != null ? e.getMessage() : "Reservation failed";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            });
    }

    private void cancelReservation() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || existingReservationId == null) {
            return;
        }

        busy = true;
        updateActionButtons();

        bookingRepository.cancelReservation(existingReservationId, user.getUid())
            .addOnSuccessListener(v -> {
                existingReservationId = null;
                busy = false;

                loadEventDetails();
                checkExistingReservation();

                Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                busy = false;
                loadEventDetails();
                checkExistingReservation();

                String message = e.getMessage() != null ? e.getMessage() : "Cancellation failed";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            });
    }

    private void sendConfirmationUsingSavedPreference() {
        FirebaseUser user = auth.getCurrentUser();
        Event currentEvent = buildCurrentEvent();

        if (user == null) {
            ConfirmationHelper.deliver(
                this,
                ConfirmationHelper.CHANNEL_DEVICE,
                currentEvent,
                null,
                null
            );
            return;
        }

        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(doc -> {
                String channel = doc.getString("confirmationChannel");
                if (channel == null || channel.trim().isEmpty()) {
                    channel = ConfirmationHelper.CHANNEL_DEVICE;
                }

                String email = doc.getString("email");
                String phone = doc.getString("phone");

                ConfirmationHelper.deliver(this, channel, currentEvent, email, phone);
            })
            .addOnFailureListener(e ->
                ConfirmationHelper.deliver(
                    this,
                    ConfirmationHelper.CHANNEL_DEVICE,
                    currentEvent,
                    null,
                    null
                )
            );
    }

    private Event buildCurrentEvent() {
        Event e = new Event();
        e.setId(eventId);
        e.setTitle(eventTitle);
        e.setLocation(location);
        e.setTime(time);
        e.setDateTimeMillis(dateTimeMillis);
        e.setAvailableSeats(seats);
        e.setCancelled(cancelled);
        return e;
    }

    private void renderEvent() {
        tvTitle.setText(eventTitle != null ? eventTitle : "");
        tvLocation.setText(location != null ? location : "");
        tvTime.setText(formatWhen());
        tvSeats.setText("Available Seats: " + Math.max(seats, 0));
    }

    private String formatWhen() {
        if (dateTimeMillis > 0) {
            return new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                .format(new Date(dateTimeMillis));
        }
        return time != null ? time : "";
    }

    private void updateActionButtons() {
        boolean signedIn = auth.getCurrentUser() != null;
        boolean hasReservation = existingReservationId != null;

        btnReserve.setVisibility(hasReservation ? View.GONE : View.VISIBLE);
        btnCancelReservation.setVisibility(hasReservation ? View.VISIBLE : View.GONE);

        btnReserve.setEnabled(signedIn && !busy && !cancelled && seats > 0);
        btnCancelReservation.setEnabled(signedIn && !busy && hasReservation);

        if (cancelled && !hasReservation) {
            btnReserve.setText("Event Cancelled");
        } else if (seats <= 0 && !hasReservation) {
            btnReserve.setText("Sold Out");
        } else {
            btnReserve.setText("Reserve Ticket");
        }
    }
}