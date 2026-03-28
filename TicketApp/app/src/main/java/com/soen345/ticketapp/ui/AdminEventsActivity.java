package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityAdminEventsBinding;
import com.soen345.ticketapp.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity implements AdminEventAdapter.Listener {

    private ActivityAdminEventsBinding binding;
    private final AuthService authService = new AuthService();

    private final List<Event> events = new ArrayList<>();
    private AdminEventAdapter adapter;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationIcon(null);
        binding.toolbar.inflateMenu(R.menu.menu_admin_events);
        binding.toolbar.setOnMenuItemClickListener(this::onToolbarMenuItem);

        adapter = new AdminEventAdapter(events, this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(v ->
            startActivity(new Intent(this, AdminEventFormActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            authService.signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        if (authService.currentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private boolean onToolbarMenuItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_public_events) {
            startActivity(new Intent(this, EventListActivity.class));
            return true;
        }
        if (id == R.id.action_sign_out) {
            authService.signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = authService.currentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                if (isFinishing()) return;
                if (!Boolean.TRUE.equals(doc.getBoolean("isOrganizer"))) {
                    Toast.makeText(this, "Organizer access only", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                subscribeEvents();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
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
                    Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
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
                events.sort((a, b) -> Long.compare(a.getDateTimeMillis(), b.getDateTimeMillis()));
                adapter.notifyDataSetChanged();
            });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    @Override
    public void onEdit(@NonNull Event event) {
        Intent i = new Intent(this, AdminEventFormActivity.class);
        i.putExtra(AdminEventFormActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(i);
    }

    @Override
    public void onCancelEvent(@NonNull Event event) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel event")
            .setMessage("Mark \"" + event.getTitle() + "\" as cancelled?")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("Cancel event", (d, w) ->
                FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(event.getId())
                    .update("cancelled", true)
                    .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    ))
            .show();
    }
}
