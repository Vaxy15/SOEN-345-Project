package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.data.BookingRepository;
import com.soen345.ticketapp.databinding.ActivityMyReservationsBinding;
import com.soen345.ticketapp.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.Listener {

    private ActivityMyReservationsBinding binding;
    private final AuthService authService = new AuthService();
    private final BookingRepository bookingRepository = new BookingRepository();

    private final List<Reservation> reservations = new ArrayList<>();
    private ReservationAdapter adapter;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authService.currentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMyReservationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        adapter = new ReservationAdapter(reservations, this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = authService.currentUser();
        if (user == null) return;

        listener = FirebaseFirestore.getInstance()
            .collection("reservations")
            .whereEqualTo("userId", user.getUid())
            .addSnapshotListener((snap, err) -> {
                if (err != null) {
                    Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                reservations.clear();
                if (snap != null) {
                    snap.getDocuments().forEach(doc -> {
                        Reservation r = doc.toObject(Reservation.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            reservations.add(r);
                        }
                    });
                }
                adapter.notifyDataSetChanged();
            });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) listener.remove();
    }

    @Override
    public void onCancel(@NonNull Reservation reservation) {
        FirebaseUser user = authService.currentUser();
        if (user == null || reservation.getId() == null) return;

        bookingRepository.cancelReservation(reservation.getId(), user.getUid())
            .addOnSuccessListener(unused -> Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e ->
                Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Cancel failed", Toast.LENGTH_LONG).show()
            );
    }
}
