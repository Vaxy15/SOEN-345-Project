package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final AuthService authService = new AuthService();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TEMPORARY FOR TESTING:
        // force sign out every time app opens, so you always see login screen
        authService.getAuth().signOut();

        binding.rgRole.setVisibility(View.GONE);

        binding.btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Login button clicked", Toast.LENGTH_SHORT).show();
            login();
        });

        binding.btnRegister.setOnClickListener(v -> {
            if (!isRegisterMode) {
                isRegisterMode = true;
                binding.rgRole.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Select Admin or User, then press Register again", Toast.LENGTH_SHORT).show();
            } else {
                register();
            }
        });
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Trying Firebase login...", Toast.LENGTH_SHORT).show();

        authService.getAuth()
                .signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    Toast.makeText(this, "Firebase login success", Toast.LENGTH_SHORT).show();
                    String uid = res.getUser().getUid();
                    redirectUserByRole(uid);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void register() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedRole = getSelectedRole();
        if (selectedRole == null) {
            Toast.makeText(this, "Please select Admin or User", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.getAuth()
                .createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("role", selectedRole);

                    db.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                resetRegisterMode();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String getSelectedRole() {
        int checkedId = binding.rgRole.getCheckedRadioButtonId();

        if (checkedId == binding.rbAdmin.getId()) {
            return "admin";
        } else if (checkedId == binding.rbUser.getId()) {
            return "user";
        }

        return null;
    }

    private void resetRegisterMode() {
        isRegisterMode = false;
        binding.rgRole.clearCheck();
        binding.rgRole.setVisibility(View.GONE);
        binding.etEmail.setText("");
        binding.etPassword.setText("");

        authService.getAuth().signOut();
    }

    private void redirectUserByRole(String uid) {
        Toast.makeText(this, "Checking role...", Toast.LENGTH_SHORT).show();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "User role not found in Firestore", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String role = documentSnapshot.getString("role");

                    if ("admin".equalsIgnoreCase(role)) {
                        Toast.makeText(this, "Admin detected", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminMenuActivity.class));
                        finish();
                    } else if ("user".equalsIgnoreCase(role)) {
                        Toast.makeText(this, "User login successful. User page not implemented yet.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Unknown user role", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get role: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}