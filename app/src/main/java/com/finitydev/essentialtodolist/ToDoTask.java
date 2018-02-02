package com.finitydev.essentialtodolist;

/**
 * Created by finit on 8/6/2017.
 */

public class ToDoTask {

    private String title;
    private boolean isFinished;


    public ToDoTask(String title, boolean isFinished) {
        this.title = title;
        this.isFinished = isFinished;


    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
