package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityEventListBinding;
import com.soen345.ticketapp.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity implements EventListAdapter.OnEventClick {

    private ActivityEventListBinding binding;
    private final AuthService authService = new AuthService();

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> displayEvents = new ArrayList<>();
    private EventListAdapter adapter;
    private ListenerRegistration listener;

    private Long filterFromStartMillis;
    private Long filterToEndMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authService.currentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = new EventListAdapter(displayEvents, this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.event_categories,
            android.R.layout.simple_spinner_item
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(catAdapter);
        binding.spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        binding.etSearch.addTextChangedListener(tw);
        binding.etFilterLocation.addTextChangedListener(tw);

        binding.btnFromDate.setOnClickListener(v -> pickDate(true));
        binding.btnToDate.setOnClickListener(v -> pickDate(false));
        binding.btnClearFilters.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.etFilterLocation.setText("");
            binding.spCategory.setSelection(0);
            filterFromStartMillis = null;
            filterToEndMillis = null;
            applyFilters();
        });

        binding.btnMyReservations.setOnClickListener(v ->
            startActivity(new Intent(this, MyReservationsActivity.class)));

        binding.btnAdmin.setOnClickListener(v ->
            startActivity(new Intent(this, AdminEventsActivity.class)));

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
        if (authService.currentUser() == null) {
            return;
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(authService.currentUser().getUid())
            .get()
            .addOnSuccessListener(doc -> {
                if (isFinishing()) return;
                if (!doc.exists()) {
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                    finish();
                    return;
                }
                loadOrganizerFlag();
                subscribeEvents();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Could not verify profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void subscribeEvents() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
        listener = FirebaseFirestore.getInstance()
            .collection("events")
            .addSnapshotListener((snap, err) -> {
                if (err != null) {
                    Toast.makeText(this, "Firestore error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                allEvents.clear();
                if (snap != null) {
                    snap.getDocuments().forEach(doc -> {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            e.setId(doc.getId());
                            if (!e.getCancelled()) {
                                allEvents.add(e);
                            }
                        }
                    });
                }
                applyFilters();
            });
    }

    private void loadOrganizerFlag() {
        if (authService.currentUser() == null) return;
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(authService.currentUser().getUid())
            .get()
            .addOnSuccessListener(doc -> {
                boolean org = Boolean.TRUE.equals(doc.getBoolean("isOrganizer"));
                binding.btnAdmin.setVisibility(org ? View.VISIBLE : View.GONE);
                binding.btnSeed.setVisibility(org ? View.VISIBLE : View.GONE);
            });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) listener.remove();
    }

    private void pickDate(boolean isFrom) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(isFrom ? getString(R.string.filter_date_from) : getString(R.string.filter_date_to))
            .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (isFrom) {
                filterFromStartMillis = startOfDay(selection);
            } else {
                filterToEndMillis = endOfDay(selection);
            }
            applyFilters();
        });
        picker.show(getSupportFragmentManager(), isFrom ? "from" : "to");
    }

    private static long startOfDay(long utcMillisFromPicker) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(utcMillisFromPicker);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long endOfDay(long utcMillisFromPicker) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(utcMillisFromPicker);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private void applyFilters() {
        String q = binding.etSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        String loc = binding.etFilterLocation.getText().toString().trim().toLowerCase(Locale.getDefault());
        int catPos = binding.spCategory.getSelectedItemPosition();
        String[] cats = getResources().getStringArray(R.array.event_categories);
        String catFilter = (catPos <= 0 || catPos >= cats.length) ? null : cats[catPos];

        displayEvents.clear();
        for (Event e : allEvents) {
            if (!q.isEmpty() && !e.getTitle().toLowerCase(Locale.getDefault()).contains(q)) {
                continue;
            }
            if (!loc.isEmpty() && !e.getLocation().toLowerCase(Locale.getDefault()).contains(loc)) {
                continue;
            }
            if (catFilter != null && !catFilter.equalsIgnoreCase(e.getCategory())) {
                continue;
            }
            if (filterFromStartMillis != null && e.getDateTimeMillis() < filterFromStartMillis) {
                continue;
            }
            if (filterToEndMillis != null && e.getDateTimeMillis() > filterToEndMillis) {
                continue;
            }
            displayEvents.add(e);
        }
        adapter.notifyDataSetChanged();
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

        db.collection("events").add(new Event("Concert Night", "Downtown Hall", "Music", now + 86400000L, 120));
        db.collection("events").add(new Event("Varsity Game", "University Stadium", "Sports", now + 172800000L, 800));
        db.collection("events").add(new Event("Indie Film Night", "Campus Theatre", "Movies", now + 259200000L, 80));
        db.collection("events").add(new Event("City Tour", "Old Port", "Travel", now + 345600000L, 40));

        Toast.makeText(this, R.string.seed_events_done, Toast.LENGTH_SHORT).show();
    }
}
