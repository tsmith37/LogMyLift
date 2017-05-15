package edu.wvu.tsmith.logmylift;

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
                editWorkout();
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

    private void editWorkout() {
        LiftDbHelper lift_db_helper = new LiftDbHelper(getBaseContext());
        final Workout current_workout = lift_db_helper.selectWorkoutFromWorkoutId(getIntent().getLongExtra(WorkoutDetailFragment.workout_id, 0));
        LayoutInflater li = LayoutInflater.from(this);
        // Re-use the add workout dialog. It contains the same fields we want to use here.
        View edit_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(this);
        edit_workout_dialog_builder.setTitle(R.string.edit_workout_text);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final TextView edit_workout_date_text = (TextView) edit_workout_dialog_view.findViewById(R.id.add_workout_date_text);
        edit_workout_date_text.setText(current_workout.getReadableStartDate());
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.add_workout_description_dialog_text);
        workout_description_text.setText(current_workout.getDescription());
        edit_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (workout_description_text.getText().toString().isEmpty()) {
                    Snackbar.make(findViewById(R.id.edit_workout_button), "Workout name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else {
                    current_workout.setDescription(workout_description_text.getText().toString());
                    Snackbar.make(findViewById(R.id.edit_workout_button), "Workout updated.", Snackbar.LENGTH_LONG).show();

                    WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                    workout_detail_fragment.reload();
                }
            }
        });
        edit_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.edit_workout_button), "Workout not updated.", Snackbar.LENGTH_LONG).show();
            }
        });
        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }
}
