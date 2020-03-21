package com.uday.fitdata.animation;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Chris Black
 */
public class ItemAnimator extends DefaultItemAnimator
{
    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if(oldHolder != null)
        {
            //oldHolder.itemView.setVisibility(View.INVISIBLE);
            dispatchChangeFinished(oldHolder, true);
        }

        if(newHolder != null)
        {
            //newHolder.itemView.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(newHolder.itemView, 1.0F);
            dispatchChangeFinished(newHolder, false);
        }

        return false;
    }
}
