package com.uday.fitdata.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.uday.fitdata.Utilities;
import com.uday.fitdata.activity.MainActivity;
import com.uday.fitdata.database.CupboardSQLiteOpenHelper;
import com.uday.fitdata.database.MockData;
import com.uday.fitdata.model.Workout;
import com.uday.fitdata.model.WorkoutReport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by Chris Black
 */
public class ReadCacheIntentService extends IntentService {

    private WorkoutReport workoutReport = new WorkoutReport();
    public final static String TAG = "ReadHistoricalService";
    private WeakReference<ResultReceiver> mReceiver;
    public ReadCacheIntentService() {
        super(TAG);
    }
    private boolean mockData = false;

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(MainActivity.RECEIVER_TAG);
        mReceiver = new WeakReference<>(resultReceiver);
        final CupboardSQLiteOpenHelper dbHelper = new CupboardSQLiteOpenHelper(this);
        final SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        Utilities.TimeFrame mTimeFrame = (Utilities.TimeFrame) intent.getSerializableExtra("TimeFrame");
        ArrayList<Workout> report;
        if (mockData) {
            switch (mTimeFrame) {
                case BEGINNING_OF_DAY: // 1 day
                    report = MockData.getDailyMockData().getWorkoutData();
                    break;
                case BEGINNING_OF_WEEK: // 1 week
                    report = MockData.getWeeklyMockData().getWorkoutData();
                    break;
                case BEGINNING_OF_MONTH: // 1 month
                    report = MockData.getMonthlyMockData().getWorkoutData();
                    break;
                case LAST_MONTH: // 1 month
                    report = MockData.getMonthlyMockData().getWorkoutData();
                    break;
                default:
                    report = MockData.getDailyMockData().getWorkoutData();
                    break;
            }
        } else {
            long startTime = Utilities.getTimeFrameStart(mTimeFrame);
            long endTime = Utilities.getTimeFrameEnd(mTimeFrame);
            workoutReport.clearWorkoutData();
            if (!mDb.isOpen()) {
                Log.w(TAG, "db is closed!");
                return;
            }
            QueryResultIterable<Workout> itr = cupboard().withDatabase(mDb).query(Workout.class).withSelection("start >= ? AND start <= ?", "" + startTime, "" + endTime).query();
            for (Workout workout : itr) {
                if (workout.start > startTime && workout.start <= endTime) {
                    workoutReport.addWorkoutData(workout);
                }
            }
            itr.close();
            report = workoutReport.getWorkoutData();

        }
        ResultReceiver receiver = mReceiver.get();
        if(receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("workoutList", report);
            receiver.send(200, bundle);
        }else {
            Log.w(TAG, "Weak listener is NULL.");
        }
        dbHelper.close();
    }
}
