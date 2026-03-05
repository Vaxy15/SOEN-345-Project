package com.soen345.ticketapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.soen345.ticketapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.statusText.setText("TicketApp scaffold is running ✅");
    }
}