package edu.wvu.tsmith.logmylift;

/**
 * Created by tmssm on 3/19/2017.
 */

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Lift {
    private Exercise exercise;
    private long lift_id;
    private int reps;
    private Date start_date;
    private int weight;
    private long workout_id;

    private LiftDbHelper lift_db;

    public Lift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, long workout_id) {
        this.exercise = exercise;
        this.lift_db = lift_db_helper;
        this.reps = reps;
        this.start_date = new Date();
        this.weight = weight;
        this.workout_id = workout_id;

        insertIntoDatabase();
    }

    public void deleteFromDatabase() {
        lift_db.deleteRowById(LiftDbHelper.LIFT_TABLE_NAME, LiftDbHelper.LIFT_COLUMN_LIFT_ID, this.lift_id);
    }

    public long GetLiftId() { return this.lift_id; }

    private void insertIntoDatabase() {
        long new_lift_id = lift_db.insertLift(
                this.reps,
                this.exercise.getExerciseId(),
                this.start_date,
                this.weight,
                this.workout_id);
        this.lift_id = new_lift_id;

        // SET THE EXERCISE'S LAST WORKOUT ID TO THE CURRENT WORKOUT.
        exercise.setLastWorkoutId(this.workout_id);
    }

    public void setReps(int reps) {
        this.reps = reps;
        lift_db.setFieldIntFromId(
                LiftDbHelper.LIFT_TABLE_NAME,
                LiftDbHelper.LIFT_COLUMN_LIFT_ID,
                this.lift_id,
                LiftDbHelper.LIFT_COLUMN_REPS,
                this.reps);
    }

    public void setWeight(int weight) {
        this.weight = weight;
        lift_db.setFieldIntFromId(
                LiftDbHelper.LIFT_TABLE_NAME,
                LiftDbHelper.LIFT_COLUMN_LIFT_ID,
                this.lift_id,
                LiftDbHelper.LIFT_COLUMN_WEIGHT,
                this.weight);
    }

}