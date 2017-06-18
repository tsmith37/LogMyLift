package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 3/19/2017.
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

public class Exercise {
    private String description;
    private final long exercise_id;
    private long max_lift_id;
    private String name;
    private long last_workout_id;

    /**
     * Constructor of a new exercise. Given the SQLite database helper, name, and description
     * of the exercise, the exercise is instantiated, added into the database, and the exercise
     * ID is set.
     * @param lift_db_helper    SQLite database helper.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     */
    public Exercise(LiftDbHelper lift_db_helper, String name, String description) throws ExerciseAlreadyExistsException {
        if (lift_db_helper.exerciseNameExists(name))
        {
            throw new ExerciseAlreadyExistsException(name + " already exists.");
        }
        else
        {
            this.name = name;
            this.description = description;
            this.exercise_id = lift_db_helper.insertExercise(this);
        }
    }

    /**
     * Construct a previously existing exercise from its pieces. In this instance, the exercise ID
     * already exists in the database, so don't add it again.
     * @param exercise_id       Unique exercise ID.
     * @param name              Name of the exercise.
     * @param description       Exercise description.
     * @param max_lift_id       Lift of the maximum effort of the exercise.
     * @param last_workout_id   Most recent workout ID that the exercise was performed.
     */
    public Exercise(long exercise_id, String name, String description, long max_lift_id, long last_workout_id) {
        this.exercise_id = exercise_id;
        this.name = name;
        this.description = description;
        this.max_lift_id = max_lift_id;
        this.last_workout_id = last_workout_id;
    }

    // Public access to read-only members.
    public long getExerciseId() { return this.exercise_id; }
    public long getLastWorkoutId() { return this.last_workout_id; }
    public long getMaxLiftId() { return this.max_lift_id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    /**
     * Sets the name of this exercise.
     * @param name  The new name of the exercise.
     */
    void setName(LiftDbHelper lift_db_helper, String name) {
        this.name = name;
        lift_db_helper.updateNameOfExercise(this);
    }

    /**
     * Updates the description of the exercise.
     * @param description   Updated description.
     */
    void setDescription(LiftDbHelper lift_db_helper, String description) {
        this.description = description;
        lift_db_helper.updateDescriptionOfExercise(this);
    }

    /**
     * Updates the most recent workout ID of the exercise.
     * @param last_workout_id   Workout ID.
     */
    public void setLastWorkoutId(LiftDbHelper lift_db_helper, long last_workout_id) {
        this.last_workout_id = last_workout_id;
        lift_db_helper.updateLastWorkoutIdOfExercise(this);
    }

    /**
     * Updates the ID of the maximum effort lift of the exercise.
     * @param max_lift_id   The ID of the maximum effort lift.
     */
    public void setMaxLiftId(LiftDbHelper lift_db_helper, long max_lift_id) {
        this.max_lift_id = max_lift_id;
        lift_db_helper.updateMaxLiftIdOfExercise(this);
    }

    void delete(LiftDbHelper lift_db_helper)
    {
        lift_db_helper.deleteExercise(this);
    }

    /**
     * Clears the ID of the maximum effort lift. This should only happen if the only instance of a
     * lift with this exercise is deleted.
     */
    public void clearMaxLiftId(LiftDbHelper lift_db_helper) {
        this.max_lift_id = -1;
        lift_db_helper.updateMaxLiftIdOfExerciseToNull(this);
    }

    class ExerciseAlreadyExistsException extends RuntimeException
    {

        public ExerciseAlreadyExistsException(String message)
        {
            super(message);
        }
    }

    public static void showAddExerciseDialog(Context context, final View view, final LiftDbHelper lift_db_helper, String exercise_name_hint, final Callable<Long> postAddFunction)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View add_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder add_exercise_dialog_builder = new AlertDialog.Builder(context);
        add_exercise_dialog_builder.setTitle(R.string.create_exercise);
        add_exercise_dialog_builder.setView(add_exercise_dialog_view);
        final EditText exercise_name_edit_text = (EditText) add_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_edit_text.setText(exercise_name_hint);
        final EditText exercise_description_edit_text = (EditText) add_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);

        // Handle the positive button press.
        add_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (exercise_name_edit_text.getText().toString().isEmpty())
                {
                    Snackbar.make(view, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    try {
                        new Exercise(lift_db_helper, exercise_name_edit_text.getText().toString(), exercise_description_edit_text.getText().toString());
                        Snackbar.make(view, "Exercise added.", Snackbar.LENGTH_LONG).show();

                        if (null != postAddFunction)
                        {
                            try { postAddFunction.call(); }
                            catch (Exception e)
                            {
                                Snackbar.make(view, "Something bad has happened.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Snackbar.make(view, "Exercise already exists.", Snackbar.LENGTH_LONG).show();
                    }

                }
            }
        });

        // Handle the negative button press.
        add_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(view, "Exercise not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();
        add_exercise_dialog.show();
    }
}