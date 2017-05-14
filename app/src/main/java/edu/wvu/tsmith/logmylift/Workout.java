package edu.wvu.tsmith.logmylift;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tmssm on 3/19/2017.
 * Interface to create and modify workouts. Each workout consists of one or more lifts,
 * the start date, and possibly a description. Workouts may be continue on separate
 * instances by the users and support the ability to be deleted from the database.
 * @author Tommy Smith
 */
class Workout {
    private long workout_id;
    private String description;
    private ArrayList<Long> lift_ids = new ArrayList<>();
    private Date start_date;
    private LiftDbHelper lift_db_helper;

    /**
     * Construct a new workout with a description and insert it into the database.
     * A workout ID and start date will be assigned to it.
     * @param lift_db_helper    SQLite database helper.
     * @param description       User-assigned description of the workout.
     */
    Workout(LiftDbHelper lift_db_helper, String description) {
        this.start_date = new Date();
        this.description = description;
        this.lift_db_helper = lift_db_helper;
        this.workout_id = lift_db_helper.insertWorkout(this);
    }

    /**
     * Construct a new workout and insert it into the database. This workout does not
     * have a description. The current date will be used as the description. A workout
     * ID and start date will be assigned to it.
     * @param lift_db_helper    SQLite database helper.
     */
    Workout(LiftDbHelper lift_db_helper) {
        this(lift_db_helper, new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
    }

    /**
     * Construct an existing workout from parts. Obtaining the parts of the workout
     * from its ID is done from the database. This constructor just serves to assign
     * the values to the workout object.
     * @param lift_db_helper    SQLite database helper.
     * @param workout_id        The unique ID of the workout from the SQLite database.
     * @param description       The user-assigned description of the workout. Not
     *                          required. If it is not set, then it will be set as
     *                          the start date.
     * @param lift_ids          An array of IDs uniquely identifying the lifts that
     *                          have occured during the workout.
     * @param start_date        The start date of the workout.
     */
    Workout(LiftDbHelper lift_db_helper, long workout_id, String description, ArrayList<Long> lift_ids, Date start_date) {
        this.lift_db_helper = lift_db_helper;
        this.workout_id = workout_id;
        this.description = description;
        this.lift_ids = lift_ids;
        this.start_date = start_date;
    }

    // Public read-only access to members.
    Date getStartDate() { return this.start_date; }
    String getDescription() { return this.description; }
    ArrayList getLiftIds() { return this.lift_ids; }
    long getWorkoutId() { return this.workout_id; }

    /**
     * Add a lift to the workout and return the lift.
     * @param exercise  The exercise of the new lift.
     * @param reps      The number of reps of the new lift.
     * @param weight    The weight of the new lift.
     * @return          A newly created lift object with the contents described.
     */
    Lift AddLift(Exercise exercise, int reps, int weight) {
        Lift new_lift =  new Lift(lift_db_helper, exercise, reps, weight, this.workout_id);
        this.lift_ids.add(new_lift.getLiftId());
        return new_lift;
    }
}
