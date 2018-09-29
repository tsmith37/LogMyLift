package edu.wvu.tsmith.logmylift.lift;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.exercise.SelectExerciseHistoryParams;

/**
 * Created by Tommy Smith on 3/19/2017.
 * Interface to create and modify lifts. A lift is an exercise done at a specific weight and number
 * of reps, possibly including a comment. A lift itself belongs to a workout.
 * @author Tommy Smith
 */

public class Lift
{
    private String comment;
    private final Exercise exercise;
    private final long lift_id;
    private int reps;
    private final Date start_date;
    private int weight;
    private final long workout_id;
    private final String readable_start_date;
    private final String readable_start_time;
    private static final java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final java.text.SimpleDateFormat time_format = new java.text.SimpleDateFormat("hh:mm aa", Locale.US);

    /**
     * Construct a lift. A lift should have all members at construction.
     * @param exercise          Exercise that is done during the lift.
     * @param reps              Number of reps of the lift.
     * @param weight            Weight of the lift.
     * @param workout_id        ID of the workout that the lift occured during.
     */
    public Lift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, long workout_id, String comment) {
        this.comment = comment;
        this.exercise = exercise;
        this.reps = reps;
        this.start_date = new Date();
        this.readable_start_date = date_format.format(start_date);
        this.readable_start_time = time_format.format(start_date);

        this.weight = weight;
        this.workout_id = workout_id;
        this.lift_id = lift_db_helper.insertLift(this);

        this.exercise.setLastWorkoutId(lift_db_helper, this.workout_id);

        lift_db_helper.updateMaxWeightTableWithLift(this);
    }

    /**
     * Construct an already existing lift from parts.
     * @param lift_id           Unique ID of the lift used to identify it in the database.
     * @param exercise          The exercise that was done during the lift.
     * @param reps              The number of reps done during the lift.
     * @param start_date        The datetime that the lift was done.
     * @param weight            The weight which was lifted.
     * @param workout_id        The unique ID of the workout during which the lift was done.
     * @param comment           User-specified comment of the lift.
     */
    public Lift(long lift_id, Exercise exercise, int reps, Date start_date, int weight, long workout_id, String comment) {
        this.comment = comment;
        this.lift_id = lift_id;
        this.exercise = exercise;
        this.reps = reps;
        this.start_date = start_date;
        this.readable_start_date = date_format.format(start_date);
        this.readable_start_time = time_format.format(start_date);

        this.weight = weight;
        this.workout_id = workout_id;
    }

    // Read-only access to members.
    public String getComment() { return this.comment; }
    public Exercise getExercise() { return this.exercise; }
    public long getLiftId() { return this.lift_id; }
    public String getReadableStartDate() { return this.readable_start_date; }
    public String getReadableStartTime() { return this.readable_start_time; }
    public int getReps() { return this.reps; }
    public Date getStartDate() { return this.start_date; }
    public int getWeight() { return this.weight; }
    public long getWorkoutId() { return this.workout_id; }

    /**
     * Calculate the maximum effort of this lift.
     * @return  The "max effort" that results from this lift. If there was only one rep, then
     *          the max effort is identical to the amount of weight lifted.
     */
    public int calculateMaxEffort()
    {
        return findMaxEffort(this.weight, this.reps);
    }

    public static int findMaxEffort(int weight, int reps)
    {
        Double max_effort = (weight/(1.0278 - (0.0278 * reps)));
        return max_effort.intValue();
    }

    /**
     * Delete the lift. This removes it from the database, and recalculates the maximum effort lift.
     */
    public void delete(LiftDbHelper lift_db_helper)
    {
        lift_db_helper.deleteLift(this);

        // Check if the lift was a maximum effort or maximum weight lift. Do this so that we don't
        // calculate and update these values if it's not necessary.
        if (lift_db_helper.liftContainsMaxWeight(this))
        {
            lift_db_helper.updateMaxWeightRecordByExercise(this.getExercise().getExerciseId());
        }
    }

    public void update(LiftDbHelper lift_db_helper, int weight, int reps, String comment)
    {
        this.weight = weight;
        this.reps = reps;
        this.comment = comment;
        lift_db_helper.updateLift(this);
    }
}


