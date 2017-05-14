package edu.wvu.tsmith.logmylift;

import java.util.Date;

/**
 * Created by tmssm on 3/19/2017.
 * Interface to create and modify lifts. A lift is an exercise done at a specific weight and number
 * of reps. A lift itself belongs to a workout.
 * @author Tommy Smith
 */

class Lift {
    private Exercise exercise;
    private long lift_id;
    private int reps;
    private Date start_date;
    private int weight;
    private long workout_id;
    private LiftDbHelper lift_db;

    /**
     * Construct a lift. A lift should have all members at construction.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise          Exercise that is done during the lift.
     * @param reps              Number of reps of the lift.
     * @param weight            Weight of the lift.
     * @param workout_id        ID of the workout that the lift occured during.
     */
    Lift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, long workout_id) {
        this.exercise = exercise;
        this.lift_db = lift_db_helper;
        this.reps = reps;
        this.start_date = new Date();
        this.weight = weight;
        this.workout_id = workout_id;
        this.lift_id = this.lift_db.insertLift(this);
        this.exercise.setLastWorkoutId(this.workout_id);
    }

    // Read-only access to members.
    Exercise getExercise() { return this.exercise; }
    long getLiftId() { return this.lift_id; }
    int getReps() { return this.reps; }
    Date getStartDate() { return this.start_date; }
    int getWeight() { return this.weight; }
    long getWorkoutId() { return this.workout_id; }

    /**
     * Update the number of reps of the lift.
     * @param reps  Number of reps.
     */
    void setReps(int reps)
    {
        this.reps = reps;
        lift_db.updateRepsOfLift(this, reps);
    }

    /**
     * Update the weight of the lift.
     * @param weight    Weight of the lift.
     */
    void setWeight(int weight)
    {
        this.weight = weight;
        lift_db.updateWeightOfLift(this, weight);
    }
}