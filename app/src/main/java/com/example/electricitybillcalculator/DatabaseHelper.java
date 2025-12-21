package com.example.electricitybillcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "electricity_bills.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_REBATE = "rebate";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_FINAL_COST = "final_cost";
    public static final String COLUMN_DATE_CREATED = "date_created";

    // Create table SQL
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_BILLS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_UNITS + " REAL NOT NULL, " +
                    COLUMN_REBATE + " REAL NOT NULL, " +
                    COLUMN_TOTAL_CHARGES + " REAL NOT NULL, " +
                    COLUMN_FINAL_COST + " REAL NOT NULL, " +
                    COLUMN_DATE_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d("DatabaseHelper", "Table created: " + CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    public long insertBill(String month, double units, double rebate,
                           double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);

        long id = db.insert(TABLE_BILLS, null, values);
        db.close();
        return id;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID,
                COLUMN_MONTH,
                COLUMN_UNITS,
                COLUMN_REBATE,
                COLUMN_TOTAL_CHARGES,
                COLUMN_FINAL_COST,
                COLUMN_DATE_CREATED
        };
        return db.query(TABLE_BILLS, columns, null, null, null, null,
                COLUMN_DATE_CREATED + " DESC");
    }

    public Cursor getBillById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID,
                COLUMN_MONTH,
                COLUMN_UNITS,
                COLUMN_REBATE,
                COLUMN_TOTAL_CHARGES,
                COLUMN_FINAL_COST,
                COLUMN_DATE_CREATED
        };
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.query(TABLE_BILLS, columns, selection, selectionArgs,
                null, null, null);
    }

    public boolean updateBill(long id, String month, double units, double rebate,
                              double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int result = db.update(TABLE_BILLS, values, whereClause, whereArgs);
        db.close();
        return result > 0;
    }

    public boolean deleteBill(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        int result = db.delete(TABLE_BILLS, whereClause, whereArgs);
        db.close();
        return result > 0;
    }
}