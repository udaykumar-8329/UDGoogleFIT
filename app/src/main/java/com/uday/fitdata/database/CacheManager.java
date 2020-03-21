package com.uday.fitdata.database;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.ResultReceiver;
import android.util.Log;

import com.uday.fitdata.Utilities;
import com.uday.fitdata.activity.MainActivity;
import com.uday.fitdata.model.Workout;
import com.uday.fitdata.model.WorkoutTypes;
import com.uday.fitdata.service.ReadCacheIntentService;
import com.uday.fitdata.service.SummaryCacheIntentService;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by Chris Black
 */
public class CacheManager {

    public static final String TAG = "CacheManager";

    public CacheManager() {
    }

    public static void getReport(Utilities.TimeFrame timeFrame, ResultReceiver callback, Context context) {
        if (context != null) {
            Intent intentService = new Intent(context.getApplicationContext(), ReadCacheIntentService.class);
            intentService.putExtra("TimeFrame", timeFrame);
            intentService.putExtra(MainActivity.RECEIVER_TAG, callback);
            context.startService(intentService);
        }
    }

    public static void getSummary(int workoutType, ResultReceiver callback, Context context) {
        if (context != null) {
            Intent intentService = new Intent(context.getApplicationContext(), SummaryCacheIntentService.class);
            intentService.putExtra("WorkoutType", workoutType);
            intentService.putExtra(MainActivity.RECEIVER_TAG, callback);
            context.startService(intentService);
        }
    }

    public static boolean checkConflict(Context context, Workout inWorkout) {
        boolean overlap = false;
        if (context != null) {
            final CupboardSQLiteOpenHelper dbHelper = new CupboardSQLiteOpenHelper(context);
            final SQLiteDatabase mDb = dbHelper.getReadableDatabase();
            long rangeStart = inWorkout.start - 1000 * 60 * 60 * 24;
            long rangeEnd = inWorkout.start + inWorkout.duration;
            QueryResultIterable<Workout> itr = cupboard().withDatabase(mDb).query(Workout.class).withSelection("start BETWEEN ? AND ?", "" + rangeStart, "" + rangeEnd).query();
            for (Workout workout : itr) {
                Log.d(TAG, workout.toString());
                if (workout.type != WorkoutTypes.STILL.getValue() && workout.type != WorkoutTypes.UNKNOWN.getValue() && workout.overlaps(inWorkout)) {
                    overlap = true;
                }
            }
            itr.close();
            dbHelper.close();
        }
        return overlap;
    }

    public interface ICacheManager {
        Cursor getCursor();
    }
}
