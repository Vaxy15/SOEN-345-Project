package com.soen345.ticketapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.ui.EventListActivity;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private RadioGroup    rgMethod;
    private RadioButton   rbEmail, rbPhone;
    private EditText      etEmailOrPhone, etPassword, etConfirmPassword;
    private Button        btnRegister, btnBack;

    private FirebaseAuth      auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        rgMethod        = findViewById(R.id.rgMethod);
        rbEmail         = findViewById(R.id.rbEmail);
        rbPhone         = findViewById(R.id.rbPhone);
        etEmailOrPhone  = findViewById(R.id.etEmailOrPhone);
        etPassword      = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister     = findViewById(R.id.btnRegister);
        btnBack         = findViewById(R.id.btnBack);

        rgMethod.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isEmail = checkedId == R.id.rbEmail;
            etEmailOrPhone.setHint(isEmail ? "Email address" : "Phone number (+1XXXXXXXXXX)");
            etPassword.setVisibility(isEmail ? View.VISIBLE : View.GONE);
            etConfirmPassword.setVisibility(isEmail ? View.VISIBLE : View.GONE);
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnBack.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String input = etEmailOrPhone.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            etEmailOrPhone.setError("Required");
            return;
        }

        if (rbEmail.isChecked()) {
            registerWithEmail(input);
        } else {
            registerWithPhone(input);
        }
    }

    private void registerWithEmail(String email) {
        String pwd    = etPassword.getText().toString();
        String pwdCon = etConfirmPassword.getText().toString();

        if (pwd.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return;
        }
        if (!pwd.equals(pwdCon)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnRegister.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();
                saveUserProfile(uid, email, "email");
            })
            .addOnFailureListener(e -> {
                btnRegister.setEnabled(true);
                Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void registerWithPhone(String phone) {
        // Phone auth requires SMS verification — for now store as unverified profile
        // In production, replace with FirebaseAuth PhoneAuthProvider flow
        Toast.makeText(this,
            "Phone registration: SMS verification would be triggered here.",
            Toast.LENGTH_LONG).show();

        // Placeholder: navigate back so the user knows the flow exists
        // Wire up PhoneAuthProvider.verifyPhoneNumber() here when ready
    }

    private void saveUserProfile(String uid, String contact, String method) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid",     uid);
        user.put("contact", contact);
        user.put("method",  method);
        user.put("role",    "customer");

        db.collection("users").document(uid).set(user)
            .addOnSuccessListener(v -> {
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EventListActivity.class));
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
}
