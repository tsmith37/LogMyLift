package edu.wvu.tsmith.logmylift.workout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.lift.AddLiftToWorkoutActivity;
import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * An activity representing a single Workout detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link WorkoutListActivity}.
 */
public class WorkoutDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
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
            WorkoutDetailFragment fragment = new WorkoutDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.workout_detail_container, fragment, "detail_fragment")
                    .commit();
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
            navigateUpTo(new Intent(this, WorkoutListActivity.class));
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
        edit_workout_dialog_builder.setTitle(R.string.edit_workout_text);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final TextView workout_date_text = (TextView) edit_workout_dialog_view.findViewById(R.id.edit_workout_dialog_date_text);

        LiftDbHelper lift_db_helper = new LiftDbHelper(this);
        final Workout current_workout = lift_db_helper.selectWorkoutFromWorkoutId(getIntent().getLongExtra(WorkoutDetailFragment.workout_id, 0));
        workout_date_text.setText(current_workout.getReadableStartDate());
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.edit_workout_dialog_description_text);
        final String workout_before_editing_description = current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        edit_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                Snackbar update_workout_snackbar = Snackbar.make(findViewById(R.id.current_workout_list), "Workout updated.", Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo_text, new UndoUpdateWorkoutListener(current_workout, workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();
                current_workout.setDescription(workout_description_text.getText().toString());
                workout_detail_fragment.reload();
            }
        });

        edit_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.current_workout_list), "Workout not updated.", Snackbar.LENGTH_LONG).show();
            }
        });

        edit_workout_dialog_builder.setNeutralButton("Go to workout.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent workout_intent = new Intent(getBaseContext(), AddLiftToWorkoutActivity.class);
                workout_intent.putExtra(LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID, current_workout.getWorkoutId());
                startActivity(workout_intent);
            }
        });

        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }

    private class UndoUpdateWorkoutListener implements View.OnClickListener
    {
        Workout current_workout;
        String old_description;
        WorkoutDetailFragment workout_detail_fragment;

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
            this.workout_detail_fragment.reload();
        }
    }
}
