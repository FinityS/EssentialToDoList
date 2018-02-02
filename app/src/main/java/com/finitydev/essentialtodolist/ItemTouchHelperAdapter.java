package com.finitydev.essentialtodolist;

/**
 * Created by finit on 8/14/2017.
 */

public interface ItemTouchHelperAdapter {


    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);


}
