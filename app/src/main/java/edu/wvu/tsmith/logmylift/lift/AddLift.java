package edu.wvu.tsmith.logmylift.lift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.Start;
import edu.wvu.tsmith.logmylift.exercise.AddExerciseDialog;
import edu.wvu.tsmith.logmylift.workout.EditWorkoutDialog;
import edu.wvu.tsmith.logmylift.workout.UndoEditWorkoutListener;
import edu.wvu.tsmith.logmylift.workout.WorkoutDetailFragment;
import edu.wvu.tsmith.logmylift.workout.WorkoutStatsDialog;

/**
 * Created by Tommy Smith on 6/8/2017.
 * Activity to add a lift to a workout. This activity displays the current workout history in a CardView,
 * which allows long-clicking to edit, delete, or copy each lift. A long-click also allows the user
 * to view past information about the lift's exercise. A FAB is present on the page that allows the
 * user to create a new lift via a dialog box. Long-clicking the FAB allows a new exercise to be added
 * or a new lift to be added. A menu is present in the activity with two menu options: one to edit the
 * workout description, and one to display a dialog with statistics about the workout.
 */

public class AddLift extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lift_layout);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

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

            // If the FAB is clicked, show the add lift dialog.
            final FloatingActionButton add_lift_button = findViewById(R.id.add_lift_button);
            add_lift_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Check if an exercise is currently selected.
                    LiftDbHelper lift_db_helper = new LiftDbHelper(v.getContext());
                    long selected_exercise_id = lift_db_helper.getSelectedExercise();
                    boolean any_exercise_selected = (selected_exercise_id != -1);
                    AddLiftParams add_lift_params = (any_exercise_selected ? AddLiftParams.createFromExistingExercise(selected_exercise_id) : AddLiftParams.createBlank());
                    fragment.showAddLiftDialog(lift_db_helper, add_lift_params);
                }
            });

            // If the FAB is long clicked, offer options to add a new exercise or a new lift.
            add_lift_button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder dialog_builder = new AlertDialog.Builder(AddLift.this);
                    String[] choices = {getString(R.string.add_exercise), getString(R.string.add_lift)};
                    final Context current_context = v.getContext();
                    final LiftDbHelper lift_db_helper = new LiftDbHelper(current_context);
                    dialog_builder.setItems(choices, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0:
                                {
                                    AddExerciseDialog add_exercise_dialog = new AddExerciseDialog(current_context, lift_db_helper, add_lift_button, "");
                                    add_exercise_dialog.show();
                                    break;
                                }
                                case 1:
                                {
                                    // Check if an exercise is currently selected.
                                    long selected_exercise_id = lift_db_helper.getSelectedExercise();
                                    boolean any_exercise_selected = (selected_exercise_id != -1);
                                    AddLiftParams add_lift_params = (any_exercise_selected ? AddLiftParams.createFromExistingExercise(selected_exercise_id) : AddLiftParams.createBlank());
                                    fragment.showAddLiftDialog(lift_db_helper, add_lift_params);
                                    break;
                                }
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        final WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");

        if (id == android.R.id.home)
        {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, Start.class));
            return true;
        }
        else if (id == R.id.edit_workout_menu_item)
        {
            final View snackbar_parent_view = findViewById(R.id.current_workout_list);
            final EditWorkoutDialog edit_workout_dialog = new EditWorkoutDialog(this, workout_detail_fragment.current_workout, snackbar_parent_view);
            edit_workout_dialog.show(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Snackbar update_workout_snackbar = Snackbar.make(snackbar_parent_view, R.string.workout_updated, Snackbar.LENGTH_LONG);
                    update_workout_snackbar.setAction(R.string.undo, new UndoEditWorkoutListener(edit_workout_dialog.workout_description_before_editing, workout_detail_fragment));
                    update_workout_snackbar.show();
                    workout_detail_fragment.setWorkoutDescription(edit_workout_dialog.workout_description_after_editing);
                    return 0;
                }
            });
        }
        else if (id == R.id.workout_stats_menu_item)
        {
            WorkoutStatsDialog workout_stats_dialog = new WorkoutStatsDialog(this, new LiftDbHelper(this.getBaseContext()), workout_detail_fragment);
            workout_stats_dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.add_lift_menu, menu);
        return true;
    }
}
