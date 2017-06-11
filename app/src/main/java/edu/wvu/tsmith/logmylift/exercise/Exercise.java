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

public class Exercise {
    private String description;
    private final long exercise_id;
    private long max_lift_id;
    private String name;
    private long last_workout_id;
    private final LiftDbHelper lift_db;

    /**
     * Constructor of a new exercise. Given the SQLite database helper, name, and description
     * of the exercise, the exercise is instantiated, added into the database, and the exercise
     * ID is set.
     * @param lift_db_helper    SQLite database helper.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     */
    public Exercise(LiftDbHelper lift_db_helper, String name, String description) {
        // TODO: Should we check if an exercise with this name already exists?
        this.name = name;
        this.description = description;
        this.lift_db = lift_db_helper;
        this.exercise_id = lift_db_helper.insertExercise(this);
    }

    /**
     * Construct a new exercise with no description. Otherwise, the same as the previous
     * constructor.
     * @param lift_db_helper    SQLite database helper.
     * @param name              Name of the exercise.
     */
    public Exercise(LiftDbHelper lift_db_helper, String name) {
        this(lift_db_helper, name, "");
    }

    /**
     * Construct a previously existing exercise from its pieces. In this instance, the exercise ID
     * already exists in the database, so don't add it again.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise_id       Unique exercise ID.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     * @param max_lift_id       Lift of the maximum effort of the exercise.
     * @param last_workout_id   Most recent workout ID that the exercise was performed.
     */
    public Exercise(LiftDbHelper lift_db_helper, long exercise_id, String name, String description, long max_lift_id, long last_workout_id) {
        this.lift_db = lift_db_helper;
        this.exercise_id = exercise_id;
        this.name = name;
        this.description = description;
        this.max_lift_id = max_lift_id;
        this.last_workout_id = last_workout_id;
    }

    // Public access to read-only members.
    public long getExerciseId() { return this.exercise_id; }
    public long getLastWorkoutId() { return this.last_workout_id; }
    public long getMaxLiftId() { return this.max_lift_id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    /**
     * Updates the description of the exercise.
     * @param description   Updated description.
     */
    void setDescription(String description) {
        this.description = description;
        lift_db.updateDescriptionOfExercise(this);
    }

    /**
     * Updates the most recent workout ID of the exercise.
     * @param last_workout_id   Workout ID.
     */
    public void setLastWorkoutId(long last_workout_id) {
        this.last_workout_id = last_workout_id;
        lift_db.updateLastWorkoutIdOfExercise(this);
    }

    /**
     * Updates the ID of the maximum effort lift of the exercise.
     * @param max_lift_id   The ID of the maximum effort lift.
     */
    public void setMaxLiftId(long max_lift_id) {
        this.max_lift_id = max_lift_id;
        lift_db.updateMaxLiftIdOfExercise(this);
    }

    /**
     * Sets the name of this exercise.
     * @param name  The new name of the exercise.
     */
    public void setName(String name) {
        this.name = name;
        lift_db.updateNameOfExercise(this);
    }

    /**
     * Clears the ID of the maximum effort lift. This should only happen if the only instance of a
     * lift with this exercise is deleted.
     */
    public void clearMaxLiftId() {
        this.max_lift_id = -1;
        lift_db.updateMaxLiftIdOfExerciseToNull(this);
    }
}