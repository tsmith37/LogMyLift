package edu.wvu.tsmith.logmylift.lift;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.Start;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.workout.Workout;
import edu.wvu.tsmith.logmylift.workout.WorkoutDetailFragment;

/**
 * Created by tmssm on 6/8/2017.
 */

public class AddLift extends AppCompatActivity {
    LiftDbHelper lift_db_helper;
    Exercise current_exercise;
    boolean current_exercise_correct;

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

        this.lift_db_helper = new LiftDbHelper(this);
        if (lift_db_helper.selectExerciseCount() == 0) {
            new Exercise(this.lift_db_helper, "Bench Press", "Flat barbell bench press");
            new Exercise(this.lift_db_helper, "Squat", "Barbell squat");
            new Exercise(this.lift_db_helper, "Deadlift", "Conventional deadlift");
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

            FloatingActionButton add_lift_button = (FloatingActionButton) findViewById(R.id.add_lift_button);
            add_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.showAddLiftDialog();
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
        edit_workout_dialog_builder.setTitle(R.string.edit_workout_text);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final TextView workout_date_text = (TextView) edit_workout_dialog_view.findViewById(R.id.edit_workout_dialog_date_text);

        LiftDbHelper lift_db_helper = new LiftDbHelper(this);
        WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
        final Workout current_workout = lift_db_helper.selectWorkoutFromWorkoutId(getIntent().getLongExtra(WorkoutDetailFragment.workout_id, 0));
        workout_date_text.setText(workout_detail_fragment.current_workout.getReadableStartDate());
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.edit_workout_dialog_description_text);
        final String workout_before_editing_description = workout_detail_fragment.current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        edit_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                Snackbar update_workout_snackbar = Snackbar.make(findViewById(R.id.current_workout_list), "Workout updated.", Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo_text, new AddLift.UndoUpdateWorkoutListener(workout_detail_fragment.current_workout, workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();
                workout_detail_fragment.current_workout.setDescription(workout_description_text.getText().toString());
                workout_detail_fragment.reloadWorkoutDetails();
            }
        });

        edit_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.current_workout_list), "Workout not updated.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }

    private void showAddLiftDialog()
    {
        LayoutInflater li = LayoutInflater.from(this);
        final View add_lift_dialog_view = li.inflate(R.layout.add_lift_dialog_layout, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(this);
        add_lift_dialog_builder.setTitle(R.string.add_lift_button_text);
        add_lift_dialog_builder.setView(add_lift_dialog_view);
        current_exercise_correct = true;

        final SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[] {LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[] {android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        exercise_adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                current_exercise_correct = false;
                return lift_db_helper.selectExercisesCursor(constraint.toString());
            }
        });

        exercise_adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                int column_index = cursor.getColumnIndex(LiftDbHelper.EXERCISE_COLUMN_NAME);
                return cursor.getString(column_index);
            }
        });

        final AutoCompleteTextView exercise_input = (AutoCompleteTextView) add_lift_dialog_view.findViewById(R.id.exercise_input);
        exercise_input.setAdapter(exercise_adapter);;

        exercise_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeCurrentExercise(id);
            }
        });

        // Handle the positive button press.
        add_lift_dialog_builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!current_exercise_correct)
                {
                    Exercise potential_exercise_from_input = lift_db_helper.selectExerciseFromName(exercise_input.getText().toString());
                    if (potential_exercise_from_input != null)
                    {
                        current_exercise = potential_exercise_from_input;
                        current_exercise_correct = true;
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.edit_workout_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                int weight = -1;
                EditText weight_text = (EditText) add_lift_dialog_view.findViewById(R.id.weight_input);
                try {weight = Integer.parseInt(weight_text.getText().toString());}
                catch (Throwable ignored) {}
                int reps = -1;
                EditText reps_text = (EditText) add_lift_dialog_view.findViewById(R.id.reps_input);
                try {reps = Integer.parseInt(reps_text.getText().toString());}
                catch (Throwable ignored) {}

                if ((weight > 0) && (reps > 0))
                {
                    EditText comment_text = (EditText) add_lift_dialog_view.findViewById(R.id.comment_input);
                    String comment = comment_text.getText().toString();
                    WorkoutDetailFragment workout_detail_fragment = (WorkoutDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                    workout_detail_fragment.current_workout.addLift(current_exercise, reps, weight, comment);
                    //workout_detail_fragment.reloadWorkoutList();
                }
                else
                {
                    Snackbar.make(findViewById(R.id.edit_workout_button), "Lift not valid.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Handle the negative button press.
        add_lift_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.edit_workout_button), "Lift not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_lift_dialog = add_lift_dialog_builder.create();
        add_lift_dialog.show();
    }

    private void changeCurrentExercise(long exercise_id)
    {
        this.current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
        this.current_exercise_correct = true;
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
            this.workout_detail_fragment.reloadWorkoutDetails();
        }
    }
}
