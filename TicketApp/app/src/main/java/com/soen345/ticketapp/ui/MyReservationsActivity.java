package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.data.BookingRepository;
import com.soen345.ticketapp.model.Reservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private TextView tvEmpty;
    private ReservationAdapter adapter;
    private final List<Reservation> reservations = new ArrayList<>();

    private FirebaseFirestore db;
    private BookingRepository bookingRepository;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        bookingRepository = new BookingRepository();
        uid = user.getUid();

        recycler = findViewById(R.id.recyclerReservations);
        tvEmpty = findViewById(R.id.tvEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter();
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadReservations();
    }

    private void loadReservations() {
        db.collection("reservations")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener(snapshot -> {
                reservations.clear();
                for (QueryDocumentSnapshot doc : snapshot) {
                    Reservation r = doc.toObject(Reservation.class);
                    r.setId(doc.getId());
                    reservations.add(r);
                }

                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load reservations", Toast.LENGTH_SHORT).show()
            );
    }

    private void cancelReservation(Reservation reservation, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Reservation")
            .setMessage("Cancel your ticket for \"" + reservation.getEventTitle() + "\"?")
            .setPositiveButton("Yes, cancel", (d, w) -> {
                bookingRepository.cancelReservation(reservation.getId(), uid)
                    .addOnSuccessListener(v -> {
                        if (position >= 0 && position < reservations.size()) {
                            reservations.remove(position);
                            adapter.notifyItemRemoved(position);
                        } else {
                            loadReservations();
                        }

                        tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        String message = e.getMessage() != null ? e.getMessage() : "Cancellation failed";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    });
            })
            .setNegativeButton("Keep it", null)
            .show();
    }

    class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_reservation, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Reservation r = reservations.get(pos);

            h.tvTitle.setText(r.getEventTitle() != null ? r.getEventTitle() : "Event");
            h.tvLocation.setText(r.getEventLocation() != null ? r.getEventLocation() : "");

            h.tvTime.setText(
                r.getEventDateTimeMillis() > 0
                    ? new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                        .format(new Date(r.getEventDateTimeMillis()))
                    : ""
            );

            h.tvReservedAt.setText(
                "Booked: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(r.getCreatedAt()))
            );

            h.btnCancel.setOnClickListener(v -> {
                int adapterPosition = h.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    cancelReservation(r, adapterPosition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return reservations.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvLocation, tvTime, tvReservedAt;
            Button btnCancel;

            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvTime = v.findViewById(R.id.tvTime);
                tvReservedAt = v.findViewById(R.id.tvReservedAt);
                btnCancel = v.findViewById(R.id.btnCancel);
            }
        }
    }
}