package com.gribanskij.miserplus.sql_base;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by santy on 23.07.2016.
 */
public final class MiserContract {
    public static final String CONTENT_AUTHORITY = "com.gribanskij.miserplus";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_TRANSACTIONS = "data";
    public static final String PATH_ACCOUNTS = "accounts";
    public static final String PATH_NAMES = "category";
    public static final String PATH_BUDGET = "budget";


    /**
     * Possible values for the TYPE of the TRANSACTIONS and NAMES.
     */

    public static final int TYPE_COST = 0;
    public static final int TYPE_INCOME = 1;
    public static final int TYPE_ACCOUNTS = 2;

    private MiserContract() {
    }

    public static final class TransactionEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTIONS).build();

        public static final String NAME = "data";

        private TransactionEntry() {
        }

        public static class Cols implements BaseColumns {
            public static final String TYPE = "type";
            public static final String CATEGORY_ID = "category";
            public static final String DESCRIPTION = "description";
            public static final String AMOUNT = "amount";
            public static final String DATE = "date";
            public static final String ACCOUNT = "account";
            public static final String ACCOUNT_NAME = "ac_name";
            public static final String CATEGORY_NAME = "add2";
            public static final String RESERVE_1 = "reserve1";
            public static final String RESERVE_2 = "reserve2";

        }
    }

    public static final class AccountEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNTS).build();

        public static final String NAME = "accounts";

        private AccountEntry() {
        }

        public static class Cols implements BaseColumns {
            public static final String CATEGORY_ID = "id";
            public static final String ACCOUNT_AMOUNT = "amount";
            public static final String RESERVE_1 = "add1";
            public static final String RESERVE_2 = "add2";
        }
    }

    public static final class NameEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NAMES).build();

        public static final String NAME = "category";

        private NameEntry() {
        }

        public static class Cols implements BaseColumns {
            public static final String TYPE = "type";
            public static final String CATEGORY_ID = "id";
            public static final String CATEGORY_NAME = "name";
            public static final String RESERVE_1 = "reserve1";
            public static final String RESERVE_2 = "reserve2";
        }
    }

    public static final class BudgetEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUDGET).build();
        public static final String NAME = "budget";

        private BudgetEntry() {
        }

        public static class Cols implements BaseColumns {
            public static final String TYPE = "type";
            public static final String CATEGORY_ID = "id";
            public static final String BUDGET_SUM = "sum";
            public static final String BUDGET_DESCRIPTION = "description";
            public static final String BUDGET_MONTH = "month";
            public static final String BUDGET_INSERT_DATE = "full_date";
        }
    }
}
