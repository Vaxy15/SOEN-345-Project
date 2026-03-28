package com.soen345.ticketapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.databinding.ActivityProfileSetupBinding;
import com.soen345.ticketapp.notify.ConfirmationHelper;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    public static final String EXTRA_REQUIRE_ORGANIZER = "require_organizer";

    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private ActivityProfileSetupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.confirmation_options,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spConfirmation.setAdapter(adapter);

        FirebaseUser existing = FirebaseAuth.getInstance().getCurrentUser();
        if (existing != null) {
            if (existing.getEmail() != null) {
                binding.etConfirmationEmail.setText(existing.getEmail());
            }
            if (existing.getPhoneNumber() != null) {
                binding.etConfirmationPhone.setText(existing.getPhoneNumber());
            }
        }

        boolean requireOrganizer = getIntent().getBooleanExtra(EXTRA_REQUIRE_ORGANIZER, false);
        if (requireOrganizer) {
            binding.cbOrganizer.setChecked(true);
            binding.cbOrganizer.setEnabled(false);
            binding.tvOrganizerLockedHint.setVisibility(View.VISIBLE);
        } else {
            binding.cbOrganizer.setVisibility(View.GONE);
            binding.tvOrganizerLockedHint.setVisibility(View.GONE);
        }

        binding.btnContinue.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        maybeRequestNotificationPermission(() -> persistUser(user));
    }

    private void maybeRequestNotificationPermission(@NonNull Runnable afterGranted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            afterGranted.run();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            afterGranted.run();
            return;
        }
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.POST_NOTIFICATIONS},
            REQ_POST_NOTIFICATIONS
        );
        pendingAfterNotificationPermission = afterGranted;
    }

    private Runnable pendingAfterNotificationPermission;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS && pendingAfterNotificationPermission != null) {
            Runnable r = pendingAfterNotificationPermission;
            pendingAfterNotificationPermission = null;
            r.run();
        }
    }

    private void persistUser(FirebaseUser user) {
        String[] values = getResources().getStringArray(R.array.confirmation_values);
        int idx = binding.spConfirmation.getSelectedItemPosition();
        if (idx < 0 || idx >= values.length) idx = 0;
        String channel = values[idx];

        String emailInput = binding.etConfirmationEmail.getText().toString().trim();
        String phoneInput = binding.etConfirmationPhone.getText().toString().trim();
        String resolvedEmail = !emailInput.isEmpty() ? emailInput
            : (user.getEmail() != null ? user.getEmail().trim() : "");
        String resolvedPhone = !phoneInput.isEmpty() ? phoneInput
            : (user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "");

        if (ConfirmationHelper.CHANNEL_EMAIL.equals(channel) && resolvedEmail.isEmpty()) {
            Toast.makeText(this, R.string.profile_confirm_need_email, Toast.LENGTH_LONG).show();
            return;
        }
        if (ConfirmationHelper.CHANNEL_SMS.equals(channel) && resolvedPhone.isEmpty()) {
            Toast.makeText(this, R.string.profile_confirm_need_phone, Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("confirmationChannel", channel);
        data.put("isOrganizer", binding.cbOrganizer.isChecked());
        if (!resolvedEmail.isEmpty()) {
            data.put("email", resolvedEmail);
        }
        if (!resolvedPhone.isEmpty()) {
            data.put("phone", resolvedPhone);
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .set(data)
            .addOnSuccessListener(unused -> {
                Class<?> next = binding.cbOrganizer.isChecked()
                    ? AdminEventsActivity.class
                    : EventListActivity.class;
                startActivity(new Intent(this, next));
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Could not save profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
}
