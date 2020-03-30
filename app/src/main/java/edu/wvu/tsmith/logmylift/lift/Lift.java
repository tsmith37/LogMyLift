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
    private boolean toCreate;
    private long liftId;
    private Exercise exercise;
    private String comment;
    private int reps;
    private int weight;
    private Date startDate;
    private long workoutId;
    private String readableStartDate;
    private String readableStartTime;
    private LiftDbHelper liftDbHelper;

    private static java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static java.text.SimpleDateFormat time_format = new java.text.SimpleDateFormat("hh:mm aa", Locale.US);

    public static class Builder
    {
        // Required:
        private Exercise exercise = null;
        private int reps = -1;
        private int weight = -1;
        private long workoutId = -1;

        // Optional:
        private boolean toCreate = false;
        private long liftId = -1;
        private LiftDbHelper liftDbHelper;
        private String comment = "";
        private Date startDate = new Date();

        public Builder() {}

        public Builder exercise(Exercise value)
        {
            exercise = value;
            return this;
        }

        public Builder reps(int value)
        {
            reps = value;
            return this;
        }

        public Builder weight(int value)
        {
            weight = value;
            return this;
        }

        public Builder workoutId(long value)
        {
            workoutId = value;
            return this;
        }

        public Builder toCreate(boolean value)
        {
            toCreate = value;
            return this;
        }

        public Builder liftId(long value)
        {
            liftId = value;
            return this;
        }

        public Builder liftDbHelper(LiftDbHelper value)
        {
            liftDbHelper = value;
            return this;
        }

        public Builder comment(String value)
        {
            comment = value;
            return this;
        }

        public Builder startDate(Date value)
        {
            startDate = value;
            return this;
        }

        public Lift build()
        {
            return new Lift(this);
        }
    }

    private Lift(Builder builder)
    {
        if (builder.exercise == null || builder.reps == -1 || builder.weight == -1 || builder.workoutId == -1)
        {
            throw new LiftNotCorrectlyInstantiated("Lift not correctly instantiated");
        }

        this.toCreate = builder.toCreate;
        this.liftId = builder.liftId;
        this.exercise = builder.exercise;
        this.comment = builder.comment;
        this.reps = builder.reps;
        this.weight = builder.weight;
        this.startDate = builder.startDate;
        this.workoutId = builder.workoutId;
        this.readableStartDate = date_format.format(startDate);
        this.readableStartTime = time_format.format(startDate);
        this.liftDbHelper = builder.liftDbHelper;

        this.initLift();
    }

    private void initLift()
    {
        if (this.toCreate)
        {
            this.createLift();
        }
        else
        {
            if (this.liftId == -1)
            {
                throw new LiftNotCorrectlyInstantiated("Lift not correctly instantiated.");
            }
        }
    }

    private void createLift()
    {
        if (liftDbHelper != null)
        {
            this.liftId = liftDbHelper.insertLift(this);
            this.exercise.setLastWorkoutId(this.workoutId);
            liftDbHelper.updateMaxWeightTableWithLift(this);
        }
        else
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
    }

    // Read-only access to members.
    public String getComment() { return this.comment; }
    public Exercise getExercise() { return this.exercise; }
    public long getLiftId() { return this.liftId; }
    public String getReadableStartDate() { return this.readableStartDate; }
    public String getReadableStartTime() { return this.readableStartTime; }
    public int getReps() { return this.reps; }
    public Date getStartDate() { return this.startDate; }
    public int getWeight() { return this.weight; }
    public long getWorkoutId() { return this.workoutId; }

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
    public void delete()
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        this.liftDbHelper.deleteLift(this);

        // Check if the lift was a maximum effort or maximum weight lift. Do this so that we don't
        // calculate and update these values if it's not necessary.
        if (this.liftDbHelper.liftContainsMaxWeight(this))
        {
            liftDbHelper.updateMaxWeightRecordByExercise(this.getExercise().getExerciseId());
        }
    }

    public void update(int weight, int reps, String comment)
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        this.weight = weight;
        this.reps = reps;
        this.comment = comment;
        this.liftDbHelper.updateLift(this);
    }

    class LiftNotCorrectlyInstantiated extends RuntimeException
    {
        public LiftNotCorrectlyInstantiated(String message)
        {
            super(message);
        }
    }

    class LiftDbHelperNotInstantiated extends RuntimeException
    {
        public LiftDbHelperNotInstantiated(String message)
    {
        super(message);
    }
    }
}


