package com.soen345.ticketapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketapp.R;

public class AdminMenuActivity extends AppCompatActivity {

    private Button btnAddEvent;
    private Button btnViewEvents;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnViewEvents = findViewById(R.id.btnViewEvents);
        btnLogout = findViewById(R.id.btnLogout);

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEventActivity.class);
            startActivity(intent);
        });

        btnViewEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventListActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}