package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventListAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Events");
        }

        recyclerView = findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int leftRight = (int) (16 * density);
        int top = (int) (140 * density);
        int bottom = (int) (16 * density);

        recyclerView.setPadding(leftRight, top, leftRight, bottom);
        recyclerView.setClipToPadding(false);

        adapter = new EventListAdapter(eventList, event -> {
            Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(this::handleEvents)
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void handleEvents(QuerySnapshot querySnapshot) {
        eventList.clear();

        for (var doc : querySnapshot.getDocuments()) {
            Event event = doc.toObject(Event.class);
            if (event != null) {
                event.setId(doc.getId());
                eventList.add(event);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}