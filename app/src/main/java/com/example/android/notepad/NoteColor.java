package com.example.android.notepad;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class NoteColor extends Activity {
    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,
                PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null && mCursor.moveToFirst()) {
            int columnIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
            if (columnIndex != -1) {
                color = mCursor.getInt(columnIndex);
                Log.i("NoteColor", "before " + color);
            } else {
                Log.e("NoteColor", "Column index not found");
                color = NotePad.Notes.DEFAULT_COLOR; // Default color if column index is not found
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            ContentValues values = new ContentValues();
            int columnIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
            if (columnIndex != -1) {
                values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
                getContentResolver().update(mUri, values, null, null);
                int y = mCursor.getInt(columnIndex);
                Log.i("NoteColor", "after " + y);
            } else {
                Log.e("NoteColor", "Column index not found");
            }
        }
    }

    public void white(View view) {
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();
    }

    public void yellow(View view) {
        color = NotePad.Notes.YELLOW_COLOR;
        finish();
    }

    public void blue(View view) {
        color = NotePad.Notes.BLUE_COLOR;
        finish();
    }

    public void green(View view) {
        color = NotePad.Notes.GREEN_COLOR;
        finish();
    }

    public void red(View view) {
        color = NotePad.Notes.RED_COLOR;
        finish();
    }
}