package edu.wvu.tsmith.logmylift;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tmssm on 3/19/2017.
 * Interface to create and modify lifts. A lift is an exercise done at a specific weight and number
 * of reps, possibly including a comment. A lift itself belongs to a workout.
 * @author Tommy Smith
 */

class Lift {
    private String comment;
    private Exercise exercise;
    private LiftDbHelper lift_db;
    private long lift_id;
    private int reps;
    private Date start_date;
    private int weight;
    private long workout_id;

    /**
     * Construct a lift. A lift should have all members at construction.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise          Exercise that is done during the lift.
     * @param reps              Number of reps of the lift.
     * @param weight            Weight of the lift.
     * @param workout_id        ID of the workout that the lift occured during.
     */
    Lift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, long workout_id, String comment) {
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

    Lift(LiftDbHelper lift_db_helper, long lift_id, Exercise exercise, int reps, Date start_date, int weight, long workout_id, String comment) {
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
    String getComment() { return this.comment; }
    Exercise getExercise() { return this.exercise; }
    long getLiftId() { return this.lift_id; }
    String getReadableStartDate() { return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this.start_date); }
    String getReadableStartTime() { return new java.text.SimpleDateFormat("hh:mm aa", Locale.US).format(this.start_date); }
    int getReps() { return this.reps; }
    Date getStartDate() { return this.start_date; }
    int getWeight() { return this.weight; }
    long getWorkoutId() { return this.workout_id; }

    int calculateMaxEffort()
    {
        Double maximum_effort = this.weight/(1.0278-(0.278*this.reps));
        return maximum_effort.intValue();
    }

    void delete()
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

    void setComment(String comment)
    {
        this.comment = comment;
        lift_db.updateCommentOfLift(this);
    }
    /**
     * Update the number of reps of the lift.
     * @param reps  Number of reps.
     */
    void setReps(int reps)
    {
        this.reps = reps;
        lift_db.updateRepsOfLift(this);
    }

    /**
     * Update the weight of the lift.
     * @param weight    Weight of the lift.
     */
    void setWeight(int weight)
    {
        this.weight = weight;
        lift_db.updateWeightOfLift(this);
    }
}