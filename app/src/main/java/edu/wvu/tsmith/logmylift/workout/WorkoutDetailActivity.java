package edu.wvu.tsmith.logmylift.workout;

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
            arguments.putParcelable(WorkoutDetailFragment.workout_parcel, getIntent().getParcelableExtra(WorkoutDetailFragment.workout_parcel));
            final WorkoutDetailFragment fragment = new WorkoutDetailFragment();
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
     * Show a dialog to the user allowing them to edit the description of the workout.
     */
    private void showEditWorkoutDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View edit_workout_dialog_view = li.inflate(R.layout.edit_workout_dialog, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(this);

        // Set the edit workout dialog to reflect the current workout details.
        final WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
        String edit_workout_title = getString(R.string.edit_workout) + ": " + workout_detail_fragment.current_workout.getReadableStartDate();
        edit_workout_dialog_builder.setTitle(edit_workout_title);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        final String workout_before_editing_description = workout_detail_fragment.current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        // Change the workout details.
        edit_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar update_workout_snackbar = Snackbar.make(findViewById(R.id.current_workout_list), R.string.workout_updated, Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo, new WorkoutDetailActivity.UndoUpdateWorkoutListener(workout_detail_fragment.current_workout, workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();
                workout_detail_fragment.setWorkoutDescription(workout_description_text.getText().toString());
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
            this.workout_detail_fragment.setWorkoutDescription(old_description);
        }
    }
}
