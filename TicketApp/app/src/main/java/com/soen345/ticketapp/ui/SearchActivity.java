package com.soen345.ticketapp.ui;

import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etLocation;
    private Spinner spinnerCategory;
    private Button btnFromDate;
    private Button btnToDate;
    private Button btnSearch;
    private Button btnClear;
    private Button btnBack;
    private RecyclerView recycler;
    private TextView tvNoResults;

    private EventAdapter adapter;
    private final List<Event> results = new ArrayList<>();
    private FirebaseFirestore db;

    private Long fromDateMillis = null;
    private Long toDateMillis = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);
        recycler = findViewById(R.id.recyclerResults);
        tvNoResults = findViewById(R.id.tvNoResults);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.event_categories,
            android.R.layout.simple_spinner_item
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter();
        recycler.setAdapter(adapter);

        btnFromDate.setOnClickListener(v -> openDatePicker(true));
        btnToDate.setOnClickListener(v -> openDatePicker(false));
        btnSearch.setOnClickListener(v -> performSearch());
        btnClear.setOnClickListener(v -> clearFilters());
        btnBack.setOnClickListener(v -> finish());
    }

    private void openDatePicker(boolean isFromDate) {
        Calendar c = Calendar.getInstance();

        Long current = isFromDate ? fromDateMillis : toDateMillis;
        if (current != null) {
            c.setTimeInMillis(current);
        }

        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth, 0, 0, 0);
                chosen.set(Calendar.MILLISECOND, 0);

                if (isFromDate) {
                    fromDateMillis = chosen.getTimeInMillis();
                    btnFromDate.setText(formatDateOnly(fromDateMillis));
                } else {
                    toDateMillis = chosen.getTimeInMillis();
                    btnToDate.setText(formatDateOnly(chosen.getTimeInMillis()));
                }
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void performSearch() {
        String title = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (fromDateMillis != null && toDateMillis != null && fromDateMillis > toDateMillis) {
            Toast.makeText(this, "From date must be before To date", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = db.collection("events").whereEqualTo("cancelled", false);

        query.get()
            .addOnSuccessListener(snapshot -> {
                results.clear();

                long start = fromDateMillis != null ? startOfDay(fromDateMillis) : Long.MIN_VALUE;
                long end = toDateMillis != null ? endOfDay(toDateMillis) : Long.MAX_VALUE;

                for (QueryDocumentSnapshot doc : snapshot) {
                    Event e = doc.toObject(Event.class);
                    e.setId(doc.getId());

                    if (!matchesTitle(e, title)) continue;
                    if (!matchesLocation(e, location)) continue;
                    if (!matchesCategory(e, category)) continue;
                    if (!matchesDateRange(e, start, end)) continue;

                    results.add(e);
                }

                adapter.notifyDataSetChanged();
                tvNoResults.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }

    private boolean matchesTitle(Event e, String title) {
        if (TextUtils.isEmpty(title)) return true;
        return e.getTitle() != null
            && e.getTitle().toLowerCase(Locale.getDefault())
                .contains(title.toLowerCase(Locale.getDefault()));
    }

    private boolean matchesLocation(Event e, String location) {
        if (TextUtils.isEmpty(location)) return true;
        return e.getLocation() != null
            && e.getLocation().toLowerCase(Locale.getDefault())
                .contains(location.toLowerCase(Locale.getDefault()));
    }

    private boolean matchesCategory(Event e, String category) {
        return "All".equals(category) || category.equalsIgnoreCase(e.getCategory());
    }

    private boolean matchesDateRange(Event e, long start, long end) {
        long when = e.getDateTimeMillis();
        if (when <= 0) {
            return false;
        }
        return when >= start && when <= end;
    }

    private void clearFilters() {
        etTitle.setText("");
        etLocation.setText("");
        spinnerCategory.setSelection(0);

        fromDateMillis = null;
        toDateMillis = null;
        btnFromDate.setText("From date");
        btnToDate.setText("To date");

        results.clear();
        adapter.notifyDataSetChanged();
        tvNoResults.setVisibility(View.GONE);
    }

    private static String formatDateOnly(long millis) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(new Date(millis));
    }

    private static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long endOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

        @NonNull
        @Override
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
            h.tvTime.setText(
                new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
                    .format(new Date(e.getDateTimeMillis()))
            );
            h.tvSeats.setText("Seats: " + e.getAvailableSeats());

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", e.getId());
                intent.putExtra("eventTitle", e.getTitle());
                intent.putExtra("location", e.getLocation());
                intent.putExtra("dateTimeMillis", e.getDateTimeMillis());
                intent.putExtra("seats", e.getAvailableSeats());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvLocation, tvTime, tvSeats;

            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvTime = v.findViewById(R.id.tvTime);
                tvSeats = v.findViewById(R.id.tvSeats);
            }
        }
    }
}