package com.finitydev.essentialtodolist;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finitydev.essentialtodolist.DataBase.TodoContract;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by finit on 8/4/2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements ItemTouchHelperAdapter{
    private ArrayList<ToDoTask> mDataset;
    private SQLiteDatabase db;
    private Context context;
    private View emptyView;







    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataset, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataset, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);


        //get id of moved element and update it after for loop







        // position that was moved gets tagged with new position
        //new value for one column
        ContentValues values;


        //which row to update
        String selection = TodoContract.TodoEntry.POSITION + " == ?";
        String arg = "" + fromPosition;
        String[] selectionArgs = { arg };

        Cursor cursor = db.query(TodoContract.TodoEntry.TABLE,
                new String[]{TodoContract.TodoEntry._ID},
                selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int index = cursor.getColumnIndex(TodoContract.TodoEntry._ID);

        int id = cursor.getInt(index);
       // Log.d("ID", "" + id);



        if(fromPosition > toPosition) {  //element is moving up

            //make every position after new position plus one until (old position - 1) is updated
            for (int i = toPosition; i < fromPosition; i++) {

                //new value for one column
                values = new ContentValues();
                values.put(TodoContract.TodoEntry.POSITION, (i + 1));


                //which row to update
                selection = TodoContract.TodoEntry.POSITION + " == ?";
                arg = "" + i;
                selectionArgs[0] = arg;

                db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);


            }
        } else {

            for( int j = (fromPosition + 1); j <= toPosition; j++ ) { //element is moving down

                values = new ContentValues();
                values.put(TodoContract.TodoEntry.POSITION, (j-1));

                selection = TodoContract.TodoEntry.POSITION + " == ?";
                arg = "" + j;
                selectionArgs[0] = arg;

                db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);


            }



        }


        values = new ContentValues();
        values.put(TodoContract.TodoEntry.POSITION, toPosition );



        //which row to update
        selection = TodoContract.TodoEntry._ID + " == ?";
        arg = "" + id;
        selectionArgs[0] = arg;






        db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);



       // String database = TodoDbHelper.getTableAsString(db,TodoContract.TodoEntry.TABLE);
        //Log.d("move",database);










    }

    @Override
    public void onItemDismiss(int position) {

        //remove from database
        //which row to delete
        String selection = TodoContract.TodoEntry.POSITION + " == ?";
        String arg = "" + position;
        String[] selectionArgs = { arg };

        //delete from database
        db.delete(TodoContract.TodoEntry.TABLE, selection, selectionArgs);

        //find all positions that are greater than position
        // make each position minus one

        if (mDataset.size() == 0) {               //list might be empty now, display empty text
           // g.displayEmptyText();

        }
        else {

            for (int i = position + 1; i < mDataset.size(); i++) {

                //new value for one column
                ContentValues values = new ContentValues();
                values.put(TodoContract.TodoEntry.POSITION, (i - 1));


                //which row to update
                selection = TodoContract.TodoEntry.POSITION + " == ?";
                arg = "" + i;
                selectionArgs[0] = arg;

                db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);


            }
        }

        mDataset.remove(position);
        notifyItemRemoved(position);

        if(mDataset.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        }



       // String database = TodoDbHelper.getTableAsString(db,TodoContract.TodoEntry.TABLE);
       // Log.d("delete",database);






    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public CheckBox mCheckbox;
        public ViewHolder(View view, TextView textView, CheckBox checkBox) {
            super(view);
            mTextView = textView;
            mCheckbox = checkBox;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<ToDoTask> myDataset,SQLiteDatabase db) {

        mDataset = myDataset;
        this.db = db;
    }


    public MyAdapter(ArrayList<ToDoTask> myDataset, Context context, View emptyView) {

        mDataset = myDataset;
        this.context = context;
        this.emptyView = emptyView;

    }

    public void provideDb(SQLiteDatabase db) {
        this.db = db;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_item, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.itemText);
        CheckBox checkBox = (CheckBox)  view.findViewById(R.id.checkBox);
        // set the view's size, margins, paddings and layout parameters
        //...

        ViewHolder vh = new ViewHolder(view,textView,checkBox);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final ToDoTask current = mDataset.get(position);
        holder.mTextView.setText(current.getTitle());
        holder.mCheckbox.setChecked(current.isFinished());
        //EditText editText = new EditText(context);
       // editText.setText(current.getTitle());
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(context);
                editText.setText(current.getTitle());
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setMaxLines(1);

                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(60);
                //filterArray[1] = new InputFilter.
                editText.setFilters(filterArray);
                final AlertDialog dialog = new AlertDialog.Builder(context)  //building with a chain

                        .setTitle("Edit your task")
                        .setMessage("You can edit your task here")
                        .setView(editText)
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if( editText.getText().toString().trim().length() == 0 )
                                {
                                    Toast.makeText(context, "You cannot have an empty task", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String title = String.valueOf(editText.getText());

                                current.setTitle(title);


                                //notify recycler of UI change
                                notifyDataSetChanged();  //not the most efficient, change later

                                updateDatabaseTitle(current, title);






                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();


                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        holder.mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newFinished = !current.isFinished();
                current.setFinished(newFinished);  //flip the checked box based on current state
                //Log.d("Checkbox", "ArrayList: " + current.getTitle() + ": " + newFinished);

                updateDatabaseCheckBox( current, newFinished);



        }});


    }

    protected void updateDatabaseCheckBox(ToDoTask current, boolean newFinished) {
        int finishedValue;
        if(newFinished) {
             finishedValue = 1;
        } else {
            finishedValue = 0;
        }


        int position = mDataset.indexOf(current);

        //new value for one column
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.FINISHED_TASK, finishedValue );


        //which row to update
        String selection = TodoContract.TodoEntry.POSITION + " == ?";
        String arg = "" + position;
        String[] selectionArgs = { arg };

        if(db != null) {
            db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);
        }

     //  String database = TodoDbHelper.getTableAsString(db,TodoContract.TodoEntry.TABLE);
      //  Log.d("Checkbox db update", database);









    }


    protected void updateDatabaseTitle(ToDoTask current, String title) {

        int position = mDataset.indexOf(current);



        //new value for one column
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.TITLE, title );


        //which row to update
        String selection = TodoContract.TodoEntry.POSITION + " == ?";
        String arg = "" + position;
        String[] selectionArgs = { arg };

        if(db != null) {
            db.update(TodoContract.TodoEntry.TABLE, values, selection, selectionArgs);
        }



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset.size();
    }
}




