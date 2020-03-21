package com.uday.fitdata.activity;

import android.view.View;

import com.uday.fitdata.model.Workout;

/**
 * Created by Chris Black
 *
 * Callbacks specific to the MainActivity
 */
public interface IMainActivityCallback {
    void launch(View transitionView, Workout workout);
    void quickDataRead();
    void setStepCounting(boolean active);
    void setActivityTracking(boolean active);
}
