package edu.wvu.tsmith.logmylift;

import android.content.Context;

/**
 * Created by tmssm on 3/19/2017.
 */

public class Exercise {
    private String description;
    private long exercise_id;
    private long max_lift_id;
    private String name;
    private long last_workout_id;

    private LiftDbHelper lift_db;

    public Exercise(LiftDbHelper lift_db_helper, String name) {
        this.name = name;

        lift_db = lift_db_helper;
        insertIntoDatabase();
    }

    public Exercise(LiftDbHelper lift_db_helper, String name, String description) {
        this(lift_db_helper, name);
        setDescription(description);
    }

    public Exercise(LiftDbHelper lift_db_helper, long exercise_id, String name, String description, long max_lift_id, long last_workout_id) {
        this.lift_db = lift_db_helper;
        this.exercise_id = exercise_id;
        this.name = name;
        this.description = description;
        this.max_lift_id = max_lift_id;
        this.last_workout_id = last_workout_id;
    }

    public void deleteFromDatabase() {
        lift_db.deleteRowById(LiftDbHelper.EXERCISE_TABLE_NAME, LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID, this.exercise_id);
    }

    public long getExerciseId() { return this.exercise_id; };

    public String getExerciseDescription() { return this.description; };

    private void insertIntoDatabase() {
        long new_exercise_id = lift_db.insertExercise(this.name);
        this.exercise_id = new_exercise_id;
    }

    public void setDescription(String description) {
        this.description = description;
        lift_db.setFieldStringFromId(
                LiftDbHelper.EXERCISE_TABLE_NAME,
                LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID,
                this.exercise_id,
                LiftDbHelper.EXERCISE_COLUMN_DESCRIPTION,
                this.description);
    }

    public void setLastWorkoutId(long last_workout_id) {
        this.last_workout_id = last_workout_id;
        lift_db.setFieldLongFromId(
                LiftDbHelper.EXERCISE_TABLE_NAME,
                LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID,
                this.exercise_id,
                LiftDbHelper.EXERCISE_COLUMN_LAST_WORKOUT_ID,
                this.last_workout_id);
    }

    public void setMaxLiftId(long max_lift_id) {
        this.max_lift_id = max_lift_id;
        lift_db.setFieldLongFromId(
                LiftDbHelper.EXERCISE_TABLE_NAME,
                LiftDbHelper.EXERCISE_COLUMN_EXERCISE_ID,
                this.exercise_id,
                LiftDbHelper.EXERCISE_COLUMN_MAX_LIFT_ID,
                this.max_lift_id);
    }
}