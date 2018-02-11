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
public class WorkoutDetailActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Set up the button used to edit the workout's description.
        FloatingActionButton edit_workout_button = findViewById(R.id.edit_workout_button);
        edit_workout_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showEditWorkoutDialog();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
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
        if (savedInstanceState == null)
        {
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
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
    private void showEditWorkoutDialog()
    {
        // Create the edit workout dialog.
        LayoutInflater li = LayoutInflater.from(this);
        View edit_workout_dialog_view = li.inflate(R.layout.edit_workout_dialog, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(this);

        // Set the edit workout dialog to reflect the current workout details.
        // The title of the dialog contains the date of the workout.
        final WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
        String edit_workout_title = getString(R.string.edit_workout) + ": " + workout_detail_fragment.current_workout.getReadableStartDate();
        edit_workout_dialog_builder.setTitle(edit_workout_title);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);

        // Set the description of the workout to what it is now, allowing the user to edit it.
        final EditText workout_description_text = edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        final String workout_before_editing_description = workout_detail_fragment.current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        // Handle the user editing the workout description.
        edit_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Notify the user that the workout description was updated.
                Snackbar update_workout_snackbar = Snackbar.make(findViewById(R.id.current_workout_list), R.string.workout_updated, Snackbar.LENGTH_LONG);

                // Provide the user the option to undo their changes.
                update_workout_snackbar.setAction(R.string.undo, new WorkoutDetailActivity.UndoUpdateWorkoutListener(workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();

                // Notify the fragment containing the workout description that the description has changed.
                workout_detail_fragment.setWorkoutDescription(workout_description_text.getText().toString());
            }
        });

        // Handle the user canceling the edit.
        edit_workout_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Snackbar.make(findViewById(R.id.current_workout_list), R.string.workout_not_updated, Snackbar.LENGTH_LONG).show();
            }
        });

        // Show the dialog.
        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }

    /**
     * Provides an interface for allowing a workout description update to be undone.
     */
    private class UndoUpdateWorkoutListener implements View.OnClickListener
    {
        // The description of the workout before the update.
        final String old_description;

        // The fragment where the workout's description is displayed.
        final WorkoutDetailFragment workout_detail_fragment;

        /**
         * Constructs the listener.
         * @param old_description           The description of the workout before the update.
         * @param workout_detail_fragment   The fragment where the workout's description is displayed.
         */
        UndoUpdateWorkoutListener(String old_description, WorkoutDetailFragment workout_detail_fragment)
        {
            super();
            this.old_description = old_description;
            this.workout_detail_fragment = workout_detail_fragment;
        }

        /**
         * Sets the workout description back to what it was before the update.
         * @param v The calling view.
         */
        @Override
        public void onClick(View v)
        {
            this.workout_detail_fragment.setWorkoutDescription(old_description);
        }
    }
}