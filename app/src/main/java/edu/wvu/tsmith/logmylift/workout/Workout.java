package edu.wvu.tsmith.logmylift.workout;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.exercise.Exercise;

/**
 * Created by Tommy Smith on 3/19/2017.
 * Interface to create and modify workouts. Each workout consists of one or more lifts,
 * the start date, and possibly a description. Workouts may be continued on separate
 * instances by the users; i.e., they can either be constructed and inserted into the database or
 * reconstructed from a pre-existing entry in the database.
 *
 * Optionally, a workout may contain a similar exercises algorithm. When enabled, this algorithm will
 * use the exercises performed during the lifts in the workout to find other exercises often done in
 * the same workouts.
 * @author Tommy Smith
 */
public class Workout implements Parcelable
{
    private final long workout_id;
    private String description;
    private ArrayList<Long> lift_ids = new ArrayList<>();
    private final Date start_date;
    private final boolean enable_similar_exercises_algorithm;
    private FindSimilarExercisesAlgorithm similar_exercises_algorithm;

    /**
     * Construct a new workout with a description and insert it into the database.
     * A workout ID and start date will be assigned to it.
     * @param lift_db_helper    SQLite database helper.
     * @param description       User-assigned description of the workout.
     */
    public Workout(LiftDbHelper lift_db_helper, String description)
    {
        this.start_date = new Date();
        this.description = description;
        this.workout_id = lift_db_helper.insertWorkout(this);
        this.similar_exercises_algorithm = new FindSimilarExercisesAlgorithm();
        this.enable_similar_exercises_algorithm = true;
    }

    /**
     * Construct an existing workout from parts. Obtaining the parts of the workout
     * from its ID is done from the database. This constructor just serves to assign
     * the values to the workout object.
     * @param lift_db_helper    The SQLite database helper.
     * @param workout_id        The unique ID of the workout from the SQLite database.
     * @param description       The user-assigned description of the workout. Not
     *                          required. If it is not set, then it will be set as
     *                          the start date.
     * @param lift_ids          An array of IDs uniquely identifying the lifts that
     *                          have occurred during the workout.
     * @param start_date        The start date of the workout.
     * @param enable_similar_exercises_algorithm    Determines whether the similar exercises algorithm
     *                                              should be enabled for the workout. When enabled,
     *                                              any exercises done during the workout will be used
     *                                              to calculate likely future exercises.
     */
    public Workout(
            LiftDbHelper lift_db_helper,
            long workout_id,
            String description,
            ArrayList<Long> lift_ids,
            Date start_date,
            boolean enable_similar_exercises_algorithm)
    {
        this.workout_id = workout_id;
        this.description = description;
        this.lift_ids = lift_ids;
        this.start_date = start_date;
        this.similar_exercises_algorithm = new FindSimilarExercisesAlgorithm();
        this.enable_similar_exercises_algorithm = enable_similar_exercises_algorithm;

        if (this.enable_similar_exercises_algorithm)
        {
            this.similar_exercises_algorithm.setInfo(lift_db_helper, lift_ids);
        }
    }

    // Public read-only access to members.
    public Date getStartDate() { return this.start_date; }
    public String getReadableStartDate() { return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this.start_date); }
    public String getDescription() { return this.description; }
    public long getWorkoutId() { return this.workout_id; }

    /**
     * Gets a list of exercise IDs similar to the exercises done during the workout. See the SimilarExercisesAlgorithm
     * class documentation for more information on how the algorithm itself works.
     * @return A list of similar exercise IDs.
     */
    ArrayList<Long> getSimilarExercises()
    {
        if (this.enable_similar_exercises_algorithm)
        {
            return similar_exercises_algorithm.getSimilarExercises();
        }
        else
        {
            // The algorithm is not enabled, so just return an empty list.
            return new ArrayList<>();
        }
    }

    /**
     * Get a list of the lifts done during the workout.
     * @param lift_db_helper    A SQLite database helper.
     * @return  A list of the lifts.
     */
    ArrayList<Lift> getLifts(LiftDbHelper lift_db_helper)
    {
        return lift_db_helper.selectWorkoutHistoryLifts(this);
    }

    /**
     * Add a lift to the workout and return the lift.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise          The exercise of the new lift.
     * @param reps              The number of reps of the new lift.
     * @param weight            The weight of the new lift.
     * @param comment           A comment for the lift.
     */
    Lift addLift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, String comment) {
        Lift new_lift =  new Lift(lift_db_helper, exercise, reps, weight, this.workout_id, comment);
        this.lift_ids.add(0, new_lift.getLiftId());

        if (this.enable_similar_exercises_algorithm)
        {
            this.similar_exercises_algorithm.addInfo(lift_db_helper, exercise.getExerciseId());
        }

        return new_lift;
    }

    /**
     * Readd a lift to the workout without adding it back into the database.
     * @param lift          Lift to readd.
     * @param old_position  The position in which to re-add the lift.
     */
    void reAddLift(Lift lift, int old_position)
    {
        this.lift_ids.add(old_position, lift.getLiftId());
    }

    /**
     * Remove a lift from memory. Deleting a lift from the database is done separately because that
     * operation is slower, so to undo a deletion, it is only removed from memory until it is sure
     * that the lift should be deleted.
     * @param lift_id   ID of the lift to remove.
     */
    void removeLiftInMemory(long lift_id)
    {
        this.lift_ids.remove(lift_id);
        // Remove the information from the similar exercises algorithm.
        // Right now, this doesn't do anything, so don't bother getting the exercise ID out.
        // this.similar_exercises_algorithm.removeInfo();
    }

    /**
     * Set the description of the workout.
     * @param lift_db_helper    SQLite database helper.
     * @param description       The new description of the workout.
     */
    public void setDescription(LiftDbHelper lift_db_helper, String description) {
        this.description = description;
        lift_db_helper.updateDescriptionOfWorkout(this);
    }

    // Required to make the workout parcelable.
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes the workout to a parcel. This allows the workout to be passed between activities, e.g.,
     * into the workout detail activity from the workout list activity.
     * @param dest  The destination parcel.
     * @param flags The parcel flags.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write each member to the parcel.
        dest.writeLong(this.workout_id);
        dest.writeString(this.description);
        dest.writeSerializable(this.lift_ids);
        dest.writeSerializable(this.start_date);
        dest.writeInt((int) (this.enable_similar_exercises_algorithm ? 1 : 0));

        // The similar exercise algorithm is, itself, parcelable.
        dest.writeParcelable(this.similar_exercises_algorithm, flags);
    }

    /**
     * Creates the workout from a parcel.
     */
    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>()
    {
        @Override
        public Workout createFromParcel(Parcel source) {
            return new Workout(source);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    /**
     * Workout constructor from a parcel.
     * @param source    The parcel from which to create a workout.
     */
    private Workout(Parcel source)
    {
        // Read in each member from the parcel.
        this.workout_id = source.readLong();
        this.description = source.readString();
        this.lift_ids = (ArrayList<Long>) source.readSerializable();
        this.start_date = (Date) source.readSerializable();
        this.enable_similar_exercises_algorithm = (source.readInt() != 0);

        // The similar exercises algorithm itself is a parcel.
        this.similar_exercises_algorithm = source.readParcelable(FindSimilarExercisesAlgorithm.class.getClassLoader());
    }
}
