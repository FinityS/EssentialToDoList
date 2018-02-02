package com.finitydev.essentialtodolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.finitydev.essentialtodolist.DataBase.TodoContract;
import com.finitydev.essentialtodolist.DataBase.TodoDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Addtask";

    private ListView list;
    private SQLiteDatabase db;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton floatingButton;
    private ArrayList<ToDoTask> taskList;
    private TextView emptyText;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        emptyText = (TextView) findViewById(R.id.empty_view);



        // changes in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        dbHelper = new TodoDbHelper(this);

        taskList = new ArrayList<>();





        // specify an adapter
        mAdapter = new MyAdapter(taskList, this, emptyText);        //load initial data into Recycler view
       // mAdapter.
        mRecyclerView.setAdapter(mAdapter);


        LoadDatabaseTask task = new LoadDatabaseTask();
        task.execute(dbHelper);


        floatingButton = (FloatingActionButton) findViewById(R.id.fab);

        // tasks are added with the floating action button
        // handle the new tasks with a Dialog and embedded edit text
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open Dialog

                final EditText todoEditText = new EditText(MainActivity.this);
                todoEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                todoEditText.setMaxLines(1);
              //  todoEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(60);
                todoEditText.setFilters(FilterArray);
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)  //building with a chain
                        .setTitle("Add a new task")
                        .setMessage("Please enter your next task")
                        .setView(todoEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if( todoEditText.getText().toString().trim().length() == 0 )
                                {
                                    Toast.makeText(MainActivity.this, "You cannot have an empty task", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String title = String.valueOf(todoEditText.getText());
                                newTodo(title);   //add to database
                                ToDoTask newTask = new ToDoTask(title, false);
                                int position = taskList.size();
                                taskList.add(newTask);    //add to Array List




                                //notify recycler of UI change
                                mAdapter.notifyItemInserted(position);

                                if(taskList.size() == 1) {
                                    Toast.makeText(MainActivity.this, "Swipe to delete task", Toast.LENGTH_SHORT).show();
                                }
                                else if(taskList.size() == 2) {
                                    Toast.makeText(MainActivity.this, "Hold and drag to reorder", Toast.LENGTH_SHORT).show();
                                }


                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                todoEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                dialog.show();


            }
        });


        //hide floating button on scroll, and reveal when idle
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    floatingButton.show();
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 ||dy<0 && floatingButton.isShown())
                    floatingButton.hide();
            }
        });


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this
        ,DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);












    }



    private void getDataSetFromDatabase(SQLiteDatabase db) {


        this.db = db;


        Cursor cursor = db.query(TodoContract.TodoEntry.TABLE,
                new String[]{TodoContract.TodoEntry.TITLE, TodoContract.TodoEntry.FINISHED_TASK},
                null, null, null, null, TodoContract.TodoEntry.POSITION + " ASC");
        ToDoTask current;
        String title;
        boolean isFinished;
        int i;
        while (cursor.moveToNext()) {

            i = cursor.getColumnIndex(TodoContract.TodoEntry.TITLE);
            title = cursor.getString(i);

            i = cursor.getColumnIndex(TodoContract.TodoEntry.FINISHED_TASK);
            if (cursor.getInt(i) == 0) {
                isFinished = false;
            } else {
                isFinished = true;
            }

            current = new ToDoTask(title, isFinished);
            taskList.add(current);
        }



    }

    /*
      This method loads the new task into the database
     */
    protected void newTodo(String todo) {


        displayEmptyText();

        ContentValues values = new ContentValues();   // this is a holder for database entry
        values.put(TodoContract.TodoEntry.TITLE, todo);  //put value into holder
        values.put(TodoContract.TodoEntry.FINISHED_TASK, 0);  // task is initially unchecked
        values.put(TodoContract.TodoEntry.POSITION, taskList.size());
        db.insertWithOnConflict(TodoContract.TodoEntry.TABLE,   //insert into database
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        //String database = dbHelper.getTableAsString(db,TodoContract.TodoEntry.TABLE);
       //Log.d(TAG,database);


    }


    public void displayEmptyText() {
        emptyText.setVisibility(View.GONE);
    }


    @Override
    protected void onDestroy() {
        db.close();    // close the datebase when activity is being destroyed.  opening
                       //and closing database is expensive, so done here
        super.onDestroy();
    }




    private class LoadDatabaseTask extends AsyncTask<TodoDbHelper, Void, SQLiteDatabase> {

        @Override
        protected SQLiteDatabase doInBackground(TodoDbHelper...helpers){

            return helpers[0].getWritableDatabase();
        }








        @Override
        protected void onPostExecute(SQLiteDatabase db) {


            getDataSetFromDatabase(db);   //get Data set ready
            if(taskList.size() > 0) {
                emptyText.setVisibility(View.GONE);
            }
            mAdapter.provideDb(db);
            mAdapter.notifyDataSetChanged();





        }




    }








}
