package com.example.electricitybillcalculator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_about);

        setupBottomNavigation();

        setupGitHubLink();
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

    private void setupGitHubLink() {
        TextView textViewGitHub = findViewById(R.id.textViewGitHub);

        textViewGitHub.setPaintFlags(textViewGitHub.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        textViewGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubLink();
            }
        });

        textViewGitHub.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Change color when pressed
                        textViewGitHub.setTextColor(Color.parseColor("#FF4081"));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Restore original color
                        textViewGitHub.setTextColor(getResources().getColor(R.color.secondary_color));
                        break;
                }
                return false;
            }
        });
    }

    private void openGitHubLink() {
        String githubUrl = "https://github.com/nrainablqsz/IndividualAssignmentICT602";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(githubUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open browser. Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }
}