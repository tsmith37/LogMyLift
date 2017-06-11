package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.exercise.ExerciseListActivity;
import edu.wvu.tsmith.logmylift.lift.AddLift;
import edu.wvu.tsmith.logmylift.workout.Workout;
import edu.wvu.tsmith.logmylift.workout.WorkoutDetailFragment;
import edu.wvu.tsmith.logmylift.workout.WorkoutListActivity;

public class Start extends AppCompatActivity {
    private LiftDbHelper lift_db_helper;
    private Context current_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        current_context = getApplicationContext();
        lift_db_helper = new LiftDbHelper(current_context);

        Button continue_workout_button = (Button) this.findViewById(R.id.continue_workout_button);
        continue_workout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {continueLastWorkout();
            }
        });

        Button start_new_workout_button = (Button) this.findViewById(R.id.start_new_workout_button);
        start_new_workout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startNewWorkout();
            }
        });

        Button workout_history_button = (Button) this.findViewById(R.id.workout_history_button);
        workout_history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showWorkoutHistory();
            }
        });

        Button exercise_history_button = (Button) this.findViewById(R.id.exercise_button);
        exercise_history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showExerciseHistory();
            }
        });
    }

    /**
     * Begin a new workout.
     */
    private void startNewWorkout() {
        LayoutInflater li = LayoutInflater.from(this);
        View add_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder add_workout_dialog_builder = new AlertDialog.Builder(this);
        String new_workout_title = getString(R.string.new_workout) + ": " + new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        add_workout_dialog_builder.setTitle(new_workout_title);
        add_workout_dialog_builder.setView(add_workout_dialog_view);
        final EditText workout_description_text = (EditText) add_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        add_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Workout new_workout = new Workout(lift_db_helper, workout_description_text.getText().toString());
                goToWorkout(new_workout.getWorkoutId());
            }
        });

        add_workout_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.start_new_workout_button), R.string.workout_not_added, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_exercise_dialog = add_workout_dialog_builder.create();
        add_exercise_dialog.show();
    }

    /**
     * Continue the most recent workout.
     */
    private void continueLastWorkout() {
        Workout last_workout = lift_db_helper.selectLastWorkout();
        if (last_workout != null) {
            goToWorkout(last_workout.getWorkoutId());
        }
        else
        {
            // TODO: use this snackbar action to allow the user to start a new workout.
            Snackbar no_previous_workouts = Snackbar.make(findViewById(R.id.continue_workout_button), R.string.no_workouts_to_continue, Snackbar.LENGTH_LONG);
            no_previous_workouts.setAction(R.string.start_new_workout, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNewWorkout();
                }
            });
            no_previous_workouts.show();
        }
    }

    /**
     * Go to the AddLift of a particular workout.
     * @param workout_id    The ID of the workout.
     */
    private void goToWorkout(long workout_id) {
        Intent workout_intent = new Intent(current_context, AddLift.class);
        workout_intent.putExtra(WorkoutDetailFragment.workout_id, workout_id);

        startActivity(workout_intent);
    }

    /**
     * Show the ExerciseListActivity.
     */
    private void showExerciseHistory() {
        Intent exercise_list_intent = new Intent(current_context, ExerciseListActivity.class);
        startActivity(exercise_list_intent);
    }

    /**
     * Show the WorkoutListActivity.
     */
    private void showWorkoutHistory()
    {
        Intent workout_list_intent = new Intent(current_context, WorkoutListActivity.class);
        startActivity(workout_list_intent);
    }
}
