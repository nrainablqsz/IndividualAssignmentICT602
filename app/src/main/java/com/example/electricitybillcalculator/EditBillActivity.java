package com.example.electricitybillcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class EditBillActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private RadioGroup rebateRadioGroup;
    private Button buttonUpdate, buttonDelete;
    private DatabaseHelper dbHelper;
    private long billId;

    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    // Electricity rates
    private static final double RATE_FIRST_200 = 0.218;
    private static final double RATE_NEXT_100 = 0.334;
    private static final double RATE_NEXT_300 = 0.516;
    private static final double RATE_ABOVE_600 = 0.546;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        billId = getIntent().getLongExtra("BILL_ID", -1);
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupMonthSpinner();
        loadBillData();
    }

    private void initViews() {
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);

        buttonUpdate.setOnClickListener(v -> updateBill());
        buttonDelete.setOnClickListener(v -> deleteBill());

        rebateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rebate0) rebatePercentage = 0;
            else if (checkedId == R.id.rebate1) rebatePercentage = 1;
            else if (checkedId == R.id.rebate2) rebatePercentage = 2;
            else if (checkedId == R.id.rebate3) rebatePercentage = 3;
            else if (checkedId == R.id.rebate4) rebatePercentage = 4;
            else if (checkedId == R.id.rebate5) rebatePercentage = 5;

            calculateBill();
        });
    }

    private void setupMonthSpinner() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
    }

    private void loadBillData() {
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = dbHelper.getBillById(billId);
        if (cursor != null && cursor.moveToFirst()) {
            // Get data
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
            double units = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE));
            totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES));
            finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST));

            // Set month
            for (int i = 0; i < spinnerMonth.getCount(); i++) {
                if (spinnerMonth.getItemAtPosition(i).toString().equals(month)) {
                    spinnerMonth.setSelection(i);
                    break;
                }
            }

            editTextUnits.setText(String.valueOf(units));

            switch ((int) rebate) {
                case 0: rebateRadioGroup.check(R.id.rebate0); break;
                case 1: rebateRadioGroup.check(R.id.rebate1); break;
                case 2: rebateRadioGroup.check(R.id.rebate2); break;
                case 3: rebateRadioGroup.check(R.id.rebate3); break;
                case 4: rebateRadioGroup.check(R.id.rebate4); break;
                case 5: rebateRadioGroup.check(R.id.rebate5); break;
            }

            cursor.close();
        }
    }

    private void calculateBill() {
        String unitsStr = editTextUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr)) {
            return;
        }

        try {
            double units = Double.parseDouble(unitsStr);

            if (units < 1 || units > 1000) {
                Toast.makeText(this, "Units must be 1-1000 kWh", Toast.LENGTH_SHORT).show();
                return;
            }

            totalCharges = calculateTotalCharges(units);
            double rebateAmount = totalCharges * (rebatePercentage / 100.0);
            finalCost = totalCharges - rebateAmount;

            totalCharges = Math.round(totalCharges * 100.0) / 100.0;
            finalCost = Math.round(finalCost * 100.0) / 100.0;

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid units", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateTotalCharges(double units) {
        double charges = 0;

        if (units <= 200) {
            charges = units * RATE_FIRST_200;
        } else if (units <= 300) {
            charges = (200 * RATE_FIRST_200) + ((units - 200) * RATE_NEXT_100);
        } else if (units <= 600) {
            charges = (200 * RATE_FIRST_200) + (100 * RATE_NEXT_100) + ((units - 300) * RATE_NEXT_300);
        } else {
            charges = (200 * RATE_FIRST_200) + (100 * RATE_NEXT_100) + (300 * RATE_NEXT_300) + ((units - 600) * RATE_ABOVE_600);
        }

        return charges;
    }

    private void updateBill() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = editTextUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr)) {
            Toast.makeText(this, "Please enter units", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double units = Double.parseDouble(unitsStr);

            if (units < 1 || units > 1000) {
                Toast.makeText(this, "Units must be 1-1000 kWh", Toast.LENGTH_SHORT).show();
                return;
            }

            calculateBill();

            boolean success = dbHelper.updateBill(billId, month, units, rebatePercentage,
                    totalCharges, finalCost);

            if (success) {
                Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid units", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBill() {
        boolean success = dbHelper.deleteBill(billId);

        if (success) {
            Toast.makeText(this, "Bill deleted successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
        }
    }
}