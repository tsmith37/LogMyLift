package edu.wvu.tsmith.logmylift;

/**
 * Created by tmssm on 3/19/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;

// This class provides a public interface to the SQLite database used by the application.
public class LiftDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "LogMyLiftDb.db";
    public static final int DATABASE_VERSION = 1;

    public static final String EXERCISE_TABLE_NAME = "Exercise";
    public static final String EXERCISE_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String EXERCISE_COLUMN_DESCRIPTION = "Description";
    public static final String EXERCISE_COLUMN_LAST_WORKOUT_ID = "LastWorkoutId";
    public static final String EXERCISE_COLUMN_MAX_LIFT_ID = "MaxLiftId";
    public static final String EXERCISE_COLUMN_NAME = "Name";

    public static final String LIFT_TABLE_NAME = "Lift";
    public static final String LIFT_COLUMN_LIFT_ID = "LiftId";
    public static final String LIFT_COLUMN_EXERCISE_ID = "ExerciseId";
    public static final String LIFT_COLUMN_REPS = "Reps";
    public static final String LIFT_COLUMN_START_DATE = "StartDate";
    public static final String LIFT_COLUMN_WEIGHT = "Weight";
    public static final String LIFT_COLUMN_WORKOUT_ID = "WorkoutId";

    public static final String WORKOUT_TABLE_NAME = "Workout";
    public static final String WORKOUT_COLUMN_WORKOUT_ID = "WorkoutId";
    public static final String WORKOUT_COLUMN_DESCRIPTION = "Description";
    public static final String WORKOUT_COLUMN_START_DATE = "StartDate";

    public static final String CREATE_TABLE_EXERCISE =
            "CREATE TABLE Exercise (" +
            "ExerciseId INTEGER PRIMARY KEY," +
            "Name TEXT," +
            "Description TEXT," +
            "MaxLiftId INTEGER," +
            "LastWorkoutId INTEGER," +
            "FOREIGN KEY(MaxLiftId) REFERENCES Lift(LiftId)," +
            "FOREIGN KEY(LastWorkoutId) REFERENCES Workout(WorkoutId));";

    public static final String CREATE_TABLE_LIFT =
            "CREATE TABLE Lift (" +
                    "LiftId INTEGER PRIMARY KEY," +
                    "ExerciseId INTEGER," +
                    "Reps INTEGER," +
                    "StartDate INTEGER," +
                    "Weight INTEGER," +
                    "WorkoutId INTEGER," +
                    "FOREIGN KEY(ExerciseId) REFERENCES Exercise(ExerciseId)," +
                    "FOREIGN KEY(WorkoutId) REFERENCES Workout(WorkoutId));";

    public static final String CREATE_TABLE_WORKOUT =
            "CREATE TABLE Workout (" +
                    "WorkoutId INTEGER PRIMARY KEY," +
                    "Description TEXT," +
                    "StartDate INTEGER);";

    public LiftDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORKOUT);
        db.execSQL(CREATE_TABLE_LIFT);
        db.execSQL(CREATE_TABLE_EXERCISE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void deleteRowById(String table_name, String id_column_name, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String WHERE = id_column_name + " = ?";
        String[] where_args = { Long.toString(id)};
        db.delete(table_name, WHERE, where_args);
    }

    public String getExerciseNameFromId(long exercise_id) {
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

    public ArrayList<Long> getLiftsByWorkoutId(long workout_id) {
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

    public long insertExercise(String exercise_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(EXERCISE_COLUMN_NAME, exercise_name);
        return db.insert(EXERCISE_TABLE_NAME, null, insert_values);
    }

    public long insertLift(int reps, long exercise_id, Date start_date, int weight, long workout_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(LIFT_COLUMN_EXERCISE_ID, exercise_id);
        insert_values.put(LIFT_COLUMN_REPS, reps);
        insert_values.put(LIFT_COLUMN_START_DATE, (long)start_date.getTime());
        insert_values.put(LIFT_COLUMN_WEIGHT, weight);
        insert_values.put(LIFT_COLUMN_WORKOUT_ID, workout_id);
        return db.insert(LIFT_TABLE_NAME, null, insert_values);
    }

    public long insertWorkout(Date start_date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues insert_values = new ContentValues();
        insert_values.put(WORKOUT_COLUMN_START_DATE, (long)start_date.getTime());
        return db.insert(WORKOUT_TABLE_NAME, null, insert_values);
    }

    public Cursor selectLiftsFromWorkoutCursor(long workout_id) {
        SQLiteDatabase db = this.getReadableDatabase();
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

        return new Exercise(this, exercise_id, name, description, max_lift_id, last_workout_id);
    }

    public Cursor selectExercisesCursor(String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = { EXERCISE_COLUMN_EXERCISE_ID + " AS _id", EXERCISE_COLUMN_NAME };
        String SORT_ORDER = EXERCISE_COLUMN_NAME;
        String WHERE = EXERCISE_COLUMN_NAME + " LIKE ?";
        String[] where_args = { "%" + filter + "%" };

        return db.query(
                EXERCISE_TABLE_NAME,
                RETURN_COLUMNS,
                WHERE,
                where_args,
                null,
                null,
                SORT_ORDER);
    }

    public Workout selectLastWorkout() {
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

    public Cursor selectExerciseHistoryCursor(long exercise_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        final String SELECT_QUERY = "SELECT " +
                EXERCISE_COLUMN_EXERCISE_ID + " AS _id, " +
                LIFT_COLUMN_START_DATE + "/43200000 AS HalfDay, " +
                LIFT_COLUMN_WEIGHT + " || ' for ' || Count(" + LIFT_COLUMN_LIFT_ID + ") || 'x' || " + LIFT_COLUMN_REPS + " || ' on ' || date(" + LIFT_COLUMN_START_DATE + "/1000, 'unixepoch') AS FullLiftDescription" +
                " FROM " + LIFT_TABLE_NAME +
                " WHERE " + LIFT_COLUMN_EXERCISE_ID + " = ?" +
                " GROUP BY " + LIFT_COLUMN_REPS + ", " + LIFT_COLUMN_WEIGHT + ", HalfDay" +
                " ORDER BY " + LIFT_COLUMN_START_DATE +
                " LIMIT 50;";
        Cursor database_results = db.rawQuery(SELECT_QUERY, new String[] {Long.toString(exercise_id)});
        MatrixCursor header_row = new MatrixCursor(new String[] {"_id", "FullLiftDescription"});

        String exercise_name = this.getExerciseNameFromId(exercise_id);
        header_row.addRow(new Object[] {-1, exercise_name + " history:"});
        return new MergeCursor(new Cursor[] {header_row, database_results});
    }

    public Workout selectWorkoutFromWorkoutId(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] RETURN_COLUMNS = {
            WORKOUT_COLUMN_DESCRIPTION,
            WORKOUT_COLUMN_START_DATE
        };

        String WHERE = WORKOUT_COLUMN_WORKOUT_ID + " = ?";
        String[] where_args = { Long.toString(id) };

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

        return new Workout(this, id, description, getLiftsByWorkoutId(id),start_date);
    }

    public int setFieldIntFromId(
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

    public int setFieldLongFromId(
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

    public int setFieldStringFromId(
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
}
