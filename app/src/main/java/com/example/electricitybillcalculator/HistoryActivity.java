package com.example.electricitybillcalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

        dbHelper = new DatabaseHelper(this);

        listViewHistory = findViewById(R.id.listViewHistory);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        setupBottomNavigation();

        loadBills();

        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                if (cursor != null) {
                    long billId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));

                    // Show action menu (View, Edit, Delete)
                    showActionMenu(billId);
                }
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

        if (cursor.getCount() == 0) {
            textViewEmpty.setVisibility(View.VISIBLE);
            listViewHistory.setVisibility(View.GONE);
            return;
        }

        textViewEmpty.setVisibility(View.GONE);
        listViewHistory.setVisibility(View.VISIBLE);

        String[] from = {
                DatabaseHelper.COLUMN_MONTH,
                DatabaseHelper.COLUMN_UNITS,
                DatabaseHelper.COLUMN_FINAL_COST
        };

        int[] to = {
                R.id.textViewMonth,
                R.id.textViewUnits,
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
                if (v.getId() == R.id.textViewUnits) {
                    try {
                        double units = Double.parseDouble(text);
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        v.setText(df.format(units) + " kWh");
                    } catch (NumberFormatException e) {
                        v.setText("0 kWh");
                    }
                } else if (v.getId() == R.id.textViewFinalCost) {
                    try {
                        double amount = Double.parseDouble(text);
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        v.setText("RM " + df.format(amount));
                    } catch (NumberFormatException e) {
                        v.setText("RM 0.00");
                    }
                } else {
                    super.setViewText(v, text);
                }
            }
        };

        listViewHistory.setAdapter(adapter);
    }

    private void showActionMenu(final long billId) {
        // Menu options
        final CharSequence[] options = {"View Details", "Edit Bill", "Delete Bill"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // View Details
                        viewBillDetails(billId);
                        break;

                    case 1: // Edit Bill
                        editBill(billId);
                        break;

                    case 2: // Delete Bill
                        confirmDeleteBill(billId);
                        break;
                }
            }
        });
        builder.show();
    }

    private void viewBillDetails(long billId) {
        Intent intent = new Intent(HistoryActivity.this, BillDetailActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
    }

    private void editBill(long billId) {
        Intent intent = new Intent(HistoryActivity.this, EditBillActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
    }

    private void confirmDeleteBill(final long billId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Bill");
        builder.setMessage("Are you sure you want to delete this bill?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBill(billId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
        loadBills();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}