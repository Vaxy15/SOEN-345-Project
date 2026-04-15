package com.soen345.ticketapp.ui;

import android.content.Intent;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView  recycler;
    private AdminEventAdapter adapter;
    private List<Event>   events = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        recycler = findViewById(R.id.recyclerAdminEvents);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventAdapter();
        recycler.setAdapter(adapter);

        findViewById(R.id.btnAddEvent).setOnClickListener(v ->
            startActivity(new Intent(this, AdminAddEditEventActivity.class))
        );
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents(); // refresh after add/edit
    }

    private void loadEvents() {
        db.collection("events").get()
            .addOnSuccessListener(snapshot -> {
                events.clear();
                for (QueryDocumentSnapshot doc : snapshot) {
                    Event e = doc.toObject(Event.class);
                    e.setId(doc.getId());
                    events.add(e);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
            );
    }

    private void confirmCancelEvent(Event event, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Event")
            .setMessage("Cancel \"" + event.getTitle() + "\"? All ticket holders will be notified.")
            .setPositiveButton("Cancel Event", (d, w) -> {
                db.collection("events").document(event.getId())
                    .update("cancelled", true)
                    .addOnSuccessListener(v -> {
                        event.setCancelled(true);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Event cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            })
            .setNegativeButton("Keep", null)
            .show();
    }

    // ── Adapter ──────────────────────────────────────────────────────────────
    class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_admin_event, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Event e = events.get(pos);
            h.tvTitle.setText(e.getTitle());
            h.tvLocation.setText(e.getLocation());
            h.tvTime.setText(e.getTime());
            h.tvCancelled.setText(e.isCancelled() ? "CANCELLED" : "Active");
            h.tvCancelled.setTextColor(e.isCancelled() ? 0xFFE53935 : 0xFF43A047);

            h.btnEdit.setEnabled(!e.isCancelled());
            h.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminAddEditEventActivity.class);
                intent.putExtra("eventId", e.getId());
                startActivity(intent);
            });

            h.btnCancel.setEnabled(!e.isCancelled());
            h.btnCancel.setOnClickListener(v -> confirmCancelEvent(e, pos));
        }

        @Override public int getItemCount() { return events.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvLocation, tvTime, tvCancelled;
            Button   btnEdit, btnCancel;
            VH(View v) {
                super(v);
                tvTitle    = v.findViewById(R.id.tvTitle);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvTime     = v.findViewById(R.id.tvTime);
                tvCancelled = v.findViewById(R.id.tvCancelled);
                btnEdit    = v.findViewById(R.id.btnEdit);
                btnCancel  = v.findViewById(R.id.btnCancelEvent);
            }
        }
    }
}
