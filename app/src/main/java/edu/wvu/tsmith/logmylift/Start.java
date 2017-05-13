package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Start extends AppCompatActivity {
    private LiftDbHelper lift_db_helper;
    private Context current_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Button add_exercise_button = (Button) this.findViewById(R.id.add_exercise_button);
        add_exercise_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showAddExerciseDialog();
            }
        });
    }

    private void startNewWorkout() {
        Workout new_workout = new Workout(lift_db_helper);
        goToWorkout(new_workout.getWorkoutId());
    }

    private void continueLastWorkout() {
        Workout last_workout = lift_db_helper.selectLastWorkout();
        goToWorkout(last_workout.getWorkoutId());
    }

    private void goToWorkout(long workout_id) {
        Intent workout_intent = new Intent(current_context, AddLiftToWorkoutActivity.class);
        workout_intent.putExtra(LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID, workout_id);
        startActivity(workout_intent);
        finish();
    }

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
                    Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else
                {
                    Exercise new_exercise = new Exercise(lift_db_helper, exercise_name_text.getText().toString(), exercise_description_text.getText().toString());
                }
            }
        });

        add_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise not added.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();
        add_exercise_dialog.show();
    }
}
