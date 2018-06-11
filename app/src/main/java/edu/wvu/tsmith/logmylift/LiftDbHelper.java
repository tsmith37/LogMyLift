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
import edu.wvu.tsmith.logmylift.workout.Workout;

/**
 * Created by Tommy Smith on 3/19/2017.
 * The LiftDbHelper class provides a public interface to the SQLite database used by the application.
 * This is helpful because all database operations should be central to this class.
 * @author Tommy Smith
 */
public class LiftDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LogMyLiftDb.db";
    private static final int DATABASE_VERSION = 2;

    public static final String EXERCISE_TABLE_NAME = "Exercise";
    public static final String EXERCISE_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String EXERCISE_COLUMN_DESCRIPTION = "Description";
    public static final String EXERCISE_COLUMN_LAST_WORKOUT_ID = "LastWorkoutId";
    public static final String EXERCISE_COLUMN_MAX_LIFT_ID = "MaxLiftId";
    public static final String EXERCISE_COLUMN_NAME = "Name";

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

    private static final String CREATE_TABLE_WORKOUT =
            "CREATE TABLE Workout (" +
                    "WorkoutId INTEGER PRIMARY KEY," +
                    "Description TEXT," +
                    "StartDate INTEGER);";

    private static final String CREATE_TABLE_SELECTED_EXERCISE =
            "CREATE TABLE SelectedExercise (" +
                    "ExerciseId INTEGER," +
                    "Date INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId));";

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
        if (oldVersion == 1 && newVersion >= 2)
        {
            db.execSQL(CREATE_TABLE_SELECTED_EXERCISE);
        }
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
    private ArrayList<Long> selectLiftsByWorkoutId(long workout_id) {
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
     * @param exercise_id   The ID of the exercise.
     * @return              The exercise object.
     */
    public Exercise selectExerciseFromExerciseId(long exercise_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                EXERCISE_COLUMN_NAME,
                EXERCISE_COLUMN_DESCRIPTION,
                EXERCISE_COLUMN_MAX_LIFT_ID,
                EXERCISE_COLUMN_LAST_WORKOUT_ID
        };

        String WHERE = EXERCISE_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise_id) };

        Cursor select_cursor = db.query(
                EXERCISE_TABLE_NAME,
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

        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long max_lift_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_MAX_LIFT_ID));
        long last_workout_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return new Exercise(exercise_id, name, description, max_lift_id, last_workout_id);
    }

    public Exercise selectExerciseFromName(String exercise_name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
                EXERCISE_COLUMN_EXERCISE_ID,
                EXERCISE_COLUMN_NAME,
                EXERCISE_COLUMN_DESCRIPTION,
                EXERCISE_COLUMN_MAX_LIFT_ID,
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

        long exercise_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID));
        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long max_lift_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_MAX_LIFT_ID));
        long last_workout_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return new Exercise(exercise_id, name, description, max_lift_id, last_workout_id);
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
        String[] RETURN_COLUMNS =  { EXERCISE_COLUMN_EXERCISE_ID, EXERCISE_COLUMN_NAME, EXERCISE_COLUMN_DESCRIPTION, EXERCISE_COLUMN_LAST_WORKOUT_ID, EXERCISE_COLUMN_MAX_LIFT_ID };
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
        ArrayList<Exercise> exercise_list = new ArrayList<>();

        while(select_cursor.moveToNext()) {
            exercise_list.add(new Exercise(
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID)),
                    select_cursor.getString(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME)),
                    select_cursor.getString(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION)),
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_MAX_LIFT_ID)),
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID))));
        }
        select_cursor.close();
        db.close();
        return exercise_list;
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
        long workout_id= select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_WORKOUT_ID));
        String comment = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_COMMENT));
        long start_date_as_long = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        Date start_date = new Date(start_date_as_long);
        select_cursor.close();
        db.close();
        return new Lift(lift_id, exercise, reps, start_date, weight, workout_id, comment);
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
        String WHERE = LIFT_COLUMN_EXERCISE_ID + " LIKE ?";
        String[] where_args = { Long.toString(params.getExercise().getExerciseId()) };
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

        while(select_cursor.moveToNext()) {
            exercise_history_lifts.add(new Lift(
                    select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_LIFT_ID)),
                    params.getExercise(),
                    select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_REPS)),
                    new Date(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_START_DATE))),
                    select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_WEIGHT)),
                    select_cursor.getLong(select_cursor.getColumnIndex(WORKOUT_COLUMN_WORKOUT_ID)),
                    select_cursor.getString(select_cursor.getColumnIndex(LIFT_COLUMN_COMMENT))));
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

        while(select_cursor.moveToNext()) {
            workout_history_lifts.add(new Lift(
                select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_LIFT_ID)),
                new Exercise(
                        select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_EXERCISE_ID)),
                        select_cursor.getString(select_cursor.getColumnIndex(EXERCISE_COLUMN_NAME)),
                        select_cursor.getString(select_cursor.getColumnIndex(EXERCISE_COLUMN_DESCRIPTION)),
                        select_cursor.getLong(select_cursor.getColumnIndex(EXERCISE_COLUMN_MAX_LIFT_ID)),
                        select_cursor.getLong(select_cursor.getColumnIndex(EXERCISE_COLUMN_LAST_WORKOUT_ID))),
                select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_REPS)),
                new Date(select_cursor.getLong(select_cursor.getColumnIndex(LIFT_COLUMN_START_DATE))),
                select_cursor.getInt(select_cursor.getColumnIndex(LIFT_COLUMN_WEIGHT)),
                select_cursor.getLong(select_cursor.getColumnIndex(WORKOUT_COLUMN_WORKOUT_ID)),
                select_cursor.getString(select_cursor.getColumnIndex(LIFT_COLUMN_COMMENT))));
        }

        select_cursor.close();
        db.close();
        return workout_history_lifts;
    }

    /**
     * Selects all workouts, returned as an array list.
     * @param filter    Filter on the workout name.
     * @return          An ArrayList of workouts.
     */
    public ArrayList<Workout> selectWorkoutList(String filter, Date from_date, Date to_date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String SELECT_WORKOUT_QUERY = "SELECT " + WORKOUT_COLUMN_WORKOUT_ID + ", " + WORKOUT_COLUMN_DESCRIPTION + ", " + WORKOUT_COLUMN_START_DATE +
                " FROM " + WORKOUT_TABLE_NAME +
                " WHERE " + WORKOUT_COLUMN_DESCRIPTION + " LIKE ?" +
                " AND " + WORKOUT_COLUMN_START_DATE + " BETWEEN ? AND ?" +
                " ORDER BY " + WORKOUT_COLUMN_START_DATE + " DESC";

        String[] where_args = {"%" + filter + "%", Long.toString(from_date.getTime()), Long.toString(to_date.getTime())};
        try {
            final Cursor select_cursor = db.rawQuery(SELECT_WORKOUT_QUERY, where_args);
        ArrayList<Workout> workout_list = new ArrayList<>();

        while(select_cursor.moveToNext()) {
            workout_list.add(new Workout(
                    this,
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID)),
                    select_cursor.getString(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_DESCRIPTION)),
                    selectLiftsByWorkoutId(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID))),
                    new Date(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE))),
                    false));
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
     * @param workout_id    The workout ID to select.
     * @return              The workout.
     */
    private Workout selectWorkoutFromWorkoutId(long workout_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
            WORKOUT_COLUMN_DESCRIPTION,
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

        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_DESCRIPTION));
        long start_date_as_long = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE));
        select_cursor.close();

        Date start_date = new Date(start_date_as_long);
        db.close();
        return new Workout(this, workout_id, description, selectLiftsByWorkoutId(workout_id),start_date,true);
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

    /**
     * Update the ID of the maximum effort lift of the exercise.
     * @param exercise      Exercise to update.
     */
    public void updateMaxLiftIdOfExercise(Exercise exercise)
    {
        updateFieldLongFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_MAX_LIFT_ID,
                exercise.getMaxLiftId());
    }

    public void updateMaxLiftIdOfExerciseToNull(Exercise exercise)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues update_values = new ContentValues();
        update_values.putNull(EXERCISE_COLUMN_MAX_LIFT_ID);

        String WHERE =  EXERCISE_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = { Long.toString(exercise.getExerciseId())};
        db.update(
                EXERCISE_TABLE_NAME,
                update_values,
                WHERE,
                where_args);
        db.close();
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

        updateMaxLiftIdOfExercise(lift.getExercise());
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
                EXERCISE_COLUMN_MAX_LIFT_ID,
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

        long exercise_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_EXERCISE_ID));
        String name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        String description = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_DESCRIPTION));
        long max_lift_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_MAX_LIFT_ID));
        long last_workout_id = select_cursor.getLong(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_LAST_WORKOUT_ID));
        select_cursor.close();
        db.close();
        return new Exercise(exercise_id, name, description, max_lift_id, last_workout_id);
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
}

