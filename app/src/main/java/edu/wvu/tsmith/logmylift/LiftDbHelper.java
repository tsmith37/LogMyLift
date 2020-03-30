package edu.wvu.tsmith.logmylift;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.exercise.SelectExerciseHistoryParams;
import edu.wvu.tsmith.logmylift.upgradability.MaxWeightPopulator;
import edu.wvu.tsmith.logmylift.workout.LoadWorkoutHistoryListParams;
import edu.wvu.tsmith.logmylift.workout.Workout;

/**
 * Created by Tommy Smith on 3/19/2017.
 * The LiftDbHelper class provides a public interface to the SQLite database used by the application.
 * This is helpful because all database operations should be central to this class.
 * @author Tommy Smith
 */
public class LiftDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LogMyLiftDb.db";
    private static final int DATABASE_VERSION = 4;

    public static final String EXERCISE_TABLE_NAME = "Exercise";
    public static final String EXERCISE_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String EXERCISE_COLUMN_DESCRIPTION = "Description";
    public static final String EXERCISE_COLUMN_LAST_WORKOUT_ID = "LastWorkoutId";
    public static final String EXERCISE_COLUMN_NAME = "Name";

    // Depricated:
    //public static final String EXERCISE_COLUMN_MAX_LIFT_ID = "MaxLiftId";


    public static final String LIFT_TABLE_NAME = "Lift";
    public static final String LIFT_COLUMN_LIFT_ID = "LiftId";
    public static final String LIFT_COLUMN_COMMENT = "Comment";
    public static final String LIFT_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String LIFT_COLUMN_REPS = "Reps";
    public static final String LIFT_COLUMN_START_DATE = "StartDate";
    public static final String LIFT_COLUMN_WEIGHT = "Weight";
    public static final String LIFT_COLUMN_WORKOUT_ID = "WorkoutId";

    public static final String WORKOUT_TABLE_NAME = "Workout";
    public static final String WORKOUT_COLUMN_WORKOUT_ID = "WorkoutId";
    public static final String WORKOUT_COLUMN_DESCRIPTION = "Description";
    public static final String WORKOUT_COLUMN_START_DATE = "StartDate";

    public static final String SELECTED_EXERCISE_TABLE_NAME = "SelectedExercise";
    public static final String SELECTED_EXERCISE_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String SELECTED_EXERCISE_COLUMN_DATE = "Date";

    public static final String MAX_WEIGHT_TABLE_NAME = "MaxWeight";
    public static final String MAX_WEIGHT_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String MAX_WEIGHT_COLUMN_TRAINING_WEIGHT = "TrainingWeight";
    public static final String MAX_WEIGHT_COLUMN_EFFORT_WEIGHT = "MaxEffortWeight";
    public static final String MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID = "MaxEffortLiftId";
    public static final String MAX_WEIGHT_COLUMN_LIFT_WEIGHT = "MaxLiftWeight";
    public static final String MAX_WEIGHT_COLUMN_LIFT_LIFT_ID = "MaxLiftLiftId";

    private static final String CREATE_TABLE_EXERCISE =
            "CREATE TABLE Exercise (" +
                    "ExerciseId INTEGER PRIMARY KEY," +
                    "Name TEXT," +
                    "Description TEXT," +
                    "MaxLiftId INTEGER," +
                    "LastWorkoutId INTEGER," +
                    "FOREIGN KEY(MaxLiftId) REFERENCES Lift(LiftId)," +
                    "FOREIGN KEY(LastWorkoutId) REFERENCES Workout(WorkoutId));";

    private static final String CREATE_TABLE_LIFT =
            "CREATE TABLE Lift (" +
                    "LiftId INTEGER PRIMARY KEY," +
                    "Comment TEXT," +
                    "ExerciseId INTEGER," +
                    "Reps INTEGER," +
                    "StartDate INTEGER," +
                    "Weight INTEGER," +
                    "WorkoutId INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId)," +
                    "FOREIGN KEY(WorkoutId) REFERENCES Workout(WorkoutId));";

    private static final String CREATE_LIFT_WORKOUT_ID_INDEX =
            "CREATE INDEX LiftWorkoutId " +
                    "ON Lift(WorkoutId);";

    private static final String CREATE_TABLE_WORKOUT =
            "CREATE TABLE Workout (" +
                    "WorkoutId INTEGER PRIMARY KEY," +
                    "Description TEXT," +
                    "StartDate INTEGER);";

    private static final String CREATE_WORKOUT_START_DATE_INDEX =
            "CREATE INDEX WorkoutStartDate " +
                    "ON Workout(StartDate);";

    private static final String CREATE_TABLE_SELECTED_EXERCISE =
            "CREATE TABLE SelectedExercise (" +
                    "ExerciseId INTEGER," +
                    "Date INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId));";

    private static final String CREATE_TABLE_MAX_WEIGHT =
            "CREATE TABLE MaxWeight (" +
                    "ExerciseId INTEGER PRIMARY KEY," +
                    "TrainingWeight INTEGER," +
                    "MaxEffortWeight INTEGER," +
                    "MaxEffortLiftId INTEGER," +
                    "MaxLiftWeight INTEGER," +
                    "MaxLiftLiftId INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId)," +
                    "FOREIGN KEY(MaxEffortLiftId) REFERENCES Lift(LiftId)," +
                    "FOREIGN KEY(MaxLiftLiftId) REFERENCES Lift(LiftId));";

    /**
     * Default constructor for the SQLiteOpenHelper class.
     *
     * @param context Application context.
     */
    public LiftDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * On create-as required by the parent class-creates the necessary tables.
     *
     * @param db Database to use.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_WORKOUT);
        db.execSQL(CREATE_TABLE_LIFT);
        db.execSQL(CREATE_TABLE_EXERCISE);
        db.execSQL(CREATE_TABLE_SELECTED_EXERCISE);
        db.execSQL(CREATE_TABLE_MAX_WEIGHT);
    }

    /**
     * On upgrade-also required by the parent class-is not used yet.
     *
     * @param db         Database to use.
     * @param oldVersion Old version of the database.
     * @param newVersion New version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < 2 && newVersion >= 2)
        {
            db.execSQL(CREATE_TABLE_SELECTED_EXERCISE);
        }

        if (oldVersion < 3 && newVersion >= 3)
        {
            db.execSQL(CREATE_TABLE_MAX_WEIGHT);
            this.populateMaxWeightTable(db);
        }

        if (oldVersion < 4 && newVersion >= 4)
        {
            db.execSQL(CREATE_LIFT_WORKOUT_ID_INDEX);
            db.execSQL(CREATE_WORKOUT_START_DATE_INDEX);
        }
    }

    private void populateMaxWeightTable(SQLiteDatabase db)
    {
        MaxWeightPopulator populateMaxWeight = new MaxWeightPopulator(db);
        populateMaxWeight.run();
    }

    public void updateTrainingWeight(long exercise_id, int training_weight)
    {
        if (this.maxWeightRecordExistsByExercise(exercise_id))
        {
            this.updateFieldIntFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_TRAINING_WEIGHT, training_weight);
        }
        else
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues insert_values = new ContentValues();
            insert_values.put(MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id);
            insert_values.put(MAX_WEIGHT_COLUMN_TRAINING_WEIGHT, training_weight);
            db.insert(MAX_WEIGHT_TABLE_NAME, null, insert_values);
            db.close();
        }
    }

    public int selectTrainingWeight(long exercise_id)
    {
        if (!this.maxWeightRecordExistsByExercise(exercise_id))
        {
            return -1;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_TRAINING_WEIGHT};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        select_cursor.moveToFirst();
        return select_cursor.getInt(select_cursor.getColumnIndexOrThrow(MAX_WEIGHT_COLUMN_TRAINING_WEIGHT));
    }

    public boolean liftContainsMaxWeight(Lift lift)
    {
        // Check if a row exists in the max weight table for the exercise and lift ID.
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ? AND (" + MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID + " = ? OR " + MAX_WEIGHT_COLUMN_LIFT_LIFT_ID + " = ?)";
        long exercise_id = lift.getExercise().getExerciseId();
        String[] where_args = {Long.toString(exercise_id), Long.toString(lift.getLiftId()), Long.toString(lift.getLiftId())};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();
        return (id_in_db);
    }

    private boolean maxWeightRecordExistsByExercise(long exercise_id)
    {
        // Check if a row exists in the max weight table for the exercise.
        SQLiteDatabase db = this.getWritableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, MAX_WEIGHT_COLUMN_LIFT_WEIGHT};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();

        return id_in_db;
    }

    public Lift selectMaxEffortLiftByExercise(long exercise_id)
    {
        if (!this.maxWeightRecordExistsByExercise(exercise_id))
        {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        select_cursor.moveToFirst();
        long max_lift_id = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID));

        return this.selectLiftFromLiftId(max_lift_id);
    }

    public int getMaxEffortByExercise(long exercise_id)
    {
        if (!this.maxWeightRecordExistsByExercise(exercise_id))
        {
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_EFFORT_WEIGHT};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        select_cursor.moveToFirst();
        return select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(MAX_WEIGHT_COLUMN_EFFORT_WEIGHT));
    }

    public Lift selectHeaviestLiftByExercise(long exercise_id)
    {
        if (!this.maxWeightRecordExistsByExercise(exercise_id))
        {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_LIFT_LIFT_ID};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        select_cursor.moveToFirst();
        long max_lift_id = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(MAX_WEIGHT_COLUMN_LIFT_LIFT_ID));

        return this.selectLiftFromLiftId(max_lift_id);
    }

    public int getMaxWeightByExercise(long exercise_id)
    {
        if (!this.maxWeightRecordExistsByExercise(exercise_id))
        {
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {MAX_WEIGHT_COLUMN_EXERCISE_ID, MAX_WEIGHT_COLUMN_LIFT_WEIGHT};
        String WHERE = MAX_WEIGHT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                MAX_WEIGHT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        select_cursor.moveToFirst();
        return select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(MAX_WEIGHT_COLUMN_LIFT_WEIGHT));
    }

    public void updateMaxWeightTableWithLift(Lift new_lift)
    {
        // The row already exists, so update it if applicable.
        long exercise_id = new_lift.getExercise().getExerciseId();
        if (this.maxWeightRecordExistsByExercise(exercise_id))
        {
            // Update the max effort of the exercise if the max effort new lift is >= the max effort
            // in the max weight table.
            int current_max_effort_weight = this.getMaxEffortByExercise(exercise_id);
            if (new_lift.calculateMaxEffort() >= current_max_effort_weight)
            {
                this.updateFieldLongFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID, new_lift.getLiftId());
                this.updateFieldIntFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, new_lift.calculateMaxEffort());
            }

            // Update the max weight of the exercise if the weight of the new lift is >= the max weight
            // in the max weight table.
            int current_max_weight = this.getMaxWeightByExercise(exercise_id);
            if (new_lift.getWeight() >= current_max_weight)
            {
                this.updateFieldLongFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_LIFT_LIFT_ID, new_lift.getLiftId());
                this.updateFieldIntFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_LIFT_WEIGHT, new_lift.getWeight());
            }
        }
        else
        {
            // Insert a new row because none exists.
            this.createNewMaxWeightRecord(exercise_id, new_lift.getLiftId(), new_lift.calculateMaxEffort(), new_lift.getLiftId(), new_lift.getWeight());
        }
    }

    public void updateMaxWeightRecordByExercise(long exercise_id)
    {
        // Check if the exercise has ever been done.
        SelectExerciseHistoryParams.ExerciseListOrder by_max_effort = SelectExerciseHistoryParams.ExerciseListOrder.MAX_DESC;
        SelectExerciseHistoryParams select_exercise_history_params = new SelectExerciseHistoryParams(this.selectExerciseFromExerciseId(exercise_id), by_max_effort);
        ArrayList<Lift> exercise_history = this.selectExerciseHistoryLifts(select_exercise_history_params);

        if (0 == exercise_history.size())
        {
            // Delete the max weight record, if it exists. The exercise has never been done.
            this.deleteRowById(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id);
            return;
        }

        // Calculate the max effort lift and the max weight lift.
        Lift max_effort_lift = this.calculateMaxEffortLiftByExercise(exercise_id);
        int max_effort_weight = 0;
        if (max_effort_lift != null)
        {
            max_effort_weight = max_effort_lift.calculateMaxEffort();
        }

        Lift max_lift_lift = this.calculateHeaviestLiftByExercise(exercise_id);
        int max_lift_weight = 0;
        if (max_lift_lift != null)
        {
            max_lift_weight = max_lift_lift.getWeight();
        }

        // Check if a max weight record already exists.
        if (this.maxWeightRecordExistsByExercise(exercise_id))
        {
            // Update the existing max weight record.
            this.updateMaxWeightRecord(exercise_id, max_effort_lift.getLiftId(), max_effort_weight, max_lift_lift.getLiftId(), max_lift_weight);
        }
        else
        {
            // Create a new max weight record.
            this.createNewMaxWeightRecord(exercise_id, max_effort_lift.getLiftId(), max_effort_weight, max_lift_lift.getLiftId(), max_lift_weight);
        }
    }

    private void createNewMaxWeightRecord(long exercise_id, long max_effort_lift_id, int max_effort_weight, long max_weight_lift_id, int max_lift_weight)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id);
        insert_values.put(MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID, max_effort_lift_id);
        insert_values.put(MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, max_effort_weight);
        insert_values.put(MAX_WEIGHT_COLUMN_LIFT_LIFT_ID, max_weight_lift_id);
        insert_values.put(MAX_WEIGHT_COLUMN_LIFT_WEIGHT, max_lift_weight);
        db.insert(MAX_WEIGHT_TABLE_NAME, null, insert_values);
        db.close();
    }

    private void updateMaxWeightRecord(long exercise_id, long max_effort_lift_id, int max_effort_weight, long max_weight_lift_id, int max_lift_weight)
    {
        this.updateFieldLongFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_EFFORT_LIFT_ID, max_effort_lift_id);
        this.updateFieldIntFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_EFFORT_WEIGHT, max_effort_weight);
        this.updateFieldLongFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_LIFT_LIFT_ID, max_weight_lift_id);
        this.updateFieldIntFromId(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise_id, MAX_WEIGHT_COLUMN_LIFT_WEIGHT, max_lift_weight);
    }

    /**
     * Deletes a row in the specified table based on the ID.
     * @param table_name     Table to delete from.
     * @param id_column_name ID column name.
     * @param id             ID whose row to delete.
     */
    private void deleteRowById(String table_name, String id_column_name, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String WHERE = id_column_name + " = ?";
        String[] where_args = {Long.toString(id)};
        db.delete(table_name, WHERE, where_args);
        db.close();
    }

    /**
     * Deletes a lift from the database.
     * @param lift  Lift to delete.
     */
    public void deleteLift(Lift lift)
    {
        deleteRowById(LIFT_TABLE_NAME, LIFT_COLUMN_LIFT_ID, lift.getLiftId());
    }

    public void deleteExercise(Exercise exercise) {
        deleteRowById(LIFT_TABLE_NAME, LIFT_COLUMN_EXERCISE_ID, exercise.getExerciseId());
        deleteRowById(EXERCISE_TABLE_NAME, EXERCISE_COLUMN_EXERCISE_ID, exercise.getExerciseId());
    }

    /**
     * Insert an exercise into the database. Note that the description is always set later, and
     * never on insert, so this is not a required parameter.
     * @param exercise  Exercise to add.
     * @return          The exercise ID of the newly inserted exercise.
     */
    public long insertExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(EXERCISE_COLUMN_NAME, exercise.getName());
        insert_values.put(EXERCISE_COLUMN_DESCRIPTION, exercise.getDescription());
        long exercise_id = db.insert(EXERCISE_TABLE_NAME, null, insert_values);
        db.close();
        return exercise_id;
    }

    /**
     * Insert a lift into the database.
     * @param lift  The lift to insert.
     * @return      The lift ID of the newly inserted lift.
     */
    public long insertLift(Lift lift) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(LIFT_COLUMN_COMMENT, lift.getComment());
        insert_values.put(LIFT_COLUMN_EXERCISE_ID, lift.getExercise().getExerciseId());
        insert_values.put(LIFT_COLUMN_REPS, lift.getReps());
        insert_values.put(LIFT_COLUMN_START_DATE, lift.getStartDate().getTime());
        insert_values.put(LIFT_COLUMN_WEIGHT, lift.getWeight());
        insert_values.put(LIFT_COLUMN_WORKOUT_ID, lift.getWorkoutId());
        long lift_id = db.insert(LIFT_TABLE_NAME, null, insert_values);
        db.close();
        return lift_id;
    }

    /**
     * Insert a workout into the database.
     * @param workout   The workout to insert.
     * @return          The workout ID of the newly inserted workout.
     */
    public long insertWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(WORKOUT_COLUMN_START_DATE, workout.getStartDate().getTime());
        insert_values.put(WORKOUT_COLUMN_DESCRIPTION, workout.getDescription());
        long workout_id = db.insert(WORKOUT_TABLE_NAME, null, insert_values);
        db.close();
        return workout_id;
    }

    /**
     * Selects an array of lift IDs associated with the workout.
     * @param workout_id    Workout ID to get lifts of.
     * @return              An array of lift IDs.
     */
    public ArrayList<Long> selectLiftsByWorkoutId(long workout_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {LIFT_COLUMN_LIFT_ID};

        String WHERE = LIFT_COLUMN_WORKOUT_ID + " = ?";
        String[] where_args = {Long.toString(workout_id)};

        String sort_order = LIFT_COLUMN_START_DATE + " DESC";
        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                sort_order);
        ArrayList<Long> lift_ids = new ArrayList<>();
        while(select_cursor.moveToNext()) {
            long lift_id = select_cursor.getLong(
                    select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_LIFT_ID));
            lift_ids.add(lift_id);
        }
        select_cursor.close();
        db.close();
        return lift_ids;
    }

    /**
     * Selects the number of exercises in the SQLite database. This is used to avoid the case where
     * no exercises are present but UI elements try to access the first one and other various problems
     * of that nature.
     * @return  The count of exercises.
     */
    public int selectExerciseCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String select_all_exercises_query = "SELECT * FROM " + EXERCISE_TABLE_NAME;
        Cursor select_cursor = db.rawQuery(select_all_exercises_query, null);
        int exercise_count = select_cursor.getCount();
        select_cursor.close();
        db.close();
        return exercise_count;
    }

    /**
     * Selects an exercise from the database given its ID.
     * @param exerciseId   The ID of the exercise.
     * @return              The exercise object.
     */
    public Exercise selectExerciseFromExerciseId(long exerciseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                EXERCISE_COLUMN_NAME,
                EXERCISE_COLUMN_DESCRIPTION,
                EXERCISE_COLUMN_LAST_WORKOUT_ID
        };

        String WHERE = EXERCISE_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exerciseId) };

        Cursor select_cursor = db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db)
        {
            return null;
        }

        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long lastWorkoutId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();

        return new Exercise.Builder()
                .name(name)
                .description(description)
                .exerciseId(exerciseId)
                .lastWorkoutId(lastWorkoutId)
                .toCreate(false)
                .liftDbHelper(this)
                .build();
    }

    public Exercise selectExerciseFromName(String exercise_name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                EXERCISE_COLUMN_EXERCISE_ID,
                EXERCISE_COLUMN_NAME,
                EXERCISE_COLUMN_DESCRIPTION,
                EXERCISE_COLUMN_LAST_WORKOUT_ID
        };

        String WHERE = EXERCISE_COLUMN_NAME + " LIKE ?";
        String[] where_args = { exercise_name.toLowerCase() };

        Cursor select_cursor = db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean name_in_db = select_cursor.moveToFirst();
        if (!name_in_db) {
            return null;
        }

        long exerciseId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID));
        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long lastWorkoutId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return new Exercise.Builder()
                .name(name)
                .exerciseId(exerciseId)
                .description(description)
                .lastWorkoutId(lastWorkoutId)
                .toCreate(false)
                .liftDbHelper(this)
                .build();

    }

    /**
     * Selects all exercises that match the filter. These are returned as a cursor.
     * @param filter    String on which to filter the exercises.
     * @return          A cursor of all the matching exercises.
     */
    public Cursor selectExercisesCursor(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { EXERCISE_COLUMN_EXERCISE_ID + " AS _id", EXERCISE_COLUMN_NAME, EXERCISE_COLUMN_DESCRIPTION };
        String WHERE = EXERCISE_COLUMN_NAME + " LIKE ?";
        String[] where_args = { "%" + filter + "%" };

        return db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                EXERCISE_COLUMN_NAME);
    }

    /**
     * Selects all exercises, returned as a list.
     */
    public ArrayList<Exercise> selectExerciseList(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS =  { EXERCISE_COLUMN_EXERCISE_ID, EXERCISE_COLUMN_NAME, EXERCISE_COLUMN_DESCRIPTION, EXERCISE_COLUMN_LAST_WORKOUT_ID };
        String WHERE = EXERCISE_COLUMN_NAME + " LIKE ?";
        String[] where_args = { "%" + filter + "%" };
        Cursor select_cursor = db.query(
            EXERCISE_TABLE_NAME,
            RETURN_COLUMNS,
            WHERE,
            where_args,
            null,
            null,
            EXERCISE_COLUMN_NAME);
        ArrayList<Exercise> exerciseList = new ArrayList<>();

        while(select_cursor.moveToNext())
        {
            Exercise currentExercise = new Exercise.Builder()
                    .name(select_cursor.getString(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME)))
                    .exerciseId(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID)))
                    .toCreate(false)
                    .description(select_cursor.getString(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION)))
                    .lastWorkoutId(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID)))
                    .liftDbHelper(this)
                    .build();
            exerciseList.add(currentExercise);
        }
        select_cursor.close();
        db.close();
        return exerciseList;
    }

    /**
     * Selects the most recent workout.
     * @return  The most recent workout.
     */
    Workout selectLastWorkout() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { WORKOUT_COLUMN_WORKOUT_ID };
        String SORT_ORDER = WORKOUT_COLUMN_START_DATE + " DESC";
        String LIMIT_1 = "1";

        Cursor select_cursor = db.query(
                WORKOUT_TABLE_NAME,
                RETURN_COLUMNS,
                null,
                null,
                null,
                null,
                SORT_ORDER,
                LIMIT_1);
        boolean workout_in_db = select_cursor.moveToFirst();
        if (!workout_in_db) {
            return null;
        }

        long workout_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return selectWorkoutFromWorkoutId(workout_id);
    }

    /**
     * Select a lift object from its ID.
     * @param lift_id   The ID of the lift.
     * @return          The lift object.
     */
    public Lift selectLiftFromLiftId(long lift_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                LIFT_COLUMN_EXERCISE_ID,
                LIFT_COLUMN_REPS,
                LIFT_COLUMN_WEIGHT,
                LIFT_COLUMN_WORKOUT_ID,
                LIFT_COLUMN_COMMENT,
                LIFT_COLUMN_START_DATE
        };

        String WHERE = LIFT_COLUMN_LIFT_ID + " = ?";
        String[] where_args = { Long.toString(lift_id) };

        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return null;
        }

        Exercise exercise = selectExerciseFromExerciseId(select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_EXERCISE_ID)));
        int reps = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_REPS));
        int weight = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_WEIGHT));
        long workoutId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_WORKOUT_ID));
        String comment = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_COMMENT));
        long startDateAsLong = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        Date startDate = new Date(startDateAsLong);
        select_cursor.close();
        db.close();

        return new Lift.Builder()
                .liftId(lift_id)
                .toCreate(false)
                .liftDbHelper(this)
                .exercise(exercise)
                .reps(reps)
                .weight(weight)
                .startDate(startDate)
                .workoutId(workoutId)
                .comment(comment)
                .build();
    }

    /**
     * Select a list of lift objects based on the exercise ID.
     * @param params    Parameters used to select the lifts in the history of the exercise.
     * @return          An ArrayList of the lifts.
     */
    public ArrayList<Lift> selectExerciseHistoryLifts(SelectExerciseHistoryParams params) {
        SQLiteDatabase db = this.getReadableDatabase();
        String MAX_EFFORT_COLUMN_NAME = "MaxEffort";
        String MAX_EFFORT_COLUMN = "(" + LIFT_COLUMN_WEIGHT + " / (1.0278 - (0.0278 * " + LIFT_COLUMN_REPS + "))) AS " + MAX_EFFORT_COLUMN_NAME;
        String[] RETURN_COLUMNS = { LIFT_COLUMN_LIFT_ID, LIFT_COLUMN_REPS, LIFT_COLUMN_START_DATE, LIFT_COLUMN_WEIGHT, LIFT_COLUMN_WORKOUT_ID, LIFT_COLUMN_COMMENT, MAX_EFFORT_COLUMN };
        String WHERE = LIFT_COLUMN_EXERCISE_ID + " LIKE ? AND " + LIFT_COLUMN_START_DATE + " BETWEEN ? AND ?";
        String[] where_args = { Long.toString(params.getExercise().getExerciseId()), Long.toString(params.getFromDate().getTime()), Long.toString(params.getToDate().getTime()) };
        String sort_order = "";
        switch (params.getOrder())
        {
            case DATE_DESC:
                sort_order = LIFT_COLUMN_START_DATE + " DESC";
                break;
            case DATE_ASC:
                sort_order = LIFT_COLUMN_START_DATE + " ASC";
                break;
            case WEIGHT_DESC:
                sort_order = LIFT_COLUMN_WEIGHT + " DESC";
                break;
            case WEIGHT_ASC:
                sort_order = LIFT_COLUMN_WEIGHT + " ASC";
                break;
            case MAX_DESC:
                sort_order = MAX_EFFORT_COLUMN_NAME + " DESC";
                break;
            case MAX_ASC:
                sort_order = MAX_EFFORT_COLUMN_NAME + " ASC";
                break;
        }

        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                sort_order);
        ArrayList<Lift> exercise_history_lifts = new ArrayList<>();

        while(select_cursor.moveToNext())
        {
            Lift newLift = new Lift.Builder()
                    .liftId(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_LIFT_ID)))
                    .toCreate(false)
                    .liftDbHelper(this)
                    .exercise(params.getExercise())
                    .weight(select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_WEIGHT)))
                    .reps(select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_REPS)))
                    .comment(select_cursor.getString(select_cursor.getColumnIndex(LIFT_COLUMN_COMMENT)))
                    .startDate(new Date(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_START_DATE))))
                    .workoutId(select_cursor.getLong(select_cursor.getColumnIndex(WORKOUT_COLUMN_WORKOUT_ID)))
                    .build();

            exercise_history_lifts.add(newLift);
        }
        select_cursor.close();
        db.close();
        return exercise_history_lifts;
    }

    public long selectExerciseIdFromLiftId(long lift_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {LIFT_COLUMN_EXERCISE_ID};

        String WHERE = LIFT_COLUMN_LIFT_ID + " = ?";
        String[] where_args = { Long.toString(lift_id) };

        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean lift_in_db = select_cursor.moveToFirst();
        if (!lift_in_db) {
            return -1;
        }

        long exercise_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_EXERCISE_ID));
        select_cursor.close();
        db.close();
        return exercise_id;
    }

    /**
     * Select a list of lift objects based on the workout ID.
     * @param workout  The workout of which to get all the lifts.
     * @return          An ArrayList of the lifts.
     */
    public ArrayList<Lift> selectWorkoutHistoryLifts(Workout workout) {
        SQLiteDatabase db = this.getReadableDatabase();
        String JOIN_LIFT_AND_EXERCISE_TABLE_QUERY =
                "SELECT * FROM " + LIFT_TABLE_NAME + " " +
                "LEFT OUTER JOIN " + EXERCISE_TABLE_NAME + " USING (" + EXERCISE_COLUMN_EXERCISE_ID +") " +
                "WHERE " + LIFT_COLUMN_WORKOUT_ID + " = ? " +
                "ORDER BY " + LIFT_COLUMN_START_DATE + " DESC;";

        String[] where_args = {Long.toString(workout.getWorkoutId())};
        Cursor select_cursor = db.rawQuery(JOIN_LIFT_AND_EXERCISE_TABLE_QUERY, where_args);
        ArrayList<Lift> workout_history_lifts = new ArrayList<>();

        while(select_cursor.moveToNext())
        {
            Exercise currentExercise = new Exercise.Builder()
                    .name(select_cursor.getString(select_cursor.getColumnIndex(EXERCISE_COLUMN_NAME)))
                    .exerciseId(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_EXERCISE_ID)))
                    .toCreate(false)
                    .liftDbHelper(this)
                    .description(select_cursor.getString(select_cursor.getColumnIndex(EXERCISE_COLUMN_DESCRIPTION)))
                    .lastWorkoutId(select_cursor.getLong(select_cursor.getColumnIndex(EXERCISE_COLUMN_LAST_WORKOUT_ID)))
                    .build();

            Lift newLift = new Lift.Builder()
                    .liftId(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_LIFT_ID)))
                    .toCreate(false)
                    .liftDbHelper(this)
                    .exercise(currentExercise)
                    .reps(select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_REPS)))
                    .weight(select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_WEIGHT)))
                    .comment(select_cursor.getString(select_cursor.getColumnIndex(LIFT_COLUMN_COMMENT)))
                    .startDate(new Date(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_START_DATE))))
                    .workoutId(select_cursor.getLong(select_cursor.getColumnIndex(WORKOUT_COLUMN_WORKOUT_ID)))
                    .build();

            workout_history_lifts.add(newLift);
        }

        select_cursor.close();
        db.close();
        return workout_history_lifts;
    }

    /**
     * Selects all workouts, returned as an array list.
     * @return          An ArrayList of workouts.
     */
    public ArrayList<Workout> selectWorkoutList(LoadWorkoutHistoryListParams params)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String LIFT_COUNT_COL_NAME = "LiftCount";
        String LENGTH_COL_NAME = "WorkoutLength";
        String order_by = "";
        switch (params.getOrder())
        {
            case DATE_ASC:
                order_by = " ORDER BY " + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_START_DATE + " ASC";
                break;
            default:
                order_by = " ORDER BY " + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_START_DATE + " DESC";
        }
        String SELECT_WORKOUT_QUERY =
                "SELECT " + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_WORKOUT_ID + ", "
                        + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_DESCRIPTION + ", "
                        + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_START_DATE +
                " FROM " + WORKOUT_TABLE_NAME +
                " WHERE " + WORKOUT_TABLE_NAME + "." + WORKOUT_COLUMN_START_DATE + " BETWEEN ? AND ?" +
                order_by +
                " LIMIT " + params.getWorkoutCount();

        String[] where_args = {Long.toString(params.getFromDate().getTime()), Long.toString(params.getToDate().getTime())};
        try
        {
            final Cursor select_cursor = db.rawQuery(SELECT_WORKOUT_QUERY, where_args);
            ArrayList<Workout> workout_list = new ArrayList<>();

            while(select_cursor.moveToNext())
            {
                Workout newWorkout = new Workout.Builder()
                        .workoutId(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID)))
                        .toCreate(false)
                        .liftDbHelper(this)
                        .description(select_cursor.getString(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_DESCRIPTION)))
                        .startDate(new Date(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE))))
                        .enableSimilarExercisesAlgorithm(false)
                        .build();

                workout_list.add(newWorkout);
            }

            select_cursor.close();
            db.close();
            return workout_list;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Select a workout given its ID.
     * @param workoutId    The workout ID to select.
     * @return              The workout.
     */
    private Workout selectWorkoutFromWorkoutId(long workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
            WORKOUT_COLUMN_DESCRIPTION,
            WORKOUT_COLUMN_START_DATE
        };

        String WHERE = WORKOUT_COLUMN_WORKOUT_ID + " = ?";
        String[] where_args = { Long.toString(workoutId) };

        Cursor select_cursor = db.query(
                WORKOUT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return null;
        }

        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_DESCRIPTION));
        long startDateAsLong = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        select_cursor.close();

        Date startDate = new Date(startDateAsLong);
        db.close();
        return new Workout.Builder()
                .workoutId(workoutId)
                .toCreate(false)
                .description(description)
                .liftDbHelper(this)
                .liftIds(selectLiftsByWorkoutId(workoutId))
                .startDate(startDate)
                .enableSimilarExercisesAlgorithm(true)
                .build();
    }

    /**
     * Update a long field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_long            Value to update.
     */
    private void updateFieldLongFromId(
            String table_name,
            String id_column_name,
            long id,
            String field_column_name,
            long field_long) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues update_values = new ContentValues();
        update_values.put(field_column_name, field_long);

        String WHERE =  id_column_name + " = ?";
        String[] where_args = { Long.toString(id)};
        db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
    }


    /**
     * Update an int field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_int             Value to update.
     */
    private void updateFieldIntFromId(
            String table_name,
            String id_column_name,
            long id,
            String field_column_name,
            int field_int) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues update_values = new ContentValues();
        update_values.put(field_column_name, field_int);

        String WHERE =  id_column_name + " = ?";
        String[] where_args = { Long.toString(id)};
        db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
    }

    /**
     * Update a string field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_string          Value to update.
     */
    private void updateFieldStringFromId(
            String table_name,
            String id_column_name,
            long id,
            String field_column_name,
            String field_string) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues update_values = new ContentValues();
        update_values.put(field_column_name, field_string);

        String WHERE = id_column_name + " = ?";
        String[] where_args = { Long.toString(id)};
        db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
    }

    /**
     * Update the description of an exercise.
     * @param exercise      Exercise to update.
     */
    public void updateDescriptionOfExercise(Exercise exercise)
    {
        updateFieldStringFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_DESCRIPTION,
                exercise.getDescription());
    }

    /**
     * Update the most recent workout ID that an exercise was performed.
     * @param exercise      Exercise to update.
     */
    public void updateLastWorkoutIdOfExercise(Exercise exercise)
    {
        updateFieldLongFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_LAST_WORKOUT_ID,
                exercise.getLastWorkoutId());
    }

    public void updateMaxLiftIdOfExerciseToNull(Exercise exercise)
    {
        this.deleteRowById(MAX_WEIGHT_TABLE_NAME, MAX_WEIGHT_COLUMN_EXERCISE_ID, exercise.getExerciseId());
    }

    /**
     * Update the name of the exercise.
     * @param exercise  Exercise to update.
     */
    public void updateNameOfExercise(Exercise exercise)
    {
        updateFieldStringFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_NAME,
                exercise.getName());
    }

    public void updateLift(Lift lift)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues update_values = new ContentValues();
        update_values.put(LIFT_COLUMN_WEIGHT, lift.getWeight());
        update_values.put(LIFT_COLUMN_REPS, lift.getReps());
        update_values.put(LIFT_COLUMN_COMMENT, lift.getComment());

        String WHERE = LIFT_COLUMN_LIFT_ID + " = ?";
        String[] where_args = { Long.toString(lift.getLiftId())};
        db.update(
                LIFT_TABLE_NAME,
                update_values,
                WHERE,
                where_args);
        db.close();

        this.updateMaxWeightTableWithLift(lift);
    }

    /**
     * Update the description of a workout. The workout object should already have been updated internally.
     * @param workout   Workout to update.
     */
    public void updateDescriptionOfWorkout(Workout workout)
    {
        updateFieldStringFromId(
                WORKOUT_TABLE_NAME,
                WORKOUT_COLUMN_WORKOUT_ID,
                workout.getWorkoutId(),
                WORKOUT_COLUMN_DESCRIPTION,
                workout.getDescription());
    }

    public void updateSelectedExercise(long exercise_id)
    {
        removeSelectedExercise();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(SELECTED_EXERCISE_COLUMN_EXERCISE_ID, exercise_id);
        insert_values.put(SELECTED_EXERCISE_COLUMN_DATE, new Date().getTime());
        db.insert(SELECTED_EXERCISE_TABLE_NAME, null, insert_values);
        db.close();
    }

    public void removeSelectedExercise()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] no_where_args = {};
        db.delete(SELECTED_EXERCISE_TABLE_NAME, "",no_where_args);
        db.close();
    }

    public long getSelectedExercise()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS =
                { SELECTED_EXERCISE_COLUMN_EXERCISE_ID };
        String sort_order = SELECTED_EXERCISE_COLUMN_DATE + " DESC";
        Cursor select_cursor = db.query(SELECTED_EXERCISE_TABLE_NAME, RETURN_COLUMNS, null, null, null, null, sort_order);

        boolean any_selected_exercise_exists = select_cursor.moveToFirst();
        if (!any_selected_exercise_exists)
        {
            return -1;
        }

        long selected_exercise_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(SELECTED_EXERCISE_COLUMN_EXERCISE_ID));
        select_cursor.close();
        db.close();
        return selected_exercise_id;
    }

    public Date selectDateFromWorkoutId(long workout_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                WORKOUT_COLUMN_START_DATE
        };

        String WHERE = WORKOUT_COLUMN_WORKOUT_ID + " = ?";
        String[] where_args = { Long.toString(workout_id) };

        Cursor select_cursor = db.query(
                WORKOUT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return null;
        }

        long start_date_as_long = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        Date start_date = new Date(start_date_as_long);
        select_cursor.close();
        db.close();
        return start_date;
    }

    public boolean exerciseNameExists(String exercise_name)
    {
        return null != this.selectExerciseFromName(exercise_name);
    }

    public Exercise selectMostRecentExercise()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                EXERCISE_COLUMN_EXERCISE_ID,
                EXERCISE_COLUMN_NAME,
                EXERCISE_COLUMN_DESCRIPTION,
                EXERCISE_COLUMN_LAST_WORKOUT_ID
        };

        String sort_order = EXERCISE_COLUMN_EXERCISE_ID + " DESC";

        Cursor select_cursor = db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                null,
                null,
                null,
                null,
                sort_order);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return null;
        }

        long exerciseId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID));
        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long lastWorkoutId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return new Exercise.Builder()
                .name(name)
                .exerciseId(exerciseId)
                .toCreate(false)
                .description(description)
                .lastWorkoutId(lastWorkoutId)
                .liftDbHelper(this)
                .build();
    }

    public Map<Long, Integer> getSimilarExercises(Long exercise_id)
    {
        Map similar_exercise_ids_to_set_counts = new TreeMap<Long, Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        String COUNT_COLUMN_NAME = "Ct";
        String JOIN_PREVIOUS_LIFTS_BASED_ON_WORKOUT_HISTORY_QUERY =
                "SELECT " + LIFT_COLUMN_EXERCISE_ID + ", COUNT(*) AS " + COUNT_COLUMN_NAME + " " +
                        "FROM " + LIFT_TABLE_NAME + " " +
                        "WHERE " + LIFT_COLUMN_WORKOUT_ID + " IN ( " +
                        "SELECT DISTINCT( " + LIFT_COLUMN_WORKOUT_ID + ") " +
                        "FROM " + LIFT_TABLE_NAME + " " +
                        "WHERE " + LIFT_COLUMN_EXERCISE_ID + " = ?) " +
                        "AND " + LIFT_COLUMN_EXERCISE_ID + " != ? " +
                        "GROUP BY " + LIFT_COLUMN_EXERCISE_ID + " " +
                        "ORDER BY " + COUNT_COLUMN_NAME + " DESC;";

        String[] where_args = {Long.toString(exercise_id), Long.toString(exercise_id)};
        Cursor select_cursor = db.rawQuery(JOIN_PREVIOUS_LIFTS_BASED_ON_WORKOUT_HISTORY_QUERY, where_args);

        while(select_cursor.moveToNext()) {
            similar_exercise_ids_to_set_counts.put(
                    select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_EXERCISE_ID)),
                    select_cursor.getInt(select_cursor.getColumnIndex(COUNT_COLUMN_NAME)));
        }

        select_cursor.close();
        db.close();

        return similar_exercise_ids_to_set_counts;
    }

    public long getWorkoutDurationInMs(Workout workout)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String DURATION_COLUMN_NAME = "dur";
        String SELECT_WORKOUT_DURATION_QUERY =
                "SELECT MAX(" + LIFT_COLUMN_START_DATE + ") - MIN(" + LIFT_COLUMN_START_DATE + ") AS " + DURATION_COLUMN_NAME +
                        " FROM " + LIFT_TABLE_NAME +
                        " WHERE " + LIFT_COLUMN_WORKOUT_ID + " = ?;";

        String[] where_args = {Long.toString(workout.getWorkoutId())};
        Cursor select_cursor = db.rawQuery(SELECT_WORKOUT_DURATION_QUERY, where_args);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return 0;
        }

        long workout_duration_as_long = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(DURATION_COLUMN_NAME));
        return workout_duration_as_long;
    }

    public DataPoint[] getDataPointsFromExercise(Long exercise_id, Date from_date, Date to_date)
    {
        ArrayList<DataPoint> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String max_effort_col = "MaxEffort";
        String lift_date_col = "LiftDate";
        String from_date_string = Long.toString(from_date.getTime());
        String to_date_string = Long.toString(to_date.getTime());

        String query = "SELECT MAX(" + max_effort_col + ") AS " + max_effort_col + ", " + lift_date_col + ", " + LIFT_COLUMN_WEIGHT + ", " + LIFT_COLUMN_REPS + " FROM " +
                "(SELECT (" + LIFT_COLUMN_WEIGHT + " / (1.0278 - (0.0278 * " + LIFT_COLUMN_REPS + "))) AS " + max_effort_col + ", " +
                "(" + LIFT_COLUMN_START_DATE + " - (" + LIFT_COLUMN_START_DATE + " % (1000 * 60 * 60 * 24))) AS " + lift_date_col + ", " + LIFT_COLUMN_WEIGHT + ", " + LIFT_COLUMN_REPS +
                " FROM " + LIFT_TABLE_NAME + " WHERE " + LIFT_COLUMN_EXERCISE_ID + " = " + Long.toString(exercise_id) + ") " +
                "WHERE " + lift_date_col + " BETWEEN " + from_date_string + " AND " + to_date_string +
                " GROUP BY  " + lift_date_col + " ORDER BY " + lift_date_col;

        Cursor select_cursor = db.rawQuery(query, null);
        while(select_cursor.moveToNext())
        {
            long lift_date_as_long = select_cursor.getLong(
                    select_cursor.getColumnIndexOrThrow("LiftDate"));
            Date lift_date = new Date(lift_date_as_long);
            int max_effort = select_cursor.getInt(select_cursor.getColumnIndexOrThrow("MaxEffort"));

            DataPoint dp = new DataPoint(lift_date, max_effort);
            ret.add(dp);
        }
        select_cursor.close();
        db.close();
        DataPoint[] retArray = new DataPoint[ret.size()];
        retArray = ret.toArray(retArray);
        return retArray;
    }

    private Lift calculateHeaviestLiftByExercise(long exercise_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS =
        {
                LIFT_COLUMN_LIFT_ID,
                LIFT_COLUMN_EXERCISE_ID,
                LIFT_COLUMN_REPS,
                LIFT_COLUMN_WEIGHT,
                LIFT_COLUMN_WORKOUT_ID,
                LIFT_COLUMN_COMMENT,
                LIFT_COLUMN_START_DATE
        };

        String WHERE = LIFT_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise_id) };

        String sort_order = LIFT_COLUMN_WEIGHT + " DESC, " + LIFT_COLUMN_REPS + " DESC";

        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                sort_order);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return null;
        }

        long liftId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_LIFT_ID));
        Exercise exercise = selectExerciseFromExerciseId(exercise_id);
        int reps = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_REPS));
        int weight = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_WEIGHT));
        long workoutId = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_WORKOUT_ID));
        String comment = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_COMMENT));
        long startDateAsLong = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        Date startDate = new Date(startDateAsLong);
        select_cursor.close();
        db.close();
        return new Lift.Builder()
                .liftId(liftId)
                .toCreate(false)
                .liftDbHelper(this)
                .exercise(exercise)
                .reps(reps)
                .weight(weight)
                .comment(comment)
                .startDate(startDate)
                .workoutId(workoutId)
                .build();
    }

    private Lift calculateMaxEffortLiftByExercise(long exercise_id)
    {
        SelectExerciseHistoryParams params = new SelectExerciseHistoryParams(this.selectExerciseFromExerciseId(exercise_id), SelectExerciseHistoryParams.ExerciseListOrder.MAX_DESC);
        ArrayList<Lift> lifts = this.selectExerciseHistoryLifts(params);
        if (lifts.isEmpty())
        {
            return null;
        }

        return lifts.get(0);
    }

    public int selectCountOfLiftsByExercise(long exercise_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String COUNT_COLUMN_NAME = "ct";
        String SELECT_LIFT_COUNT_QUERY =
                "SELECT COUNT(*) AS " + COUNT_COLUMN_NAME +
                        " FROM " + LIFT_TABLE_NAME +
                        " WHERE " + LIFT_COLUMN_EXERCISE_ID + " = ?;";

        String[] where_args = {Long.toString(exercise_id)};
        Cursor select_cursor = db.rawQuery(SELECT_LIFT_COUNT_QUERY, where_args);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return 0;
        }

        int lift_count = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(COUNT_COLUMN_NAME));
        return lift_count;
    }

    public int selectCountOfWorkoutsByExercise(long exercise_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String COUNT_COLUMN_NAME = "ct";
        String SELECT_LIFT_COUNT_QUERY =
                "SELECT COUNT(*) AS " + COUNT_COLUMN_NAME + " FROM " +
                        "(SELECT COUNT(*) FROM " + LIFT_TABLE_NAME +
                        " WHERE " + LIFT_COLUMN_EXERCISE_ID + " = ?" +
                        " GROUP BY " + LIFT_COLUMN_WORKOUT_ID + ") AS Sub";

        String[] where_args = {Long.toString(exercise_id)};
        Cursor select_cursor = db.rawQuery(SELECT_LIFT_COUNT_QUERY, where_args);

        boolean id_in_db = select_cursor.moveToFirst();
        if (!id_in_db) {
            return 0;
        }

        int workout_count = select_cursor.getInt(
                select_cursor.getColumnIndexOrThrow(COUNT_COLUMN_NAME));
        return workout_count;
    }
}

