package com.finitydev.essentialtodolist.DataBase;

import android.provider.BaseColumns;

/**
 * Created by finit on 8/4/2017.
 * This Class defines the schema for the SQlite database.  The outer class defines the db name
 * while the inner class defines the particular table and column
 */

public class TodoContract {

    public static final String DB_NAME = "com.finitydev.todolist.db";
    public static final int DB_VERSION = 1;

    public class TodoEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String TITLE = "title";
        public static final String FINISHED_TASK = "isDone";

        public static final String POSITION = "position";
    }
}
