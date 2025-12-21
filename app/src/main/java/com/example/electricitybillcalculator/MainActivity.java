package com.example.electricitybillcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private RadioGroup rebateRadioGroup;
    private TextView textViewTotalCharges;
    private TextView textViewRebateApplied;
    private TextView textViewFinalCost;
    private Button buttonCalculate;
    private Button buttonSave;
    private CardView cardResults;

    private DatabaseHelper dbHelper;
    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    private static final double RATE_FIRST_200 = 0.218; // 21.8 sen per kWh
    private static final double RATE_NEXT_100 = 0.334;  // 33.4 sen per kWh
    private static final double RATE_NEXT_300 = 0.516;  // 51.6 sen per kWh
    private static final double RATE_ABOVE_600 = 0.546; // 54.6 sen per kWh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();

        setupMonthSpinner();

        setupBottomNavigation();

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBill();
            }
        });

        rebateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Update rebate percentage based on selected radio button
                if (checkedId == R.id.rebate0) {
                    rebatePercentage = 0;
                } else if (checkedId == R.id.rebate1) {
                    rebatePercentage = 1;
                } else if (checkedId == R.id.rebate2) {
                    rebatePercentage = 2;
                } else if (checkedId == R.id.rebate3) {
                    rebatePercentage = 3;
                } else if (checkedId == R.id.rebate4) {
                    rebatePercentage = 4;
                } else if (checkedId == R.id.rebate5) {
                    rebatePercentage = 5;
                }

                if (cardResults.getVisibility() == View.VISIBLE) {
                    calculateFinalCost();
                }
            }
        });
    }

    private void initViews() {
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewRebateApplied = findViewById(R.id.textViewRebateApplied);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        cardResults = findViewById(R.id.cardResults);
    }

    private void setupMonthSpinner() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                months
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Set current month as default
        int currentMonth = new Date().getMonth();
        spinnerMonth.setSelection(currentMonth);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_calculate) {
                // Already in calculate page
                return true;
            } else if (itemId == R.id.navigation_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            }
            return false;
        });
    }

    private void calculateBill() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = editTextUnits.getText().toString().trim(); // FIXED: Added this line

        if (TextUtils.isEmpty(unitsStr)) {
            editTextUnits.setError("Please enter electricity units");
            editTextUnits.requestFocus();
            return;
        }

        try {
            double units = Double.parseDouble(unitsStr);

            if (units < 1) {
                editTextUnits.setError("Units must be at least 1 kWh");
                editTextUnits.requestFocus();
                return;
            }

            if (units > 1000) {
                editTextUnits.setError("Units cannot exceed 1000 kWh");
                editTextUnits.requestFocus();
                return;
            }

            totalCharges = calculateTotalCharges(units);

            calculateFinalCost();

            cardResults.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Calculation complete! Click Save to store in history.", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            editTextUnits.setError("Please enter a valid number (e.g., 250)");
            editTextUnits.requestFocus();
        }
    }

    private double calculateTotalCharges(double units) {
        double charges = 0;

        if (units <= 200) {
            charges = units * RATE_FIRST_200;
        } else if (units <= 300) {
            charges = (200 * RATE_FIRST_200) +
                    ((units - 200) * RATE_NEXT_100);
        } else if (units <= 600) {
            charges = (200 * RATE_FIRST_200) +
                    (100 * RATE_NEXT_100) +
                    ((units - 300) * RATE_NEXT_300);
        } else {
            charges = (200 * RATE_FIRST_200) +
                    (100 * RATE_NEXT_100) +
                    (300 * RATE_NEXT_300) +
                    ((units - 600) * RATE_ABOVE_600);
        }

        return Math.round(charges * 100.0) / 100.0;
    }

    private void calculateFinalCost() {
        double rebateAmount = totalCharges * (rebatePercentage / 100.0);
        finalCost = totalCharges - rebateAmount;
        finalCost = Math.round(finalCost * 100.0) / 100.0;

        DecimalFormat df = new DecimalFormat("#,##0.00");

        textViewTotalCharges.setText("RM " + df.format(totalCharges));
        textViewRebateApplied.setText(rebatePercentage + "%");
        textViewFinalCost.setText("RM " + df.format(finalCost));
    }

    private void saveBill() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = editTextUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr)) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double units = Double.parseDouble(unitsStr);

            long id = dbHelper.insertBill(month, units, rebatePercentage,
                    totalCharges, finalCost);

            if (id != -1) {
                Toast.makeText(this, "Bill saved successfully!", Toast.LENGTH_SHORT).show();

                editTextUnits.setText("");

                rebateRadioGroup.check(R.id.rebate0);

                cardResults.setVisibility(View.GONE);

                Toast.makeText(this, "Bill saved to history!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to save bill", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid units value", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}