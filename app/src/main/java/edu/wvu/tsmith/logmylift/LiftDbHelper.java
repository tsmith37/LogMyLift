package edu.wvu.tsmith.logmylift;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.id;


/**
 * The LiftDbHelper class provides a public interface to the SQLite database used by the application.
 * This is helpful because all database operations should be central to this class.
 * Created by tmssm on 3/19/2017.
 * @author Tommy Smith
 */
class LiftDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LogMyLiftDb.db";
    private static final int DATABASE_VERSION = 1;

    static final String EXERCISE_TABLE_NAME = "Exercise";
    static final String EXERCISE_COLUMN_EXERCISE_ID = "ExerciseId";
    static final String EXERCISE_COLUMN_DESCRIPTION = "Description";
    static final String EXERCISE_COLUMN_LAST_WORKOUT_ID = "LastWorkoutId";
    static final String EXERCISE_COLUMN_MAX_LIFT_ID = "MaxLiftId";
    static final String EXERCISE_COLUMN_NAME = "Name";

    static final String LIFT_TABLE_NAME = "Lift";
    static final String LIFT_COLUMN_LIFT_ID = "LiftId";
    static final String LIFT_COLUMN_EXERCISE_ID = "ExerciseId";
    static final String LIFT_COLUMN_REPS = "Reps";
    static final String LIFT_COLUMN_START_DATE = "StartDate";
    static final String LIFT_COLUMN_WEIGHT = "Weight";
    static final String LIFT_COLUMN_WORKOUT_ID = "WorkoutId";

    static final String WORKOUT_TABLE_NAME = "Workout";
    static final String WORKOUT_COLUMN_WORKOUT_ID = "WorkoutId";
    static final String WORKOUT_COLUMN_DESCRIPTION = "Description";
    static final String WORKOUT_COLUMN_START_DATE = "StartDate";

    static final String CREATE_TABLE_EXERCISE =
            "CREATE TABLE Exercise (" +
                    "ExerciseId INTEGER PRIMARY KEY," +
                    "Name TEXT," +
                    "Description TEXT," +
                    "MaxLiftId INTEGER," +
                    "LastWorkoutId INTEGER," +
                    "FOREIGN KEY(MaxLiftId) REFERENCES Lift(LiftId)," +
                    "FOREIGN KEY(LastWorkoutId) REFERENCES Workout(WorkoutId));";

    static final String CREATE_TABLE_LIFT =
            "CREATE TABLE Lift (" +
                    "LiftId INTEGER PRIMARY KEY," +
                    "ExerciseId INTEGER," +
                    "Reps INTEGER," +
                    "StartDate INTEGER," +
                    "Weight INTEGER," +
                    "WorkoutId INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId)," +
                    "FOREIGN KEY(WorkoutId) REFERENCES Workout(WorkoutId));";

    static final String CREATE_TABLE_WORKOUT =
            "CREATE TABLE Workout (" +
                    "WorkoutId INTEGER PRIMARY KEY," +
                    "Description TEXT," +
                    "StartDate INTEGER);";

    /**
     * Default constructor for the SQLiteOpenHelper class.
     *
     * @param context Application context.
     */
    LiftDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * On create-as required by the parent class-creates the necessary tables.
     *
     * @param db Database to use.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORKOUT);
        db.execSQL(CREATE_TABLE_LIFT);
        db.execSQL(CREATE_TABLE_EXERCISE);
    }

    /**
     * On upgrade-also required by the parent class-is not used yet.
     *
     * @param db         Database to use.
     * @param oldVersion Old version of the database.
     * @param newVersion New version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Deletes a row in the specified table based on the ID.
     * @param table_name     Table to delete from.
     * @param id_column_name ID column name.
     * @param id             ID whose row to delete.
     */
    void deleteRowById(String table_name, String id_column_name, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String WHERE = id_column_name + " = ?";
        String[] where_args = {Long.toString(id)};
        db.delete(table_name, WHERE, where_args);
    }

    /**
     * Deletes an exercise from the database.
     * @param exercise  Exercise to delete.
     */
    void deleteExercise(Exercise exercise)
    {
        deleteRowById(EXERCISE_TABLE_NAME, EXERCISE_COLUMN_EXERCISE_ID, exercise.getExerciseId());
    }

    /**
     * Deletes a lift from the database.
     * @param lift  Lift to delete.
     */
    void deleteLift(Lift lift)
    {
        deleteRowById(LIFT_TABLE_NAME, LIFT_COLUMN_LIFT_ID, lift.getLiftId());
    }

    /**
     * Deletes a workout from the database.
     * @param workout   Workout to delete.
     */
    void deleteWorkout(Workout workout)
    {
        deleteRowById(WORKOUT_TABLE_NAME, WORKOUT_COLUMN_WORKOUT_ID, workout.getWorkoutId());
    }

    /**
     * Insert an exercise into the database. Note that the description is always set later, and
     * never on insert, so this is not a required parameter.
     * @param exercise  Exercise to add.
     * @return          The exercise ID of the newly inserted exercise.
     */
    long insertExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(EXERCISE_COLUMN_NAME, exercise.getName());
        insert_values.put(EXERCISE_COLUMN_DESCRIPTION, exercise.getDescription());
        return db.insert(EXERCISE_TABLE_NAME, null, insert_values);
    }

    /**
     * Insert a lift into the database.
     * @param lift  The lift to insert.
     * @return      The lift ID of the newly inserted lift.
     */
    long insertLift(Lift lift) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(LIFT_COLUMN_EXERCISE_ID, lift.getExercise().getExerciseId());
        insert_values.put(LIFT_COLUMN_REPS, lift.getReps());
        insert_values.put(LIFT_COLUMN_START_DATE, lift.getStartDate().getTime());
        insert_values.put(LIFT_COLUMN_WEIGHT, lift.getWeight());
        insert_values.put(LIFT_COLUMN_WORKOUT_ID, lift.getWorkoutId());
        return db.insert(LIFT_TABLE_NAME, null, insert_values);
    }

    /**
     * Insert a workout into the database.
     * @param workout   The workout to insert.
     * @return          The workout ID of the newly inserted workout.
     */
    long insertWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(WORKOUT_COLUMN_START_DATE, workout.getStartDate().getTime());
        insert_values.put(WORKOUT_COLUMN_DESCRIPTION, workout.getDescription());
        return db.insert(WORKOUT_TABLE_NAME, null, insert_values);
    }

    /**
     * Selects all the lifts in the workout. These are returned as a cursor in a readable format.
     * Some formatting is done to group together multiple lifts into number of sets if the weight
     * and reps are the same.
     * @param workout_id    The ID of the workout.
     * @return              The cursor containing the lift of the workout.
     */
    Cursor selectLiftsFromWorkoutCursor(long workout_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // TODO: Make this better overall. It works, but it's messy.
        final String SELECT_QUERY = "SELECT " +
                LIFT_COLUMN_LIFT_ID + " AS _id, " +
                EXERCISE_COLUMN_NAME + " || ': ' || " + LIFT_COLUMN_WEIGHT + " || ' for ' || Count(" + LIFT_COLUMN_LIFT_ID + ") || 'x' || " + LIFT_COLUMN_REPS + " AS FullLiftDescription" +
                " FROM " +
                LIFT_TABLE_NAME + " INNER JOIN " + EXERCISE_TABLE_NAME + " ON " + LIFT_TABLE_NAME + "." + LIFT_COLUMN_EXERCISE_ID + " = " + EXERCISE_TABLE_NAME + "." + EXERCISE_COLUMN_EXERCISE_ID +
                " WHERE " + LIFT_COLUMN_WORKOUT_ID + " = ?" +
                " GROUP BY " + EXERCISE_COLUMN_NAME + ", " + LIFT_COLUMN_WEIGHT + ", " + LIFT_COLUMN_REPS +
                " ORDER BY " + LIFT_COLUMN_START_DATE +
                " LIMIT 50;";
        Cursor database_results = db.rawQuery(SELECT_QUERY, new String[] {Long.toString(workout_id)});
        MatrixCursor header_row = new MatrixCursor(new String[] {"_id", "FullLiftDescription"});
        header_row.addRow(new Object[] {-1, "Lifts so far:"});
        return new MergeCursor(new Cursor[] {header_row, database_results});
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

        return lift_ids;
    }

    /**
     * Selects an exercise from the database given its ID.
     * @param exercise_id   The ID of the exercise.
     * @return              The exercise object.
     */
    Exercise selectExerciseFromExerciseId(long exercise_id) {
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

        return new Exercise(this, exercise_id, name, description, max_lift_id, last_workout_id);
    }

    /**
     * Selects the exercise name from a given ID.
     * @param exercise_id   Exercise ID to return a name for.
     * @return              The exercise name.
     */
    private String selectExerciseNameFromId(long exercise_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS  = {EXERCISE_COLUMN_NAME};

        String WHERE = EXERCISE_COLUMN_EXERCISE_ID + " = ?";
        String[] where_args = {Long.toString(exercise_id)};

        Cursor select_cursor = db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                null);

        boolean workout_in_db = select_cursor.moveToFirst();
        if (!workout_in_db) {
            return null;
        }

        String exercise_name = select_cursor.getString(
                select_cursor.getColumnIndexOrThrow(EXERCISE_COLUMN_NAME));
        select_cursor.close();

        return exercise_name;
    }

    /**
     * Selects all exercises that match the filter. These are returned as a cursor.
     * @param filter    String on which to filter the exercises.
     * @return          A cursor of all the matching exercises.
     */
    Cursor selectExercisesCursor(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { EXERCISE_COLUMN_EXERCISE_ID + " AS _id", EXERCISE_COLUMN_NAME };
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

        return selectWorkoutFromWorkoutId(workout_id);
    }

    /**
     * Selects the history of a particular exercise. This history is returned as a cursor in a readable
     * format.
     * @param exercise  The exercise of which to retrieve its history.
     * @return          A cursor containing the history of the exercise.
     */
    Cursor selectExerciseHistoryCursor(Exercise exercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        // TODO: Make this one better too...
        final String SELECT_QUERY = "SELECT " +
                EXERCISE_COLUMN_EXERCISE_ID + " AS _id, " +
                LIFT_COLUMN_START_DATE + "/43200000 AS HalfDay, " +
                LIFT_COLUMN_WEIGHT + " || ' for ' || Count(" + LIFT_COLUMN_LIFT_ID + ") || 'x' || " + LIFT_COLUMN_REPS + " || ' on ' || date(" + LIFT_COLUMN_START_DATE + "/1000, 'unixepoch') AS FullLiftDescription" +
                " FROM " + LIFT_TABLE_NAME +
                " WHERE " + LIFT_COLUMN_EXERCISE_ID + " = ?" +
                " GROUP BY " + LIFT_COLUMN_REPS + ", " + LIFT_COLUMN_WEIGHT + ", HalfDay" +
                " ORDER BY " + LIFT_COLUMN_START_DATE +
                " LIMIT 50;";
        Cursor database_results = db.rawQuery(SELECT_QUERY, new String[] {Long.toString(exercise.getExerciseId())});
        MatrixCursor header_row = new MatrixCursor(new String[] {"_id", "FullLiftDescription"});

        String exercise_name = this.selectExerciseNameFromId(exercise.getExerciseId());
        header_row.addRow(new Object[] {-1, exercise_name + " history:"});

        return new MergeCursor(new Cursor[] {header_row, database_results});
    }

    /**
     * Select a workout given its ID.
     * @param workout_id    The workout ID to select.
     * @return              The workout.
     */
    Workout selectWorkoutFromWorkoutId(long workout_id) {
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

        return new Workout(this, workout_id, description, selectLiftsByWorkoutId(id),start_date);
    }

    /**
     * Update an integer field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_int             Value to update.
     * @return
     */
    private int updateFieldIntFromId(
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
        return db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
    }

    /**
     * Update a long field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_long            Value to update.
     * @return  The number of rows updated? Not quite sure...
     */
    private int updateFieldLongFromId(
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
        return db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
    }

    /**
     * Update a string field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_string          Value to update.
     * @return  The number of rows updated? Not quite sure...
     */
    private int updateFieldStringFromId(
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
        return db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
    }

    /**
     * Update the description of an exercise.
     * @param exercise      Exercise to update.
     * @param description   Updated description.
     */
    void updateDescriptionOfExercise(Exercise exercise, String description)
    {
        updateFieldStringFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_DESCRIPTION,
                description);
    }

    /**
     * Update the most recent workout ID that an exercise was performed.
     * @param exercise      Exercise to update.
     * @param workout_id    Workout ID that the exercise was performed.
     */
    void updateLastWorkoutIdOfExercise(Exercise exercise, long workout_id)
    {
        updateFieldLongFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_LAST_WORKOUT_ID,
                workout_id);
    }

    /**
     * Update the ID of the maximum effort lift of the exercise.
     * @param exercise      Exercise to update.
     * @param max_lift_id   ID of the maximum effort lift.
     */
    void updateMaxLiftIdOfExercise(Exercise exercise, long max_lift_id)
    {
        updateFieldLongFromId(
                EXERCISE_TABLE_NAME,
                EXERCISE_COLUMN_EXERCISE_ID,
                exercise.getExerciseId(),
                EXERCISE_COLUMN_MAX_LIFT_ID,
                max_lift_id);
    }
    /**
     * Update the number of reps of a lift.
     * @param lift  Lift to update.
     * @param reps  Number of reps.
     */
    void updateRepsOfLift(Lift lift, int reps)
    {
        updateFieldIntFromId(
                LIFT_TABLE_NAME,
                LIFT_COLUMN_LIFT_ID,
                lift.getLiftId(),
                LIFT_COLUMN_REPS,
                reps);
    }

    /**
     * Update the weight of a lift.
     * @param lift      Lift to update.
     * @param weight    Lift weight.
     */
    void updateWeightOfLift(Lift lift, int weight)
    {
        updateFieldIntFromId(
                LIFT_TABLE_NAME,
                LIFT_COLUMN_LIFT_ID,
                lift.getLiftId(),
                LIFT_COLUMN_WEIGHT,
                weight);
    }
}
