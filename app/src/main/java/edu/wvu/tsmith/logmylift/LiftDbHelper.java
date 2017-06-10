package edu.wvu.tsmith.logmylift;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.workout.Workout;

/**
 * The LiftDbHelper class provides a public interface to the SQLite database used by the application.
 * This is helpful because all database operations should be central to this class.
 * Created by tmssm on 3/19/2017.
 * @author Tommy Smith
 */
public class LiftDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LogMyLiftDb.db";
    private static final int DATABASE_VERSION = 1;

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

    /**
     * Default constructor for the SQLiteOpenHelper class.
     *
     * @param context Application context.
     */
    public LiftDbHelper(Context context) {
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
        return new Exercise(this, exercise_id, name, description, max_lift_id, last_workout_id);
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
        return new Exercise(this, exercise_id, name, description, max_lift_id, last_workout_id);
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

        Cursor exercises_cursor = db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                EXERCISE_COLUMN_NAME);
        return exercises_cursor;
    }

    /**
     * Selects all exercises, returned as a list.
     */
    public List<Exercise> selectExerciseList(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { EXERCISE_COLUMN_EXERCISE_ID, EXERCISE_COLUMN_NAME, EXERCISE_COLUMN_DESCRIPTION, EXERCISE_COLUMN_LAST_WORKOUT_ID, EXERCISE_COLUMN_MAX_LIFT_ID };
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
        List<Exercise> exercise_list = new ArrayList<>();

        while(select_cursor.moveToNext()) {
            exercise_list.add(new Exercise(
                    this,
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
        return new Lift(this, lift_id, exercise, reps, start_date, weight, workout_id, comment);
    }

    /**
     * Select a list of lift objects based on the exercise ID.
     * @param exercise  The exercise of which to get all the lifts.
     * @return          An ArrayList of the lifts.
     */
    public ArrayList<Lift> selectExerciseHistoryLifts(Exercise exercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { LIFT_COLUMN_LIFT_ID };
        String WHERE = LIFT_COLUMN_EXERCISE_ID + " LIKE ?";
        String[] where_args = { Long.toString(exercise.getExerciseId()) };
        String SORT_ORDER = LIFT_COLUMN_LIFT_ID + " DESC";
        Cursor select_cursor = db.query(
                LIFT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                SORT_ORDER);
        ArrayList<Lift> exercise_history_lifts = new ArrayList<>();

        while(select_cursor.moveToNext()) {
            long current_lift_id = select_cursor.getLong(select_cursor.getColumnIndexOrThrow(LIFT_COLUMN_LIFT_ID));
            exercise_history_lifts.add(selectLiftFromLiftId(current_lift_id));
        }
        select_cursor.close();
        db.close();
        return exercise_history_lifts;
    }

    /**
     * Selects all workouts, returned as an array list.
     * @param filter    Filter on the workout name.
     * @return          An ArrayList of workouts.
     */
    public ArrayList<Workout> selectWorkoutList(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { WORKOUT_COLUMN_WORKOUT_ID, WORKOUT_COLUMN_DESCRIPTION, WORKOUT_COLUMN_START_DATE };
        String WHERE = WORKOUT_COLUMN_DESCRIPTION + " LIKE ?";
        String SORT_ORDER = WORKOUT_COLUMN_START_DATE + " DESC";
        String[] where_args = { "%" + filter + "%" };
        Cursor select_cursor = db.query(
                WORKOUT_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                SORT_ORDER);
        ArrayList<Workout> workout_list = new ArrayList<>();

        while(select_cursor.moveToNext()) {
            workout_list.add(new Workout(
                    this,
                    select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID)),
                    select_cursor.getString(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_DESCRIPTION)),
                    selectLiftsByWorkoutId(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_WORKOUT_ID))),
                    new Date(select_cursor.getLong(select_cursor.getColumnIndexOrThrow(WORKOUT_COLUMN_START_DATE)))));
        }
        select_cursor.close();
        db.close();
        return workout_list;
    }

    /**
     * Select a workout given its ID.
     * @param workout_id    The workout ID to select.
     * @return              The workout.
     */
    public Workout selectWorkoutFromWorkoutId(long workout_id) {
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
        return new Workout(this, workout_id, description, selectLiftsByWorkoutId(workout_id),start_date);
    }

    /**
     * Update an integer field of a row based on its ID. Generic function in order to make the
     * calling functions easier to generate.
     * @param table_name            Table to update.
     * @param id_column_name        Column name on which to identify the row to update.
     * @param id                    ID to identify the row to update.
     * @param field_column_name     Column name to update.
     * @param field_int             Value to update.
     * @return  Not really sure...
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
        int to_return = db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
        return to_return;
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
        int to_return = db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
        return to_return;
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
        int to_return = db.update(
                table_name,
                update_values,
                WHERE,
                where_args);
        db.close();
        return to_return;
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

    /**
     * Update the comment of the lift. The comment should already be updated internally in the lift object.
     * @param lift  Lift to update.
     */
    public void updateCommentOfLift(Lift lift)
    {
        updateFieldStringFromId(
                LIFT_TABLE_NAME,
                LIFT_COLUMN_LIFT_ID,
                lift.getLiftId(),
                LIFT_COLUMN_COMMENT,
                lift.getComment());
    }

    /**
     * Update the number of reps of a lift. The lift object should already have the reps updated internally.
     * @param lift  Lift to update.
     */
    public void updateRepsOfLift(Lift lift)
    {
        updateFieldIntFromId(
                LIFT_TABLE_NAME,
                LIFT_COLUMN_LIFT_ID,
                lift.getLiftId(),
                LIFT_COLUMN_REPS,
                lift.getReps());
    }

    /**
     * Update the weight of a lift. The lift object should already have this weight updated internally.
     * @param lift      Lift to update.
     */
    public void updateWeightOfLift(Lift lift)
    {
        updateFieldIntFromId(
                LIFT_TABLE_NAME,
                LIFT_COLUMN_LIFT_ID,
                lift.getLiftId(),
                LIFT_COLUMN_WEIGHT,
                lift.getWeight());
    }

    /**
     * Update the description of a workout. The worjout object should already have been updated internally.
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
}
