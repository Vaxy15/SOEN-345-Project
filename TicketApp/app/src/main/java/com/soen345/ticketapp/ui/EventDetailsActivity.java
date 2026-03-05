package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityEventDetailsBinding;
import com.soen345.ticketapp.model.Event;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    private ActivityEventDetailsBinding binding;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            finish();
            return;
        }

        loadEvent(eventId);
        binding.btnReserve.setOnClickListener(v -> reserve(eventId));
    }

    private void loadEvent(String eventId) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    Event e = doc.toObject(Event.class);
                    if (e == null) return;

                    binding.tvTitle.setText(e.getTitle());
                    binding.tvLocation.setText(e.getLocation());
                    binding.tvTime.setText(DateFormat.getDateTimeInstance().format(new Date(e.getDateTimeMillis())));
                    binding.tvSeats.setText("Seats: " + e.getAvailableSeats());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void reserve(String eventId) {
        FirebaseUser user = authService.currentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("eventId", eventId);
        reservation.put("userId", user.getUid());
        reservation.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("reservations")
                .add(reservation)
                .addOnSuccessListener(r -> Toast.makeText(this, "Reservation created ✅", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Reserve failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}