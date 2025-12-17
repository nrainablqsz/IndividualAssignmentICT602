package com.example.electricitybillcalculator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup bottom navigation
        setupBottomNavigation();

        // Set up GitHub link click listener
        TextView textViewGitHub = findViewById(R.id.textViewGitHub);
        textViewGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubLink();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_about);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_calculate) {
                startActivity(new Intent(AboutActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_history) {
                startActivity(new Intent(AboutActivity.this, HistoryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_about) {
                return true;
            }
            return false;
        });
    }

    private void openGitHubLink() {
        String githubUrl = "https://github.com/ainaathirah/ElectricityBillCalculator";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
        startActivity(intent);
    }
}