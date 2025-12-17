package com.example.electricitybillcalculator;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    private TextView textViewMonth;
    private TextView textViewUnits;
    private TextView textViewRebate;
    private TextView textViewTotalCharges;
    private TextView textViewFinalCost;
    private TextView textViewDate;

    private DatabaseHelper dbHelper;
    private long billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_bill_detail);

        // Get bill ID from intent
        billId = getIntent().getLongExtra("BILL_ID", -1);
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Load bill details
        loadBillDetails();

        // Set up back button
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        textViewMonth = findViewById(R.id.textViewMonth);
        textViewUnits = findViewById(R.id.textViewUnits);
        textViewRebate = findViewById(R.id.textViewRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        textViewDate = findViewById(R.id.textViewDate);
    }

    private void loadBillDetails() {
        Cursor cursor = dbHelper.getBillById(billId);

        if (cursor != null && cursor.moveToFirst()) {
            DecimalFormat df = new DecimalFormat("#,##0.00");

            // Get values from cursor
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
            double units = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST));
            String dateCreated = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_CREATED));

            // Format date
            String formattedDate = formatDate(dateCreated);

            // Update UI
            textViewMonth.setText(month);
            textViewUnits.setText(df.format(units) + " kWh");
            textViewRebate.setText(df.format(rebate) + "%");
            textViewTotalCharges.setText("RM " + df.format(totalCharges));
            textViewFinalCost.setText("RM " + df.format(finalCost));
            textViewDate.setText(formattedDate);

            cursor.close();
        } else {
            Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}