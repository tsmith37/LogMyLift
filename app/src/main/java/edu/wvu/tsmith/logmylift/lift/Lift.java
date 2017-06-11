package edu.wvu.tsmith.logmylift.lift;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.exercise.Exercise;

/**
 * Created by Tommy Smith on 3/19/2017.
 * Interface to create and modify lifts. A lift is an exercise done at a specific weight and number
 * of reps, possibly including a comment. A lift itself belongs to a workout.
 * @author Tommy Smith
 */

public class Lift {
    private String comment;
    private final Exercise exercise;
    private final LiftDbHelper lift_db;
    private final long lift_id;
    private int reps;
    private final Date start_date;
    private int weight;
    private final long workout_id;

    /**
     * Construct a lift. A lift should have all members at construction.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise          Exercise that is done during the lift.
     * @param reps              Number of reps of the lift.
     * @param weight            Weight of the lift.
     * @param workout_id        ID of the workout that the lift occured during.
     */
    public Lift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, long workout_id, String comment) {
        this.comment = comment;
        this.exercise = exercise;
        this.lift_db = lift_db_helper;
        this.reps = reps;
        this.start_date = new Date();
        this.weight = weight;
        this.workout_id = workout_id;
        this.lift_id = this.lift_db.insertLift(this);

        this.exercise.setLastWorkoutId(this.workout_id);

        Lift exercise_max_effort_lift = this.lift_db.selectLiftFromLiftId(this.exercise.getMaxLiftId());
        if (exercise_max_effort_lift != null) {
            int exercise_current_max_effort = exercise_max_effort_lift.calculateMaxEffort();

            if (calculateMaxEffort() > exercise_current_max_effort) {
                this.exercise.setMaxLiftId(this.lift_id);
            }
        }
        else
        {
            this.exercise.setMaxLiftId(this.lift_id);
        }

    }

    /**
     * Construct an already existing lift from parts.
     * @param lift_db_helper    The SQLite database helper.
     * @param lift_id           Unique ID of the lift used to identify it in the database.
     * @param exercise          The exercise that was done during the lift.
     * @param reps              The number of reps done during the lift.
     * @param start_date        The datetime that the lift was done.
     * @param weight            The weight which was lifted.
     * @param workout_id        The unique ID of the workout during which the lift was done.
     * @param comment           User-specified comment of the lift.
     */
    public Lift(LiftDbHelper lift_db_helper, long lift_id, Exercise exercise, int reps, Date start_date, int weight, long workout_id, String comment) {
        this.comment = comment;
        this.lift_id = lift_id;
        this.exercise = exercise;
        this.lift_db = lift_db_helper;
        this.reps = reps;
        this.start_date = start_date;
        this.weight = weight;
        this.workout_id = workout_id;
    }

    // Read-only access to members.
    public String getComment() { return this.comment; }
    public Exercise getExercise() { return this.exercise; }
    public long getLiftId() { return this.lift_id; }
    public String getReadableStartDate() { return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this.start_date); }
    public String getReadableStartTime() { return new java.text.SimpleDateFormat("hh:mm aa", Locale.US).format(this.start_date); }
    public int getReps() { return this.reps; }
    public Date getStartDate() { return this.start_date; }
    public int getWeight() { return this.weight; }
    public long getWorkoutId() { return this.workout_id; }

    /**
     * Calculate the maximum effort of this lift.
     * @return  The "max effort" that results from this lift. If there was only one rep, then
     *          the max effort is identical to the amount of weight lifted.
     */
    private int calculateMaxEffort()
    {
        Double maximum_effort = this.weight/(1.0278-(0.0278*this.reps));
        return maximum_effort.intValue();
    }

    /**
     * Delete the lift. This removes it from the database, and recalculates the maximum effort lift.
     */
    public void delete()
    {
        lift_db.deleteLift(this);
        ArrayList<Lift> exercise_history = lift_db.selectExerciseHistoryLifts(this.exercise);

        if (0 == exercise_history.size())
        {
            this.exercise.clearMaxLiftId();
        }
        else
        {
            Lift max_effort_lift_found = exercise_history.get(0);
            for (Lift current_lift : exercise_history)
            {
                if (current_lift.calculateMaxEffort() > max_effort_lift_found.calculateMaxEffort())
                {
                    max_effort_lift_found = current_lift;
                }
            }
            this.exercise.setMaxLiftId(max_effort_lift_found.getLiftId());
        }
    }

    /**
     * Set the comment of the lift.
     * @param comment   New comment of the lift.
     */
    public void setComment(String comment)
    {
        this.comment = comment;
        lift_db.updateCommentOfLift(this);
    }

    /**
     * Update the number of reps of the lift.
     * @param reps  Number of reps.
     */
    public void setReps(int reps)
    {
        this.reps = reps;
        lift_db.updateRepsOfLift(this);
    }

    /**
     * Update the weight of the lift.
     * @param weight    Weight of the lift.
     */
    public void setWeight(int weight)
    {
        this.weight = weight;
        lift_db.updateWeightOfLift(this);
    }
}