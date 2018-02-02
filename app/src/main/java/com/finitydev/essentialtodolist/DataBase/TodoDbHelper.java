package com.finitydev.essentialtodolist.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by finit on 8/4/2017.
 * Helper class for the database, as required for Android SQlite
 */

public class TodoDbHelper extends SQLiteOpenHelper {


    public TodoDbHelper(Context context) {
        super(context, TodoContract.DB_NAME, null, TodoContract.DB_VERSION);
    }



    //creates the basic database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TodoContract.TodoEntry.TABLE + " ( " +
                TodoContract.TodoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TodoContract.TodoEntry.TITLE + " TEXT NOT NULL, " +
                TodoContract.TodoEntry.POSITION + " INTEGER, " +
                TodoContract.TodoEntry.FINISHED_TASK + " INTEGER DEFAULT 0);";

        db.execSQL(createTable);
    }



    //Upgrading the table merely replaces the old with the new
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TodoContract.TodoEntry.TABLE);
        onCreate(db);
    }

    public static String getTableAsString(SQLiteDatabase db, String tableName) {
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }



}
