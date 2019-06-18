package edu.wvu.tsmith.logmylift.upgradability;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.wvu.tsmith.logmylift.LiftDbHelper;

public class MaxWeightPopulator
{
    private SQLiteDatabase database;
    private Cursor exerciseCursor;

    private class LiftIdAndWeight
    {
        private long liftId;
        private int weight;

        private LiftIdAndWeight(long liftId, int weight)
        {
            this.liftId = liftId;
            this.weight = weight;
        }
    }

    public MaxWeightPopulator(SQLiteDatabase database)
    {
        this.database = database;
        this.exerciseCursor = this.initExerciseCursor();
    }

    public void run()
    {
        while(this.exerciseCursor.moveToNext())
        {
            long exercise_id = exerciseCursor.getLong(exerciseCursor.getColumnIndexOrThrow(LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID));
            this.createMaxWeightRecordForExercise(exercise_id);
        }
    }

    private void createMaxWeightRecordForExercise(long exercise_id)
    {
        LiftIdAndWeight maxWeight = this.findMaxWeightLiftByExercise(exercise_id);
        LiftIdAndWeight maxEffort = this.findMaxEffortLiftByExercise(exercise_id);

        if (maxWeight == null || maxEffort == null)
        {
            return;
        }

        if (this.maxWeightRecordExistsByExercise(exercise_id))
        {
            this.updateExistingMaxWeightRecord(exercise_id, maxEffort, maxWeight);
        }
        else
        {
            this.createNewMaxWeightRecord(exercise_id, maxEffort, maxWeight);
        }
    }

    private void createNewMaxWeightRecord(long exercise_id, LiftIdAndWeight maxEffort, LiftIdAndWeight maxWeight)
    {
        ContentValues insert_values = new ContentValues();
        insert_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id);
        insert_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID, maxEffort.liftId);
        insert_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, maxEffort.weight);
        insert_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_LIFT_LIFT_ID, maxWeight.liftId);
        insert_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_LIFT_WEIGHT, maxWeight.weight);
        database.insert(LiftDbHelper.MAX_WEIGHT_TABLE_NAME, null, insert_values);
    }

    private void updateExistingMaxWeightRecord(long exercise_id, LiftIdAndWeight maxEffort, LiftIdAndWeight maxWeight)
    {
        ContentValues update_values = new ContentValues();
        update_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID, maxEffort.liftId);
        update_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, maxEffort.weight);
        update_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_LIFT_LIFT_ID, maxWeight.liftId);
        update_values.put(LiftDbHelper.MAX_WEIGHT_COLUMN_LIFT_WEIGHT, maxWeight.weight);

        String WHERE =  LiftDbHelper.MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise_id)};
        database.update(
                LiftDbHelper.MAX_WEIGHT_TABLE_NAME,
                update_values,
                WHERE,
                where_args);
    }

    private LiftIdAndWeight findMaxWeightLiftByExercise(long exercise_id)
    {
        String[] RETURN_COLUMNS = { LiftDbHelper.LIFT_COLUMN_LIFT_ID, LiftDbHelper.LIFT_COLUMN_WEIGHT };
        String WHERE = LiftDbHelper.LIFT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise_id)};
        String sort_order = LiftDbHelper.LIFT_COLUMN_WEIGHT + " DESC";
        Cursor select_cursor = database.query(
                LiftDbHelper.LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                sort_order);

        if (select_cursor.moveToFirst())
        {
            return new LiftIdAndWeight(
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_LIFT_ID)),
                    select_cursor.getInt(select_cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_WEIGHT)));
        }
        else
        {
            return null;
        }
    }

    private LiftIdAndWeight findMaxEffortLiftByExercise(long exercise_id)
    {
        String MAX_EFFORT_COLUMN_NAME = "MaxEffort";
        String MAX_EFFORT_COLUMN = "(" + LiftDbHelper.LIFT_COLUMN_WEIGHT + " / (1.0278 - (0.0278 * " + LiftDbHelper.LIFT_COLUMN_REPS + "))) AS " + MAX_EFFORT_COLUMN_NAME;
        String[] RETURN_COLUMNS = { LiftDbHelper.LIFT_COLUMN_LIFT_ID, MAX_EFFORT_COLUMN };
        String WHERE = LiftDbHelper.LIFT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise_id)};
        String sort_order = MAX_EFFORT_COLUMN_NAME + " DESC";
        Cursor select_cursor = database.query(
                LiftDbHelper.LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                sort_order);

        if (select_cursor.moveToFirst())
        {
            return new LiftIdAndWeight(
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_LIFT_ID)),
                    select_cursor.getInt(select_cursor.getColumnIndexOrThrow(MAX_EFFORT_COLUMN_NAME)));
        }
        else
        {
            return null;
        }
    }

    private boolean maxWeightRecordExistsByExercise(long exercise_id)
    {
        // Check if a row exists in the max weight table for the exercise.
        String[] RETURN_COLUMNS = {LiftDbHelper.MAX_WEIGHT_COLUMN_EXERCISE_ID};
        String WHERE = LiftDbHelper.MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = database.query(
                LiftDbHelper.MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();

        return id_in_db;
    }

    private Cursor initExerciseCursor()
    {
        String[] RETURN_COLUMNS = {
                LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID,
                LiftDbHelper.EXERCISE_COLUMN_NAME,
                LiftDbHelper.EXERCISE_COLUMN_DESCRIPTION };

            return database.query(
                    LiftDbHelper.EXERCISE_TABLE_NAME,
                    RETURN_COLUMNS,
                    null,
                    null,
                    null,
                    null,
                    LiftDbHelper.EXERCISE_COLUMN_NAME);
    }
}

