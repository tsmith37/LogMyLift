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
 * the start date, and possibly a description. Workouts may be continue on separate
 * instances by the users and support the ability to be deleted from the database.
 * @author Tommy Smith
 */
public class Workout implements Parcelable {
    private final long workout_id;
    private String description;
    private ArrayList<Long> lift_ids = new ArrayList<>();
    private final Date start_date;

    /**
     * Construct a new workout with a description and insert it into the database.
     * A workout ID and start date will be assigned to it.
     * @param lift_db_helper    SQLite database helper.
     * @param description       User-assigned description of the workout.
     */
    public Workout(LiftDbHelper lift_db_helper, String description) {
        this.start_date = new Date();
        this.description = description;
        this.workout_id = lift_db_helper.insertWorkout(this);
    }

    /**
     * Construct an existing workout from parts. Obtaining the parts of the workout
     * from its ID is done from the database. This constructor just serves to assign
     * the values to the workout object.
     * @param workout_id        The unique ID of the workout from the SQLite database.
     * @param description       The user-assigned description of the workout. Not
     *                          required. If it is not set, then it will be set as
     *                          the start date.
     * @param lift_ids          An array of IDs uniquely identifying the lifts that
     *                          have occured during the workout.
     * @param start_date        The start date of the workout.
     */
    public Workout(long workout_id, String description, ArrayList<Long> lift_ids, Date start_date) {
        this.workout_id = workout_id;
        this.description = description;
        this.lift_ids = lift_ids;
        this.start_date = start_date;
    }

    // Public read-only access to members.
    public Date getStartDate() { return this.start_date; }
    public String getReadableStartDate() { return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this.start_date); }
    public String getDescription() { return this.description; }
    public long getWorkoutId() { return this.workout_id; }

    /**
     * Get an ArrayList of the lifts done during the workout.
     * @return  An ArrayList of the lifts.
     */
    ArrayList<Lift> getLifts(LiftDbHelper lift_db_helper)
    {
        return lift_db_helper.selectWorkoutHistoryLifts(this);
    }

    Lift getLift(LiftDbHelper lift_db_helper, int position)
    {
        return lift_db_helper.selectLiftFromLiftId(lift_ids.get(position));
    }

    /**
     * Add a lift to the workout and return the lift.
     * @param exercise  The exercise of the new lift.
     * @param reps      The number of reps of the new lift.
     * @param weight    The weight of the new lift.
     * @param comment   A comment for the lift.
     */
    Lift addLift(LiftDbHelper lift_db_helper, Exercise exercise, int reps, int weight, String comment) {
        Lift new_lift =  new Lift(lift_db_helper, exercise, reps, weight, this.workout_id, comment);
        this.lift_ids.add(0, new_lift.getLiftId());
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
    }

    /**
     * Set the description of the workout.
     * @param description   The new description of the workout.
     */
    public void setDescription(LiftDbHelper lift_db_helper, String description) {
        this.description = description;
        lift_db_helper.updateDescriptionOfWorkout(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.workout_id);
        dest.writeString(this.description);
        dest.writeSerializable(this.lift_ids);
        dest.writeSerializable(this.start_date);
    }

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

    private Workout(Parcel source)
    {
        this.workout_id = source.readLong();
        this.description = source.readString();
        this.lift_ids = (ArrayList<Long>) source.readSerializable();
        this.start_date = (Date) source.readSerializable();
    }
}
