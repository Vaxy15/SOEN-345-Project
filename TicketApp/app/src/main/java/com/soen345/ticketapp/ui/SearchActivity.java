package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText  etLocation;
    private Spinner   spinnerCategory;
    private Button    btnSearch, btnClear, btnBack;
    private RecyclerView recycler;
    private TextView  tvNoResults;

    private EventAdapter     adapter;
    private List<Event>      results = new ArrayList<>();
    private FirebaseFirestore db;

    private static final String[] CATEGORIES =
            {"All", "Movie", "Concert", "Sports", "Travel", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        etLocation      = findViewById(R.id.etLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSearch       = findViewById(R.id.btnSearch);
        btnClear        = findViewById(R.id.btnClear);
        btnBack         = findViewById(R.id.btnBack);
        recycler        = findViewById(R.id.recyclerResults);
        tvNoResults     = findViewById(R.id.tvNoResults);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter();
        recycler.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> performSearch());
        btnClear.setOnClickListener(v -> clearFilters());
        btnBack.setOnClickListener(v -> finish());
    }

    private void performSearch() {
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        Query query = db.collection("events").whereEqualTo("cancelled", false);
        if (!category.equals("All")) query = query.whereEqualTo("category", category);

        query.get().addOnSuccessListener(snapshot -> {
            results.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                Event e = doc.toObject(Event.class);
                e.setId(doc.getId());
                if (!TextUtils.isEmpty(location) &&
                        !e.getLocation().toLowerCase().contains(location.toLowerCase())) continue;
                results.add(e);
            }
            adapter.notifyDataSetChanged();
            tvNoResults.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show()
        );
    }

    private void clearFilters() {
        etLocation.setText("");
        spinnerCategory.setSelection(0);
        results.clear();
        adapter.notifyDataSetChanged();
        tvNoResults.setVisibility(View.GONE);
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
            Event e = results.get(pos);
            h.tvTitle.setText(e.getTitle());
            h.tvLocation.setText(e.getLocation());
            h.tvTime.setText(new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                    .format(new Date(e.getDateTimeMillis())));
            h.tvSeats.setText("Seats: " + e.getAvailableSeats());

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId",        e.getId());
                intent.putExtra("eventTitle",     e.getTitle());
                intent.putExtra("location",       e.getLocation());
                intent.putExtra("dateTimeMillis", e.getDateTimeMillis());
                intent.putExtra("seats",          e.getAvailableSeats());
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return results.size(); }

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