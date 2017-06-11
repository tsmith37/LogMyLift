package edu.wvu.tsmith.logmylift.lift;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.Start;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.workout.Workout;
import edu.wvu.tsmith.logmylift.workout.WorkoutDetailFragment;

/**
 * Created by Tommy Smith on 6/8/2017.
 * Activity to add a lift to a workout. This activity displays the current workout history in a
 * CardView, which allows swiping to delete each lift and long-clicking to edit each lift. The FAB
 * allows a new lift to be added via a dialog box, and long-clicking the FAB allows a new exercise
 * to be added or a new lift to be added.
 */

public class AddLift extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lift_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton edit_workout_button = (FloatingActionButton) findViewById(R.id.edit_workout_button);
        edit_workout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditWorkoutDialog();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // If no exercises are available, add three default ones.
        LiftDbHelper lift_db_helper = new LiftDbHelper(this);
        if (lift_db_helper.selectExerciseCount() == 0) {
            new Exercise(lift_db_helper, getString(R.string.bench_press), getString(R.string.bench_press_description));
            new Exercise(lift_db_helper, getString(R.string.squat), getString(R.string.squat_description));
            new Exercise(lift_db_helper, getString(R.string.deadlift), getString(R.string.deadlift_description));
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(WorkoutDetailFragment.workout_id, getIntent().getLongExtra(WorkoutDetailFragment.workout_id, 0));
            final WorkoutDetailFragment fragment = new WorkoutDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.workout_detail_container, fragment, "detail_fragment")
                    .commit();

            // If the FAB is clicked, show the add lift dialog.
            final FloatingActionButton add_lift_button = (FloatingActionButton) findViewById(R.id.add_lift_button);
            add_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.showAddLiftDialog();
                }
            });

            // If the FAB is long clicked, offer options to either add a new exercise or a new lift.
            add_lift_button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog_builder = new AlertDialog.Builder(AddLift.this);
                    String[] choices = {getString(R.string.add_exercise), getString(R.string.add_lift)};
                    dialog_builder.setItems(choices, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which) {
                                case 0:
                                    fragment.showAddExerciseDialog("");
                                    break;
                                case 1:
                                    fragment.showAddLiftDialog();
                            }
                        }
                    });
                    AlertDialog dialog = dialog_builder.create();
                    dialog.show();
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, Start.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show a dialog to the user allowing them to edit the description of the workout or go to the workout.
     */
    private void showEditWorkoutDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View edit_workout_dialog_view = li.inflate(R.layout.edit_workout_dialog, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(this);
        edit_workout_dialog_builder.setTitle(R.string.edit_workout);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);

        // Set the edit workout dialog to reflect the current workout details.
        WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
        final TextView workout_date_text = (TextView) edit_workout_dialog_view.findViewById(R.id.date_text_view);
        workout_date_text.setText(workout_detail_fragment.current_workout.getReadableStartDate());
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        final String workout_before_editing_description = workout_detail_fragment.current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        // Change the workout details.
        edit_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                Snackbar update_workout_snackbar = Snackbar.make(findViewById(R.id.current_workout_list), R.string.workout_updated, Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo, new AddLift.UndoUpdateWorkoutListener(workout_detail_fragment.current_workout, workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();
                workout_detail_fragment.current_workout.setDescription(workout_description_text.getText().toString());
                workout_detail_fragment.reloadWorkoutDetails();
            }
        });

        // Cancel the changes.
        edit_workout_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.current_workout_list), R.string.workout_not_updated, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }

    // Undo updating the workout.
    private class UndoUpdateWorkoutListener implements View.OnClickListener
    {
        final Workout current_workout;
        final String old_description;
        final WorkoutDetailFragment workout_detail_fragment;

        UndoUpdateWorkoutListener(Workout current_workout, String old_description, WorkoutDetailFragment workout_detail_fragment)
        {
            super();
            this.current_workout = current_workout;
            this.old_description = old_description;
            this.workout_detail_fragment = workout_detail_fragment;
        }

        @Override
        public void onClick(View v)
        {
            this.current_workout.setDescription(old_description);
            this.workout_detail_fragment.reloadWorkoutDetails();
        }
    }
}
