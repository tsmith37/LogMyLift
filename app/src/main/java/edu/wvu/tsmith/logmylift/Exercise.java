package edu.wvu.tsmith.logmylift;

/**
 * Created by tmssm on 3/19/2017.
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

class Exercise {
    private String description;
    private long exercise_id;
    private long max_lift_id;
    private String name;
    private long last_workout_id;
    private LiftDbHelper lift_db;

    /**
     * Constructor of a new exercise. Given the SQLite database helper, name, and description
     * of the exercise, the exercise is instantiated, added into the database, and the exercise
     * ID is set.
     * @param lift_db_helper    SQLite database helper.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     */
    Exercise(LiftDbHelper lift_db_helper, String name, String description) {
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
    Exercise(LiftDbHelper lift_db_helper, String name) {
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
    Exercise(LiftDbHelper lift_db_helper, long exercise_id, String name, String description, long max_lift_id, long last_workout_id) {
        this.lift_db = lift_db_helper;
        this.exercise_id = exercise_id;
        this.name = name;
        this.description = description;
        this.max_lift_id = max_lift_id;
        this.last_workout_id = last_workout_id;
    }

    // Public access to read-only members.
    long getExerciseId() { return this.exercise_id; }
    String getName() { return this.name; }
    String getDescription() { return this.description; }

    /**
     * Updates the description of the exercise.
     * @param description   Updated description.
     */
    void setDescription(String description) {
        this.description = description;
        lift_db.updateDescriptionOfExercise(this, this.description);
    }

    /**
     * Updates the most recent workout ID of the exercise.
     * @param last_workout_id   Workout ID.
     */
    void setLastWorkoutId(long last_workout_id) {
        this.last_workout_id = last_workout_id;
        lift_db.updateLastWorkoutIdOfExercise(this, this.last_workout_id);
    }

    /**
     * Updates the ID of the maximum effort lift of the exercise.
     * @param max_lift_id
     */
    void setMaxLiftId(long max_lift_id) {
        this.max_lift_id = max_lift_id;
        lift_db.updateMaxLiftIdOfExercise(this, this.max_lift_id);
    }
}