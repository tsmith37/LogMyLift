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
    private String description;
    private final long exercise_id;
    private String name;
    private long last_workout_id;

    // Depricated:
    //private long max_lift_id;


    /**
     * Constructor of a new exercise. Given the SQLite database helper, name, and description
     * of the exercise, the exercise is instantiated, added into the database, and the exercise
     * ID is set.
     * @param lift_db_helper    SQLite database helper.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     */
    public Exercise(LiftDbHelper lift_db_helper, String name, String description) throws ExerciseAlreadyExistsException
    {
        // Check if the exercise name already exists.
        if (lift_db_helper.exerciseNameExists(name))
        {
            throw new ExerciseAlreadyExistsException(name + " already exists.");
        }
        else
        {
            this.name = name;
            this.description = description;
            this.exercise_id = lift_db_helper.insertExercise(this);
        }
    }

    /**
     * Construct a previously existing exercise from its pieces. In this instance, the exercise ID
     * already exists in the database, so don't add it again.
     * @param exercise_id       Unique exercise ID.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     * @param last_workout_id   Most recent workout ID that the exercise was performed.
     */
    public Exercise(long exercise_id, String name, String description, long last_workout_id)
    {
        this.exercise_id = exercise_id;
        this.name = name;
        this.description = description;
        this.last_workout_id = last_workout_id;
    }

    // Public access to read-only members.
    public long getExerciseId() { return this.exercise_id; }
    public long getLastWorkoutId() { return this.last_workout_id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    /**
     * Sets the name of this exercise.
     * @param name  The new name of the exercise.
     */
    void setName(LiftDbHelper lift_db_helper, String name)
    {
        this.name = name;
        lift_db_helper.updateNameOfExercise(this);
    }

    /**
     * Updates the description of the exercise.
     * @param description   Updated description.
     */
    void setDescription(LiftDbHelper lift_db_helper, String description)
    {
        this.description = description;
        lift_db_helper.updateDescriptionOfExercise(this);
    }

    /**
     * Updates the most recent workout ID of the exercise.
     * @param last_workout_id   Workout ID.
     */
    public void setLastWorkoutId(LiftDbHelper lift_db_helper, long last_workout_id)
    {
        this.last_workout_id = last_workout_id;
        lift_db_helper.updateLastWorkoutIdOfExercise(this);
    }

    /**
     * Deletes the exercise from the database.
     * @param lift_db_helper    The database helper.
     */
    void delete(LiftDbHelper lift_db_helper)
    {
        lift_db_helper.deleteExercise(this);
    }

    class ExerciseAlreadyExistsException extends RuntimeException
    {

        public ExerciseAlreadyExistsException(String message)
        {
            super(message);
        }
    }
}