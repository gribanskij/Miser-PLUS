package com.gribanskij.miserplus.sql_base;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by SESA175711 on 01.06.2017.
 */

public class Provider extends ContentProvider {

    public static final String LOG_TAG = Provider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int TRANSACTIONS = 100;
    private static final int TRANSACTION_WITH_ID = 101;
    private static final int TRANSACTION_BY_CATEGORIES = 102;

    private static final int ACCOUNTS = 200;
    private static final int ACCOUNT_WITH_ID = 201;


    private static final int NAMES = 300;
    private static final int NAME_WITH_ID = 301;

    private static final int BUDGET = 400;
    private static final int BUDGET_WITH_ID = 410;
    private static final int BUDGET_BY_CATEGORIES = 420;

    static {
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_TRANSACTIONS, TRANSACTIONS);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_TRANSACTIONS + "/#", TRANSACTION_WITH_ID);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_TRANSACTIONS + "/*", TRANSACTION_BY_CATEGORIES);


        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_ACCOUNTS, ACCOUNTS);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_ACCOUNTS + "/#", ACCOUNT_WITH_ID);


        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_NAMES, NAMES);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_NAMES + "/#", NAME_WITH_ID);


        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_BUDGET, BUDGET);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_BUDGET + "/#", BUDGET_WITH_ID);
        sUriMatcher.addURI(MiserContract.CONTENT_AUTHORITY, MiserContract.PATH_BUDGET + "/*", BUDGET_BY_CATEGORIES);
    }

    private MiserDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new MiserDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] p, @Nullable String s, @Nullable String[] sArgs, @Nullable String sOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String groupBy = null;
        String[] projection;
        String[] selectionArgs;
        String having = null;
        String selection;
        String id;
        String sortOrder = null;


        switch (sUriMatcher.match(uri)) {

            case TRANSACTIONS:
                queryBuilder.setTables(MiserContract.TransactionEntry.NAME);
                projection = p;
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;

            case ACCOUNTS:
                queryBuilder.setTables(MiserContract.AccountEntry.NAME);
                projection = p;
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;

            case NAMES:
                queryBuilder.setTables(MiserContract.NameEntry.NAME);
                projection = p;
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;

            case BUDGET:
                queryBuilder.setTables(MiserContract.BudgetEntry.NAME);
                projection = p;
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;


            case TRANSACTION_WITH_ID:
                queryBuilder.setTables(MiserContract.TransactionEntry.NAME);
                projection = p;
                // Get the transaction ID from the URI path
                id = uri.getPathSegments().get(1);
                selection = "_id = ?";
                selectionArgs = new String[]{id};
                sortOrder = sOrder;
                break;

            case ACCOUNT_WITH_ID:
                queryBuilder.setTables(MiserContract.AccountEntry.NAME);
                projection = p;
                // Get the account ID from the URI path
                id = uri.getPathSegments().get(1);
                selection = "_id = ?";
                selectionArgs = new String[]{id};
                sortOrder = sOrder;
                break;

            case NAME_WITH_ID:
                queryBuilder.setTables(MiserContract.NameEntry.NAME);
                projection = p;
                // Get the account ID from the URI path
                id = uri.getPathSegments().get(1);
                selection = "_id = ?";
                selectionArgs = new String[]{id};
                sortOrder = sOrder;
                break;

            case BUDGET_WITH_ID:
                queryBuilder.setTables(MiserContract.BudgetEntry.NAME);
                projection = p;
                // Get the budget ID from the URI path
                id = uri.getPathSegments().get(1);
                selection = "_id = ?";
                selectionArgs = new String[]{id};
                sortOrder = sOrder;
                break;

            case TRANSACTION_BY_CATEGORIES:
                queryBuilder.setTables(MiserContract.TransactionEntry.NAME);
                projection = p;
                // Get the transaction ID from the URI path
                groupBy = uri.getPathSegments().get(1);
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;

            case BUDGET_BY_CATEGORIES:
                queryBuilder.setTables(MiserContract.BudgetEntry.NAME);
                projection = p;
                groupBy = uri.getPathSegments().get(1);
                selection = s;
                selectionArgs = sArgs;
                sortOrder = sOrder;
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, groupBy, having, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.i(LOG_TAG, "Query - OK");

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        String table;
        Uri content_uri = null;

        switch (sUriMatcher.match(uri)) {

            case TRANSACTIONS:
                table = MiserContract.TransactionEntry.NAME;
                content_uri = MiserContract.TransactionEntry.CONTENT_URI;
                break;

            case ACCOUNTS:
                table = MiserContract.AccountEntry.NAME;
                content_uri = MiserContract.AccountEntry.CONTENT_URI;
                break;

            case NAMES:
                table = MiserContract.NameEntry.NAME;
                content_uri = MiserContract.NameEntry.CONTENT_URI;
                break;

            case BUDGET:
                table = MiserContract.BudgetEntry.NAME;
                content_uri = MiserContract.BudgetEntry.CONTENT_URI;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Uri returnUri;

        long id = database.insert(table, null, contentValues);

        if (id > 0) {
            returnUri = ContentUris.withAppendedId(content_uri, id);
            Log.i(LOG_TAG, "Insert a row - OK");
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        String table;
        String id;
        String where = "_id=?";
        Uri uri_group_sum = null;

        switch (sUriMatcher.match(uri)) {

            case TRANSACTION_WITH_ID:
                // Get the transaction ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.TransactionEntry.NAME;
                uri_group_sum = MiserContract.TransactionEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.TransactionEntry.Cols.CATEGORY_ID).build();
                break;

            case ACCOUNT_WITH_ID:
                // Get the account ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.AccountEntry.NAME;
                break;


            case NAME_WITH_ID:
                // Get the name ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.NameEntry.NAME;
                break;

            case BUDGET_WITH_ID:
                // Get the name ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.BudgetEntry.NAME;
                uri_group_sum = MiserContract.BudgetEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.BudgetEntry.Cols.CATEGORY_ID).build();
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int count_deleted = database.delete(table, where, new String[]{id});

        if (count_deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            if (uri_group_sum != null) {
                getContext().getContentResolver().notifyChange(uri_group_sum, null);
            }
            getContext().getContentResolver().notifyChange(uri, null);
            Log.i(LOG_TAG, "Delete a row - OK");
        }
        return count_deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {

        String table;
        String id;
        String where = "_id=?";
        Uri uri_group_sum = null;
        String[] arg = null;

        switch (sUriMatcher.match(uri)) {

            case TRANSACTION_WITH_ID:
                // Get the transaction ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.TransactionEntry.NAME;
                arg = new String[]{id};
                uri_group_sum = MiserContract.TransactionEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.TransactionEntry.Cols.CATEGORY_ID).build();
                break;

            case BUDGET_WITH_ID:
                // Get the transaction ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.BudgetEntry.NAME;
                arg = new String[]{id};
                uri_group_sum = MiserContract.BudgetEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.BudgetEntry.Cols.CATEGORY_ID).build();
                break;

            case ACCOUNT_WITH_ID:
                // Get the account ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.AccountEntry.NAME;
                arg = new String[]{id};
                break;

            case ACCOUNTS:
                where = MiserContract.AccountEntry.Cols.CATEGORY_ID + " =?";
                table = MiserContract.AccountEntry.NAME;
                arg = strings;
                where = s;
                break;

            case NAME_WITH_ID: {
                // Get the name ID from the URI path
                id = uri.getPathSegments().get(1);
                table = MiserContract.NameEntry.NAME;
                arg = new String[]{id};
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int count_updated = database.update(table, contentValues, where, arg);

        if (count_updated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            if (uri_group_sum != null) {
                getContext().getContentResolver().notifyChange(uri_group_sum, null);
            }
            Log.i(LOG_TAG, "Update a row - OK");
        }
        return count_updated;
    }
}
