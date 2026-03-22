package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketapp.R;
import com.soen345.ticketapp.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken phoneResendToken;

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallbacks =
        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Phone verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                phoneVerificationId = verificationId;
                phoneResendToken = token;
                Toast.makeText(LoginActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (auth.getCurrentUser() != null) {
            routeAfterAuth();
            return;
        }

        binding.toggleAuthMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean email = checkedId == R.id.btnModeEmail;
            binding.layoutEmail.setVisibility(email ? View.VISIBLE : View.GONE);
            binding.layoutPhone.setVisibility(email ? View.GONE : View.VISIBLE);
        });

        binding.btnLogin.setOnClickListener(v -> loginEmail());
        binding.btnRegister.setOnClickListener(v -> registerEmail());
        binding.btnSendCode.setOnClickListener(v -> sendPhoneCode());
        binding.btnVerifyPhone.setOnClickListener(v -> verifyPhoneCode());
    }

    private void loginEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(res -> routeAfterAuth())
            .addOnFailureListener(e ->
                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void registerEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(res -> {
                startActivity(new Intent(this, ProfileSetupActivity.class));
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void sendPhoneCode() {
        String raw = binding.etPhone.getText().toString().trim();
        if (raw.isEmpty()) {
            Toast.makeText(this, "Enter phone number with country code", Toast.LENGTH_SHORT).show();
            return;
        }
        String e164 = raw.startsWith("+") ? raw : "+" + raw.replaceAll("^0+", "");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(e164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneCallbacks)
            .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneCode() {
        if (phoneVerificationId == null) {
            Toast.makeText(this, "Request a code first", Toast.LENGTH_SHORT).show();
            return;
        }
        String code = binding.etSmsCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Enter the SMS code", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener(res -> routeAfterAuth())
            .addOnFailureListener(e ->
                Toast.makeText(this, "Phone sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void routeAfterAuth() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                } else {
                    startActivity(new Intent(this, EventListActivity.class));
                }
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Could not load profile: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
}
