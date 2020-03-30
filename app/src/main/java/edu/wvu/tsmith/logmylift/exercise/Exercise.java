package edu.wvu.tsmith.logmylift.exercise;

import edu.wvu.tsmith.logmylift.LiftDbHelper;

/**
 * Created by Tommy Smith on 3/19/2017.
 * Interface to create and modify exercises. Exercises are the the major key here; without
 * the exercises, lifts cannot be done. Without lifts, workouts cannot be done. Exercises
 * allow tracking of different movements. They are stored in a SQLite database with a
 * unique ID as well as a name and a description. This allows the history of an exercise
 * to be tracked, via the lift table which contains the exercise done during the lift. The
 * most recent workout that an exercise is done is tracked using the last workout ID of
 * the exercise. Finally, the maximum effort done by the user on a particular exercise
 * can be tracked using the max lift ID of the exercise.
 * @author Tommy Smith
 */

public class Exercise
{
    private boolean toCreate;
    private String name;
    private long exerciseId;
    private String description;
    private LiftDbHelper liftDbHelper;
    private long lastWorkoutId;

    // Depricated:
    //private long max_lift_id;

    public static class Builder
    {
        // Required:
        private String name = "";

        // Optional:
        private long exerciseId;
        private boolean toCreate = false;
        private String description = "";
        private LiftDbHelper liftDbHelper = null;
        private long lastWorkoutId = -1;

        public Builder() {}

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder exerciseId(long exerciseId)
        {
            this.exerciseId = exerciseId;
            return this;
        }

        public Builder toCreate(boolean toCreate)
        {
            this.toCreate = toCreate;
            return this;
        }

        public Builder description(String value)
        {
            description = value;
            return this;
        }

        public Builder liftDbHelper(LiftDbHelper value)
        {
            liftDbHelper = value;
            return this;
        }

        public Builder lastWorkoutId(long value)
        {
            lastWorkoutId = value;
            return this;
        }

        public Exercise build()
        {
            return new Exercise(this);
        }
    }

    private Exercise(Builder builder)
    {
        if (builder.name == "")
        {
            throw new ExerciseNotCorrectlyInstantiated("Exercise not correctly instantiated.");
        }

        if (builder.exerciseId == -1 && !builder.toCreate)
        {
            throw new ExerciseNotCorrectlyInstantiated("Exercise not correctly instantiated.");
        }

        this.toCreate = builder.toCreate;
        this.name = builder.name;
        this.exerciseId = builder.exerciseId;
        this.description = builder.description;
        this.liftDbHelper = builder.liftDbHelper;
        this.lastWorkoutId = builder.lastWorkoutId;

        this.initExercise();
    }

    private void initExercise()
    {
        if (this.toCreate)
        {
            this.createExercise();
        }
    }

    private void createExercise()
    {
        if (liftDbHelper != null)
        {
            if (liftDbHelper.exerciseNameExists(this.name))
            {
                throw new ExerciseAlreadyExistsException(name + " already exists.");
            }
            this.exerciseId = liftDbHelper.insertExercise(this);
        }
        else
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
    }

    // Public access to read-only members.
    public long getExerciseId() { return this.exerciseId; }
    public long getLastWorkoutId() { return this.lastWorkoutId; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    /**
     * Sets the name of this exercise.
     * @param name  The new name of the exercise.
     */
    void setName(String name)
    {
        this.name = name;

        if (liftDbHelper != null)
        {
            this.liftDbHelper.updateNameOfExercise(this);
        }
        else
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
    }

    /**
     * Updates the description of the exercise.
     * @param description   Updated description.
     */
    void setDescription(String description)
    {
        this.description = description;

        if (liftDbHelper != null)
        {
            this.liftDbHelper.updateDescriptionOfExercise(this);
        }
        else
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
    }

    /**
     * Updates the most recent workout ID of the exercise.
     * @param lastWorkoutId   Workout ID.
     */
    public void setLastWorkoutId(long lastWorkoutId)
    {
        this.lastWorkoutId = lastWorkoutId;

        if (liftDbHelper != null)
        {
            this.liftDbHelper.updateLastWorkoutIdOfExercise(this);
        }
        else
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
    }

    public int getMaxEffort()
    {
        if (liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        return this.liftDbHelper.getMaxEffortByExercise(this.exerciseId);
    }

    public int getMaxWeight()
    {
        if (liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        return this.liftDbHelper.getMaxWeightByExercise(this.exerciseId);
    }

    public int getTrainingWeight()
    {
        if (liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        return this.liftDbHelper.selectTrainingWeight(this.exerciseId);
    }

    public void updateTrainingWeight(int training_weight)
    {
        if (liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        this.liftDbHelper.updateTrainingWeight(this.exerciseId, training_weight);
    }

    /**
     * Deletes the exercise from the database.
     */
    void delete()
    {
        if (liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }
        this.liftDbHelper.deleteExercise(this);
    }

    class ExerciseNotCorrectlyInstantiated extends RuntimeException
    {
        public ExerciseNotCorrectlyInstantiated(String message)
        {
            super(message);
        }
    }

    class ExerciseAlreadyExistsException extends RuntimeException
    {

        public ExerciseAlreadyExistsException(String message)
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