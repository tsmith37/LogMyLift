package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This class provides the activity to add a lift to the current workout, as well as providing
 * information to the user regarding the current workout, as well as stats about the most recent
 * occurence of the selected workout. This activity is one of the main focal points of the app,
 * so it has a lot going on. Here are the main parts:
 * - Exercise Selection: This has three moving parts: 1) The exercise spinner. This is how the user
 *                       will select which exercise they desire. 2) The exercise filter. The user
 *                       will be able to narrow down the exercises located in the exercise spinner by
 *                       simply typing into the filter. 3) The exercise description. This is fairly
 *                       self explanatory, as it should just change when a different exercise is
 *                       selected.
 * - History of the Currently Selected Exercise: When a different exercise is selected, the user should
 *                                               be given a brief "history" of the exercise. This will
 *                                               tell the user how much weight they can use or how many
 *                                               reps they should be able to do based on previous
 *                                               experience.
 * - Adding a Lift: In addition to selecting the exercise, the user will input a weight and number
 *                  of reps for the lift. Apart from the exercise selection, this should encompass
 *                  three UI parts: the weight & reps text inputs, and the "Add lifts" button.
 * - Workout So Far: Similar to the "History of the Currently Selected Exercise", the user should be
 *                   shown what they have done so far in the current workout.
 * - Adding an Exercise: If all the exercises that a user may need aren't already added, provide a
 *                       basic UI to do that on this screen. This saves the user the hassle of going
 *                       back to the main menu to do so.
 * @author Tommy Smith
 */
public class AddLiftToWorkoutActivity extends AppCompatActivity {
    private LiftDbHelper lift_db_helper;
    private Workout current_workout;
    private Exercise current_exercise;

    /**
     * On create method. This is where the previously mentioned UI elements should be configured.
     * @param savedInstanceState    The state of the application if it was saved? Not really sure.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Figure out what this saved instance state does...
        super.onCreate(savedInstanceState);

        // SET THE LAYOUT OF THE ACTIVITY.
        setContentView(R.layout.activity_add_lift_to_workout);

        // ADDING AN EXERCISE IS DONE VIA THE TOOLBAR.
        // Configure the toolbar. The toolbar contains a button that will bring up a dialog to allow
        // the user to add an exercise, as well as the title of the activity.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_lift_to_workout_text);
        // The rest of this configuration is done in the onCreateOptionsMenu() and onOptionsItemSelected() handlers.

        Context current_context = getApplicationContext();
        lift_db_helper = new LiftDbHelper(current_context);

        // GET THE CURRENT WORKOUT FROM THE INTENT PASSED TO THIS ACTIVITY.
        Intent workout_intent = getIntent();
        // TODO: maybe don't just set the default to 0...
        long default_workout_id = 0;
        long current_workout_id = workout_intent.getLongExtra(LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID, default_workout_id);
        current_workout = lift_db_helper.selectWorkoutFromWorkoutId(current_workout_id);

        // SET UP EXERCISE SPINNER.
        reloadExerciseSpinner();
        Spinner exercise_spinner = (Spinner) this.findViewById(R.id.exercise_spinner);
        if (exercise_spinner != null) {
            exercise_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    changeCurrentExercise(id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        changeCurrentExercise(exercise_spinner.getSelectedItemId());

        // SET UP EXERCISE FILTERING.
        final EditText exercise_filter_input = (EditText) this.findViewById(R.id.exercise_filter_input);
        if (exercise_filter_input != null) {
            exercise_filter_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    reloadExerciseSpinner();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        // SET UP CURRENT WORKOUT TABLE.
        reloadCurrentWorkout();

        // SET UP EXERCISE HISTORY TABLE.
        reloadExerciseHistory();

        // SET UP "ADD LIFT" BUTTON.
        Button add_lift_button = (Button) this.findViewById(R.id.add_lift_button);
        if (add_lift_button != null) {
            add_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CurrentLiftIsValid()) {
                        EditText weight_text = (EditText) findViewById(R.id.weight_input);
                        int weight = Integer.parseInt(weight_text.getText().toString());
                        EditText reps_text = (EditText) findViewById(R.id.reps_input);
                        int reps = Integer.parseInt(reps_text.getText().toString());
                        current_workout.AddLift(current_exercise, reps, weight);

                        reloadCurrentWorkout();
                        reloadExerciseHistory();
                    }
                }
            });
        }
    }

    // CONFIGURE THE TOOLBAR LAYOUT.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_toolbar, menu);
        return true;
    }

    // HANDLE THE BUTTON PRESS OF THE TOOLBAR. SO FAR, THIS ONLY INCLUDES ADDING AN EXERCISE.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int toolbar_item_id = item.getItemId();

        // Handle add exercise button.
        if (toolbar_item_id == R.id.add_exercise_toolbar_item) {
            showAddExerciseDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the add exercise dialog. This allows the user to insert a new exercise into the database for selection.
     */
    private void showAddExerciseDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View add_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder add_exercise_dialog_builder = new AlertDialog.Builder(this);
        add_exercise_dialog_builder.setTitle(R.string.create_exercise_text);
        add_exercise_dialog_builder.setView(add_exercise_dialog_view);
        final EditText exercise_name_text = (EditText) add_exercise_dialog_view.findViewById(R.id.add_exercise_name_dialog_text);
        final EditText exercise_description_text = (EditText) add_exercise_dialog_view.findViewById(R.id.add_exercise_description_dialog_text);
        add_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (exercise_name_text.getText().toString().isEmpty())
                {
                    Snackbar.make(findViewById(R.id.add_exercise_toolbar_item), "Exercise name not valid.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else
                {
                    Exercise new_exercise = new Exercise(lift_db_helper, exercise_name_text.getText().toString(), exercise_description_text.getText().toString());
                    reloadExerciseSpinner();
                }
            }
        });

        add_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_exercise_toolbar_item), "Exercise not added.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();
        add_exercise_dialog.show();
    }

    /**
     * Check if the lift is valid as currently selected by the user.
     * @return  True if the lift is valid; false otherwise.
     */
    private boolean CurrentLiftIsValid() {
        EditText weight_text = (EditText) findViewById(R.id.weight_input);
        try
        {
            Integer.parseInt(weight_text.getText().toString());
        }
        catch (Throwable e)
        {
            Snackbar.make(findViewById(R.id.weight_input), "Weight not valid.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        EditText reps_text = (EditText) findViewById(R.id.reps_input);
        try
        {
            Integer.parseInt(reps_text.getText().toString());
        }
        catch (Throwable e)
        {
            Snackbar.make(findViewById(R.id.reps_input), "Reps not valid.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Change the current exercise. This reloads the exercise history and changes the exercise description displayed.
     * @param exercise_id   The new exercise ID.
     */
    private void changeCurrentExercise(long exercise_id)
    {
        this.current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
        reloadExerciseHistory();

        TextView exercise_description_text_view = (TextView) this.findViewById(R.id.exercise_description_text);
        if (exercise_description_text_view != null) {
            exercise_description_text_view.setText(this.current_exercise.getDescription());
        }
    }

    /**
     * Reload the exercise spinner with all exercises that match the filter.
     */
    private void reloadExerciseSpinner()
    {
        final EditText exercise_filter_input = (EditText) this.findViewById(R.id.exercise_filter_input);
        Cursor exercises = null;
        if (exercise_filter_input != null) {
            exercises = lift_db_helper.selectExercisesCursor(exercise_filter_input.getText().toString());
        }
        final SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                this,
                R.layout.big_spinner_item,
                exercises,
                new String[] {LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[] {R.id.big_spinner_item_text},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        exercise_adapter.setDropDownViewResource(R.layout.big_spinner_item);

        Spinner exercise_spinner = (Spinner) this.findViewById(R.id.exercise_spinner);
        if (exercise_spinner != null) {
            exercise_spinner.setAdapter(exercise_adapter);
        }
    }

    /**
     * Reload the exercise history based on the current exercise.
     */
    private void reloadExerciseHistory()
    {
        ListView exercise_history_list = (ListView) this.findViewById(R.id.exercise_history_list);
        Cursor exercise_history_cursor = lift_db_helper.selectExerciseHistoryCursor(current_exercise);
        final CurrentWorkoutCursorAdapter exercise_history_adapter = new CurrentWorkoutCursorAdapter(
                this,
                exercise_history_cursor);
        if (exercise_history_list != null) {
            exercise_history_list.setAdapter(exercise_history_adapter);
        }
    }

    /**
     * Reload the current workout table.
     */
    private void reloadCurrentWorkout()
    {
        ListView current_workout_list = (ListView) this.findViewById(R.id.current_workout_list);
        Cursor current_workout_cursor = lift_db_helper.selectLiftsFromWorkoutCursor(current_workout.getWorkoutId());
        final CurrentWorkoutCursorAdapter current_workout_adapter = new CurrentWorkoutCursorAdapter(
                this,
                current_workout_cursor);
        if (current_workout_list != null) {
            current_workout_list.setAdapter(current_workout_adapter);
        }
    }
}
