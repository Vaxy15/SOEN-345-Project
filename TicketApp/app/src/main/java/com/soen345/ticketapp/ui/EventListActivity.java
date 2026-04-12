package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.auth.LoginActivity;
import com.soen345.ticketapp.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private EventAdapter adapter;
    private List<Event>  events = new ArrayList<>();

    private FirebaseAuth      auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter();
        recycler.setAdapter(adapter);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btnSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class))
        );

        findViewById(R.id.btnMyReservations).setOnClickListener(v ->
                startActivity(new Intent(this, MyReservationsActivity.class))
        );

        loadEvents();
    }

    private void loadEvents() {
        db.collection("events")
                .whereEqualTo("cancelled", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    events.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());
                        events.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private static String formatDate(long millis) {
        return new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                .format(new Date(millis));
    }

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_event, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Event e = events.get(pos);
            h.tvTitle.setText(e.getTitle());
            h.tvLocation.setText(e.getLocation());
            h.tvTime.setText(e.getTime());
            h.tvSeats.setText("Seats: " + e.getAvailableSeats());

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId",          e.getId());
                intent.putExtra("eventTitle",       e.getTitle());
                intent.putExtra("location",         e.getLocation());
                intent.putExtra("dateTimeMillis",   e.getDateTimeMillis()); // long
                intent.putExtra("seats",            e.getAvailableSeats());
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return events.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvLocation, tvTime, tvSeats;
            VH(View v) {
                super(v);
                tvTitle    = v.findViewById(R.id.tvTitle);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvTime     = v.findViewById(R.id.tvTime);
                tvSeats    = v.findViewById(R.id.tvSeats);
            }
        }
    }
}