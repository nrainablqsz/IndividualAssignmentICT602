package com.example.electricitybillcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;

public class HistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private TextView textViewEmpty;
    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_history);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        listViewHistory = findViewById(R.id.listViewHistory);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Setup bottom navigation
        setupBottomNavigation();

        // Load bills from database
        loadBills();

        // Set up item click listener
        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                if (cursor != null) {
                    long billId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    viewBillDetails(billId);
                }
            }
        });

        // Set up long click listener for delete
        listViewHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                if (cursor != null) {
                    long billId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    deleteBill(billId);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_calculate) {
                startActivity(new Intent(HistoryActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_history) {
                return true;
            } else if (itemId == R.id.navigation_about) {
                startActivity(new Intent(HistoryActivity.this, AboutActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadBills() {
        Cursor cursor = dbHelper.getAllBills();

        // Check if there are any bills
        if (cursor.getCount() == 0) {
            textViewEmpty.setVisibility(View.VISIBLE);
            listViewHistory.setVisibility(View.GONE);
            return;
        }

        textViewEmpty.setVisibility(View.GONE);
        listViewHistory.setVisibility(View.VISIBLE);

        // Create adapter
        String[] from = {
                DatabaseHelper.COLUMN_MONTH,
                DatabaseHelper.COLUMN_FINAL_COST
        };

        int[] to = {
                R.id.textViewMonth,
                R.id.textViewFinalCost
        };

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_bill,
                cursor,
                from,
                to,
                0
        ) {
            @Override
            public void setViewText(TextView v, String text) {
                if (v.getId() == R.id.textViewFinalCost) {
                    try {
                        double amount = Double.parseDouble(text);
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        v.setText("RM " + df.format(amount));
                    } catch (NumberFormatException e) {
                        v.setText(text);
                    }
                } else {
                    super.setViewText(v, text);
                }
            }
        };

        listViewHistory.setAdapter(adapter);
    }

    private void viewBillDetails(long billId) {
        Intent intent = new Intent(HistoryActivity.this, BillDetailActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
    }

    private void deleteBill(long billId) {
        if (dbHelper.deleteBill(billId)) {
            Toast.makeText(this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();
            loadBills(); // Refresh the list
        } else {
            Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills(); // Refresh when returning to this activity
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

}