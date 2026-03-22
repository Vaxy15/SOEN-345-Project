package com.soen345.ticketapp.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.databinding.ActivityAdminEventFormBinding;
import com.soen345.ticketapp.model.Event;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminEventFormActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "eventId";

    private ActivityAdminEventFormBinding binding;
    private Long selectedMillis;
    private Integer pendingYear;
    private Integer pendingMonth;
    private Integer pendingDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEventFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.event_categories_picker,
            android.R.layout.simple_spinner_item
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(catAdapter);

        binding.btnPickWhen.setOnClickListener(v -> openDateThenTime());
        binding.btnSave.setOnClickListener(v -> save());

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId != null) {
            binding.toolbar.setTitle(R.string.toolbar_admin_form_edit);
            load(eventId);
        } else {
            selectedMillis = System.currentTimeMillis() + 86400000L;
            binding.tvWhen.setText(DateFormat.getDateTimeInstance().format(new Date(selectedMillis)));
        }
    }

    private void load(String eventId) {
        FirebaseFirestore.getInstance()
            .collection("events")
            .document(eventId)
            .get()
            .addOnSuccessListener(doc -> {
                Event e = doc.toObject(Event.class);
                if (e == null) return;
                binding.etTitle.setText(e.getTitle());
                binding.etLocation.setText(e.getLocation());
                binding.etSeats.setText(String.valueOf(e.getAvailableSeats()));
                selectedMillis = e.getDateTimeMillis();
                binding.tvWhen.setText(DateFormat.getDateTimeInstance().format(new Date(selectedMillis)));

                String[] cats = getResources().getStringArray(R.array.event_categories_picker);
                for (int i = 0; i < cats.length; i++) {
                    if (cats[i].equalsIgnoreCase(e.getCategory())) {
                        binding.spCategory.setSelection(i);
                        break;
                    }
                }
            })
            .addOnFailureListener(ex ->
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void openDateThenTime() {
        com.google.android.material.datepicker.MaterialDatePicker<Long> dp =
            com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date_time)
                .build();
        dp.addOnPositiveButtonClickListener(selection -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(selection);
            pendingYear = c.get(java.util.Calendar.YEAR);
            pendingMonth = c.get(java.util.Calendar.MONTH);
            pendingDay = c.get(java.util.Calendar.DAY_OF_MONTH);

            com.google.android.material.timepicker.MaterialTimePicker tp =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                    .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                    .setHour(19)
                    .setMinute(0)
                    .build();
            tp.addOnPositiveButtonClickListener(v -> {
                java.util.Calendar cc = java.util.Calendar.getInstance();
                cc.set(pendingYear, pendingMonth, pendingDay, tp.getHour(), tp.getMinute(), 0);
                cc.set(java.util.Calendar.MILLISECOND, 0);
                selectedMillis = cc.getTimeInMillis();
                binding.tvWhen.setText(DateFormat.getDateTimeInstance().format(new Date(selectedMillis)));
            });
            tp.show(getSupportFragmentManager(), "admin_time");
        });
        dp.show(getSupportFragmentManager(), "admin_date");
    }

    private void save() {
        String title = binding.etTitle.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String seatsRaw = binding.etSeats.getText().toString().trim();
        if (title.isEmpty() || location.isEmpty() || seatsRaw.isEmpty() || selectedMillis == null) {
            Toast.makeText(this, "Fill all fields and pick date/time", Toast.LENGTH_SHORT).show();
            return;
        }
        int seats;
        try {
            seats = Integer.parseInt(seatsRaw);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid seat count", Toast.LENGTH_SHORT).show();
            return;
        }
        if (seats < 0) {
            Toast.makeText(this, "Seats cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] cats = getResources().getStringArray(R.array.event_categories_picker);
        int idx = binding.spCategory.getSelectedItemPosition();
        String category = (idx >= 0 && idx < cats.length) ? cats[idx] : "Other";

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("location", location);
        data.put("category", category);
        data.put("availableSeats", seats);
        data.put("dateTimeMillis", selectedMillis);

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null) {
            data.put("cancelled", false);
            FirebaseFirestore.getInstance().collection("events")
                .add(data)
                .addOnSuccessListener(ref -> finish())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            FirebaseFirestore.getInstance().collection("events")
                .document(eventId)
                .update(data)
                .addOnSuccessListener(unused -> finish())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
