package com.gribanskij.miserplus.asynctask;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.add_screen.AddFragment;

public class AsyncQueryTask extends AsyncQueryHandler {

    private Context context;

    public AsyncQueryTask(ContentResolver cr, Context context) {
        super(cr);
        this.context = context;
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);

        if (result == 0) Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        if (uri == null) Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
        else {
            if (token == AddFragment.TOKEN_INSERT_BUDGET) {
                Toast.makeText(context, R.string.Insert_Budget_OK, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.Insert_OK, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
        if (result == 0)
            Toast.makeText(context, R.string.db_error, Toast.LENGTH_SHORT).show();
    }
}
