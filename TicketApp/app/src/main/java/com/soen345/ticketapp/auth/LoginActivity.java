package com.soen345.ticketapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.ui.AdminDashboardActivity;
import com.soen345.ticketapp.ui.EventListActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin, btnRegister;
    private FirebaseAuth auth;

    // Hardcoded admin email — in production, check a Firestore "role" field instead
    private static final String ADMIN_EMAIL = "admin@ticketapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Skip login if already signed in
        if (auth.getCurrentUser() != null) {
            navigateByRole(auth.getCurrentUser().getEmail());
            return;
        }

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd   = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) { etEmail.setError("Required");    return; }
        if (TextUtils.isEmpty(pwd))   { etPassword.setError("Required"); return; }

        btnLogin.setEnabled(false);
        auth.signInWithEmailAndPassword(email, pwd)
            .addOnSuccessListener(result -> navigateByRole(email))
            .addOnFailureListener(e -> {
                btnLogin.setEnabled(true);
                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void navigateByRole(String email) {
        Intent intent = ADMIN_EMAIL.equalsIgnoreCase(email)
            ? new Intent(this, AdminDashboardActivity.class)
            : new Intent(this, EventListActivity.class);
        startActivity(intent);
        finish();
    }
}
