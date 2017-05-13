package edu.wvu.tsmith.logmylift;

/**
 * Created by tmssm on 3/19/2017.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Workout {
    private long workout_id;

    private String description;
    private ArrayList<Long> lift_ids = new ArrayList<>();
    private Date start_date;

    private LiftDbHelper lift_db_helper;

    // Construct a new workout and insert it into the database.

    public Workout(LiftDbHelper lift_db_helper) {
        this.start_date = new Date();
        this.lift_db_helper = lift_db_helper;

        DateFormat human_readable_date_format = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        setDescription(human_readable_date_format.format(this.start_date));

        insertIntoDatabase();
    }

    public Workout(LiftDbHelper lift_db_helper, String description) {
        this(lift_db_helper);
        this.description = description;
        setDescription(this.description);
    }

    public Workout(LiftDbHelper lift_db_helper, long workout_id, String description, ArrayList<Long> lift_ids, Date start_date) {
        this.lift_db_helper = lift_db_helper;
        this.workout_id = workout_id;
        this.description = description;
        this.lift_ids = lift_ids;
        this.start_date = start_date;
    }

    public Lift AddLift(Exercise exercise, int reps, int weight) {
        Lift new_lift =  new Lift(lift_db_helper, exercise, reps, weight, this.workout_id);
        this.lift_ids.add(new_lift.GetLiftId());
        return new_lift;
    }

    public void deleteFromDatabase() {
        lift_db_helper.deleteRowById(LiftDbHelper.WORKOUT_TABLE_NAME, LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID, this.workout_id);
    }

    public long getWorkoutId() { return this.workout_id; };

    private void insertIntoDatabase() {
        long new_workout_id = this.lift_db_helper.insertWorkout(this.start_date);
        this.workout_id = new_workout_id;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lift_db_helper.setFieldStringFromId(
                LiftDbHelper.WORKOUT_TABLE_NAME,
                LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID,
                this.workout_id,
                LiftDbHelper.WORKOUT_COLUMN_DESCRIPTION,
                this.description);
    }
}
