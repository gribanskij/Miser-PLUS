package com.gribanskij.miserplus.sql_base;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gribanskij.miserplus.R;


/**
 * Created by SESA175711 on 22.07.2016.
 */
class MiserDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "miserBase.db";
    private static final int DB_VERSION = 2;
    private final int SUM_NULL = 0;
    private Context context;

    public MiserDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE " + MiserContract.TransactionEntry.NAME + "(" + MiserContract.TransactionEntry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MiserContract.TransactionEntry.Cols.TYPE + " INTEGER NOT NULL" + ", " +
                    MiserContract.TransactionEntry.Cols.CATEGORY_ID + " INTEGER NOT NULL" + ", " +
                    MiserContract.TransactionEntry.Cols.DESCRIPTION + " TEXT NOT NULL" + ", " +
                    MiserContract.TransactionEntry.Cols.AMOUNT + " REAL NOT NULL" + ", " +
                    MiserContract.TransactionEntry.Cols.DATE + " INTEGER NOT NULL" + ", " +
                    MiserContract.TransactionEntry.Cols.ACCOUNT + " INTEGER NOT NULL" + " ," +
                    MiserContract.TransactionEntry.Cols.ACCOUNT_NAME + " TEXT NOT NULL" + " ," +
                    MiserContract.TransactionEntry.Cols.CATEGORY_NAME + " TEXT NOT NULL" + " )");

            db.execSQL("CREATE TABLE " + MiserContract.AccountEntry.NAME + "(" + MiserContract.AccountEntry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MiserContract.AccountEntry.Cols.CATEGORY_ID + " INTEGER NOT NULL" + ", " +
                    MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT + " REAL NOT NULL" + " )");

            db.execSQL("CREATE TABLE " + MiserContract.NameEntry.NAME + "(" + MiserContract.NameEntry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MiserContract.NameEntry.Cols.TYPE + " INTEGER NOT NULL" + ", " +
                    MiserContract.NameEntry.Cols.CATEGORY_ID + " INTEGER NOT NULL" + ", " +
                    MiserContract.NameEntry.Cols.CATEGORY_NAME + " TEXT NOT NULL" + " )");
        }
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + MiserContract.BudgetEntry.NAME + "(" + MiserContract.BudgetEntry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MiserContract.BudgetEntry.Cols.TYPE + " INTEGER NOT NULL" + ", " +
                    MiserContract.BudgetEntry.Cols.CATEGORY_ID + " INTEGER NOT NULL" + ", " +
                    MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION + " TEXT NOT NULL" + ", " +
                    MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " INTEGER NOT NULL" + ", " +
                    MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE + " INTEGER NOT NULL" + ", " +
                    MiserContract.BudgetEntry.Cols.BUDGET_SUM + " REAL NOT NULL" + " )");
        }

        initDB(db);
    }

    //Fill category table and accounts table names by default

    private void initDB(SQLiteDatabase db) {

        ContentValues contentValues = new ContentValues();
        String[] income_category = context.getResources().getStringArray(R.array.income_category);
        String[] cost_category = context.getResources().getStringArray(R.array.cost_category);
        String[] account = context.getResources().getStringArray(R.array.accounts);

        for (int i = 0; i < account.length; i++) {
            contentValues.put(MiserContract.AccountEntry.Cols.CATEGORY_ID, i);
            contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, SUM_NULL);
            db.insert(MiserContract.AccountEntry.NAME, null, contentValues);
            contentValues.clear();
        }

        int a = 0;
        for (String str : cost_category) {
            contentValues.put(MiserContract.NameEntry.Cols.TYPE, MiserContract.TYPE_COST);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_ID, a++);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_NAME, str);
            db.insert(MiserContract.NameEntry.NAME, null, contentValues);
            contentValues.clear();
        }

        a = 0;
        for (String str : income_category) {
            contentValues.put(MiserContract.NameEntry.Cols.TYPE, MiserContract.TYPE_INCOME);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_ID, a++);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_NAME, str);
            db.insert(MiserContract.NameEntry.NAME, null, contentValues);
            contentValues.clear();
        }

        a = 0;
        for (String str : account) {
            contentValues.put(MiserContract.NameEntry.Cols.TYPE, MiserContract.TYPE_ACCOUNTS);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_ID, a++);
            contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_NAME, str);
            db.insert(MiserContract.NameEntry.NAME, null, contentValues);
            contentValues.clear();
        }
    }
}
