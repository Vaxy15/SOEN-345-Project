package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketapp.auth.AuthService;
import com.soen345.ticketapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (authService.currentUser() != null) {
            goToEvents();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> login());
        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.getAuth()
                .signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> goToEvents())
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

        authService.getAuth()
                .createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> goToEvents())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void goToEvents() {
        startActivity(new Intent(this, EventListActivity.class));
        finish();
    }
}