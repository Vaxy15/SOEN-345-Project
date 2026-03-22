package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.data.BookingRepository;
import com.soen345.ticketapp.databinding.ActivityEventDetailsBinding;
import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.notify.ConfirmationHelper;

import java.text.DateFormat;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity {

    private ActivityEventDetailsBinding binding;
    private final AuthService authService = new AuthService();
    private final BookingRepository bookingRepository = new BookingRepository();

    private Event cachedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

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
                e.setId(doc.getId());
                cachedEvent = e;

                    binding.toolbar.setTitle(e.getTitle());
                    binding.tvTitle.setText(e.getTitle());
                binding.tvCategory.setText(e.getCategory());
                binding.tvLocation.setText(e.getLocation());
                binding.tvTime.setText(DateFormat.getDateTimeInstance().format(new Date(e.getDateTimeMillis())));
                binding.tvSeats.setText("Seats: " + e.getAvailableSeats());

                boolean cancelled = e.getCancelled();
                binding.tvCancelled.setVisibility(cancelled ? View.VISIBLE : View.GONE);
                binding.btnReserve.setEnabled(!cancelled && e.getAvailableSeats() > 0);
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

        bookingRepository.reserveSeat(eventId, user.getUid())
            .addOnSuccessListener(reservationId -> {
                Toast.makeText(this, "Reservation created", Toast.LENGTH_SHORT).show();
                if (cachedEvent != null) {
                    cachedEvent.setAvailableSeats(Math.max(0, cachedEvent.getAvailableSeats() - 1));
                    binding.tvSeats.setText("Seats: " + cachedEvent.getAvailableSeats());
                    binding.btnReserve.setEnabled(!cachedEvent.getCancelled() && cachedEvent.getAvailableSeats() > 0);
                }
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String ch = ConfirmationHelper.CHANNEL_DEVICE;
                        if (doc.exists() && doc.getString("confirmationChannel") != null) {
                            ch = doc.getString("confirmationChannel");
                        }
                        if (cachedEvent != null) {
                            ConfirmationHelper.deliver(
                                this,
                                ch,
                                cachedEvent,
                                user.getEmail(),
                                user.getPhoneNumber()
                            );
                        }
                    });
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Reserve failed", Toast.LENGTH_LONG).show()
            );
    }
}
