package com.uday.fitdata.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Created by Chris Black
 */
public class AnimatedLayoutManager extends GridLayoutManager {


    public AnimatedLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }
}
