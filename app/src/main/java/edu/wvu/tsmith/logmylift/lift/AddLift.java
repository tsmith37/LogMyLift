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
import android.widget.AutoCompleteTextView;

import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.Start;
import edu.wvu.tsmith.logmylift.exercise.AddExerciseDialog;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.exercise.PercentMaxCalculatorDialog;
import edu.wvu.tsmith.logmylift.workout.EditWorkoutDialog;
import edu.wvu.tsmith.logmylift.workout.SuggestedExercisesDialog;
import edu.wvu.tsmith.logmylift.workout.UndoEditWorkoutListener;
import edu.wvu.tsmith.logmylift.workout.Workout;
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
    WorkoutDetailFragment fragment;

    public static void start(Context context, Workout workout)
    {
        Intent starter = new Intent(context, AddLift.class);
        starter.putExtra(WorkoutDetailFragment.workout_parcel, workout);
        context.startActivity(starter);
    }

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
            arguments.putBoolean(WorkoutDetailFragment.enable_edit_key, true);
            this.fragment = new WorkoutDetailFragment();
            this.fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.workout_detail_container, this.fragment, "detail_fragment")
                    .commit();
        }
        else
        {
            this.fragment = (WorkoutDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, WorkoutDetailFragment.workout_parcel);
        }

        FloatingActionButton add_lift_button = findViewById(R.id.add_lift_button);
        if (add_lift_button != null)
        {
            this.initAddLiftButton(add_lift_button, this.fragment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, WorkoutDetailFragment.workout_parcel, this.fragment);
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
            //`
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, Start.class));
            return true;
        }
        else if (id == R.id.edit_workout_menu_item)
        {
            View snackbarParentView = findViewById(R.id.current_workout_list);
            EditWorkoutDialog editWorkoutDialog = new EditWorkoutDialog(this, workout_detail_fragment.current_workout);
            editWorkoutDialog.show(() -> {
                Snackbar update_workout_snackbar = Snackbar.make(snackbarParentView, R.string.workout_updated, Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo, new UndoEditWorkoutListener(editWorkoutDialog.workoutDescriptionBeforeEditing, workout_detail_fragment));
                update_workout_snackbar.show();
                workout_detail_fragment.setWorkoutDescription(editWorkoutDialog.workoutDescriptionAfterEditing);
                return 0;
            });
        }
        else if (id == R.id.workout_stats_menu_item)
        {
            WorkoutStatsDialog workout_stats_dialog = new WorkoutStatsDialog(this, new LiftDbHelper(this.getBaseContext()), workout_detail_fragment);
            workout_stats_dialog.show();
        }
        else if (id == R.id.add_exercise_menu_item)
        {
            View snackbar_parent_view = findViewById(R.id.current_workout_list);
            AddExerciseDialog add_exercise_dialog = new AddExerciseDialog(this, new LiftDbHelper(this), snackbar_parent_view, "");
            add_exercise_dialog.show();
        }
        else if (id == R.id.suggested_exercises_menu_item)
        {
            final AutoCompleteTextView text_view = new AutoCompleteTextView(this);
            final LiftDbHelper lift_db_helper = new LiftDbHelper(this);
            SuggestedExercisesDialog suggested_exercises_dialog = new SuggestedExercisesDialog(this, lift_db_helper, workout_detail_fragment.current_workout, text_view);
            suggested_exercises_dialog.show(new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    AddLiftParams add_lift_params = (AddLiftParams.createFromExistingExercise(lift_db_helper.selectExerciseFromName(text_view.getText().toString()).getExerciseId()));
                    workout_detail_fragment.showAddLiftDialog(lift_db_helper, add_lift_params);
                    return null;
                }
            });
        }
        else if (id == R.id.percent_max_menu_item)
        {
            LiftDbHelper liftDbHelper = new LiftDbHelper(this);
            Exercise mostRecentExercise = liftDbHelper.selectExerciseFromExerciseId(liftDbHelper.getSelectedExercise());
            PercentMaxCalculatorDialog max_calculator_dialog = new PercentMaxCalculatorDialog(this, mostRecentExercise);
            max_calculator_dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.add_lift_menu, menu);
        return true;
    }

    private void initAddLiftButton(FloatingActionButton add_lift_button, WorkoutDetailFragment fragment)
    {
        add_lift_button.setOnClickListener(this.addLift(fragment));
    }

    private View.OnClickListener addLift(WorkoutDetailFragment fragment)
    {
        View.OnClickListener addLiftListener = v ->
        {
            // Check if an exercise is currently selected.
            LiftDbHelper lift_db_helper = new LiftDbHelper(v.getContext());
            long selected_exercise_id = lift_db_helper.getSelectedExercise();
            boolean any_exercise_selected = (selected_exercise_id != -1);
            AddLiftParams add_lift_params = (any_exercise_selected ? AddLiftParams.createFromExistingExercise(selected_exercise_id) : AddLiftParams.createBlank());
            fragment.showAddLiftDialog(lift_db_helper, add_lift_params);
        };

        return addLiftListener;
    }
}
