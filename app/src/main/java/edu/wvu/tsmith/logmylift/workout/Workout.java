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
    private boolean toCreate;
    private long workoutId;
    private String description;
    private ArrayList<Long> liftIds = new ArrayList<>();
    private Date startDate;
    private LiftDbHelper liftDbHelper;
    private boolean enableSimilarExercisesAlgorithm;
    private FindSimilarExercisesAlgorithm similarExercisesAlgorithm;

    private static java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static class Builder
    {
        // Optional:
        private long workoutId = -1;
        private boolean toCreate = false;
        private Date startDate = new Date();
        private String description = "";
        private ArrayList<Long> liftIds = new ArrayList<>();
        private LiftDbHelper liftDbHelper;
        private boolean enableSimilarExercisesAlgorithm = true;
        private FindSimilarExercisesAlgorithm similarExercisesAlgorithm = null;

        public Builder() {}

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

        public Builder startDate(Date value)
        {
            startDate = value;
            return this;
        }

        public Builder description(String value)
        {
            description = value;
            return this;
        }

        public Builder liftIds(ArrayList<Long> value)
        {
            liftIds = value;
            return this;
        }

        public Builder liftDbHelper(LiftDbHelper value)
        {
            liftDbHelper = value;
            return this;
        }

        public Builder enableSimilarExercisesAlgorithm(boolean value)
        {
            enableSimilarExercisesAlgorithm = value;
            return this;
        }

        public Builder similarExercisesAlgorithm(FindSimilarExercisesAlgorithm value)
        {
            similarExercisesAlgorithm = value;
            return this;
        }

        public Workout build()
        {
            return new Workout(this);
        }
    }

    private Workout(Builder builder)
    {
        this.toCreate = builder.toCreate;
        this.workoutId = builder.workoutId;
        this.description = builder.description;
        this.liftIds = builder.liftIds;
        this.startDate = builder.startDate;
        this.liftDbHelper = builder.liftDbHelper;
        this.enableSimilarExercisesAlgorithm = builder.enableSimilarExercisesAlgorithm;
        this.similarExercisesAlgorithm = builder.similarExercisesAlgorithm;

        this.initWorkout();

    }

    private void initWorkout()
    {
        if (this.toCreate)
        {
            this.createWorkout();
        }
        else
        {
            if (this.workoutId == -1)
            {
                throw new WorkoutNotCorrectlyInstantiated("Workout not correctly instantiated.");
            }
        }

        this.createSimilarExercisesAlgorithm();
    }

    private void createWorkout()
    {
        if (this.workoutId != -1)
        {
            throw new WorkoutNotCorrectlyInstantiated("Workout not correctly instantiated.");
        }

        this.workoutId = liftDbHelper.insertWorkout(this);
    }

    private void createSimilarExercisesAlgorithm()
    {
        if (this.similarExercisesAlgorithm == null && this.enableSimilarExercisesAlgorithm)
        {
            this.similarExercisesAlgorithm = new FindSimilarExercisesAlgorithm();
            if (!this.toCreate)
            {
                this.similarExercisesAlgorithm.setInfo(this.liftDbHelper, this.liftIds);
            }
        }
    }

    // Public read-only access to members.
    public Date getStartDate() { return this.startDate; }
    public String getReadableStartDate() { return date_format.format(this.startDate); }
    public String getDescription() { return this.description; }
    public long getWorkoutId() { return this.workoutId; }

    public void setLiftDbHelper(LiftDbHelper value)
    {
        this.liftDbHelper = value;
    }

    /**
     * Gets a list of exercise IDs similar to the exercises done during the workout. See the SimilarExercisesAlgorithm
     * class documentation for more information on how the algorithm itself works.
     * @return A list of similar exercise IDs.
     */
    ArrayList<Long> getSimilarExercises()
    {
        if (this.enableSimilarExercisesAlgorithm)
        {
            return similarExercisesAlgorithm.getSimilarExercises();
        }
        else
        {
            // The algorithm is not enabled, so just return an empty list.
            return new ArrayList<>();
        }
    }

    /**
     * Get a list of the lifts done during the workout.
     * @return  A list of the lifts.
     */
    ArrayList<Lift> getLifts()
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        return this.liftDbHelper.selectWorkoutHistoryLifts(this);
    }

    public void initExistingLifts()
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        this.liftIds = this.liftDbHelper.selectLiftsByWorkoutId(this.workoutId);
    }

    /**
     * Add a lift to the workout and return the lift.
     * @param exercise          The exercise of the new lift.
     * @param reps              The number of reps of the new lift.
     * @param weight            The weight of the new lift.
     * @param comment           A comment for the lift.
     */
    public Lift addLift(Exercise exercise, int reps, int weight, String comment)
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        Lift new_lift =  new Lift.Builder()
                .liftDbHelper(this.liftDbHelper)
                .exercise(exercise)
                .reps(reps)
                .weight(weight)
                .workoutId(this.workoutId)
                .comment(comment)
                .toCreate(true)
                .build();

        this.liftIds.add(0, new_lift.getLiftId());

        if (this.enableSimilarExercisesAlgorithm)
        {
            this.similarExercisesAlgorithm.addInfo(this.liftDbHelper, exercise.getExerciseId());
        }

        return new_lift;
    }

    public String getReadableDuration()
    {
        return convertDurationInMsToReadableString(getDurationInMs());
    }

    public String getReadableTimePerSet()
    {
        long duration_in_ms = this.getDurationInMs();
        int lifts_performed = this.getLiftsPerformedCount();
        long time_per_set_in_ms = ((lifts_performed > 0) ? (duration_in_ms / lifts_performed) : 0);
        return convertDurationInMsToReadableString(time_per_set_in_ms);
    }
    private long getDurationInMs()
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        return this.liftDbHelper.getWorkoutDurationInMs(this);
    }

    private static String convertDurationInMsToReadableString(long duration_in_ms)
    {
        long duration = duration_in_ms;
        int ms_in_one_s = 1000;
        int s_in_one_min = 60;
        int ms_in_one_min = ms_in_one_s * s_in_one_min;
        int min_in_one_hour = 60;
        int ms_in_one_hour = ms_in_one_min * min_in_one_hour;

        int duration_in_hours = ((int) duration / ms_in_one_hour);
        duration -= (duration_in_hours * ms_in_one_hour);

        int duration_in_mins = ((int) duration / ms_in_one_min);
        duration -= (duration_in_mins * ms_in_one_min);

        int duration_in_s = ((int) duration / ms_in_one_s);

        return String.format("%02d:%02d:%02d", duration_in_hours, duration_in_mins, duration_in_s);
    }

    public int getLiftsPerformedCount()
    {
        return this.liftIds.size();
    }

    /**
     * Readd a lift to the workout without adding it back into the database.
     * @param lift          Lift to readd.
     * @param old_position  The position in which to re-add the lift.
     */
    void reAddLift(Lift lift, int old_position)
    {
        this.liftIds.add(old_position, lift.getLiftId());
    }

    /**
     * Remove a lift from memory. Deleting a lift from the database is done separately because that
     * operation is slower, so to undo a deletion, it is only removed from memory until it is sure
     * that the lift should be deleted.
     * @param lift_id   ID of the lift to remove.
     */
    void removeLiftInMemory(long lift_id)
    {
        this.liftIds.remove(lift_id);
        // Remove the information from the similar exercises algorithm.
        // Right now, this doesn't do anything, so don't bother getting the exercise ID out.
        // this.similar_exercises_algorithm.removeInfo();
    }

    /**
     * Set the description of the workout.
     * @param description       The new description of the workout.
     */
    public void setDescription(String description)
    {
        if (this.liftDbHelper == null)
        {
            throw new LiftDbHelperNotInstantiated("LiftDbHelper not instantiated.");
        }

        this.description = description;
        liftDbHelper.updateDescriptionOfWorkout(this);
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
        dest.writeLong(this.workoutId);
        dest.writeString(this.description);
        dest.writeSerializable(this.liftIds);
        dest.writeSerializable(this.startDate);
        dest.writeInt((int) (this.enableSimilarExercisesAlgorithm ? 1 : 0));

        // The similar exercise algorithm is, itself, parcelable.
        dest.writeParcelable(this.similarExercisesAlgorithm, flags);
    }

    /**
     * Creates the workout from a parcel.
     */
    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>()
    {
        @Override
        public Workout createFromParcel(Parcel source)
        {
            return new Builder()
                    .workoutId(source.readLong())
                    .toCreate(false)
                    .description(source.readString())
                    .liftIds((ArrayList<Long>) source.readSerializable())
                    .startDate((Date) source.readSerializable())
                    .enableSimilarExercisesAlgorithm(source.readInt() != 0)
                    .similarExercisesAlgorithm(source.readParcelable(FindSimilarExercisesAlgorithm.class.getClassLoader()))
                    .build();
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    class WorkoutNotCorrectlyInstantiated extends RuntimeException
    {
        public WorkoutNotCorrectlyInstantiated(String message)
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
