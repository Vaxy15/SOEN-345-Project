package com.soen345.ticketapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

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

        Map<String, Object> data = new HashMap<>();
        data.put("confirmationChannel", channel);
        data.put("isOrganizer", binding.cbOrganizer.isChecked());
        if (user.getEmail() != null) {
            data.put("email", user.getEmail());
        }
        if (user.getPhoneNumber() != null) {
            data.put("phone", user.getPhoneNumber());
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .set(data)
            .addOnSuccessListener(unused -> {
                startActivity(new Intent(this, EventListActivity.class));
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Could not save profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
}
