package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityEventListBinding;
import com.soen345.ticketapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements EventListAdapter.OnEventClick {

    private ActivityEventListBinding binding;
    private final AuthService authService = new AuthService();

    private final List<Event> events = new ArrayList<>();
    private EventListAdapter adapter;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // QUICK CONFIRM YOU'RE HERE
        Toast.makeText(this, "EventListActivity", Toast.LENGTH_SHORT).show();

        if (authService.currentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // <-- THIS is what makes the screen not blank

        adapter = new EventListAdapter(events, this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnLogout.setOnClickListener(v -> {
            authService.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.btnSeed.setOnClickListener(v -> seedSampleEvents());
    }

    @Override
    protected void onStart() {
        super.onStart();

        listener = FirebaseFirestore.getInstance()
                .collection("events")
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(this, "Firestore error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    events.clear();
                    if (snap != null) {
                        snap.getDocuments().forEach(doc -> {
                            Event e = doc.toObject(Event.class);
                            if (e != null) {
                                e.setId(doc.getId());
                                events.add(e);
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
    public void onClick(Event event) {
        Intent i = new Intent(this, EventDetailsActivity.class);
        i.putExtra("eventId", event.getId());
        startActivity(i);
    }

    private void seedSampleEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();

        db.collection("events").add(new Event("Concert Night", "Downtown Hall", now + 86400000L, 120));
        db.collection("events").add(new Event("Comedy Show", "Campus Theatre", now + 172800000L, 80));
        db.collection("events").add(new Event("Tech Talk", "Room H-110", now + 259200000L, 200));

        Toast.makeText(this, "Seeded sample events ✅", Toast.LENGTH_SHORT).show();
    }
}