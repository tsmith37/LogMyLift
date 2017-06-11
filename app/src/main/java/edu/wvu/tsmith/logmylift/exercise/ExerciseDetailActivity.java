package edu.wvu.tsmith.logmylift.exercise;

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

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * An activity representing a single Exercise detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ExerciseListActivity}.
 */
public class ExerciseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton edit_exercise_button = (FloatingActionButton) findViewById(R.id.edit_exercise_button);
        edit_exercise_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editExercise();
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
            // TODO: Maybe don't just set the default to 0...
            arguments.putLong(ExerciseDetailFragment.exercise_id, getIntent().getLongExtra(ExerciseDetailFragment.exercise_id, 0));
            ExerciseDetailFragment fragment = new ExerciseDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.exercise_detail_container, fragment, "detail_fragment")
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
            navigateUpTo(new Intent(this, ExerciseListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the dialog to edit the current exercise description and update it acordingly.
     */
    private void editExercise() {
        LiftDbHelper lift_db_helper = new LiftDbHelper(getBaseContext());
        final Exercise current_exercise = lift_db_helper.selectExerciseFromExerciseId(getIntent().getLongExtra(ExerciseDetailFragment.exercise_id, 0));
        LayoutInflater li = LayoutInflater.from(this);
        // Re-use the add exercise dialog. It contains the same fields we want to use here.
        View edit_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder edit_exercise_dialog_builder = new AlertDialog.Builder(this);
        edit_exercise_dialog_builder.setTitle(R.string.edit_exercise);
        edit_exercise_dialog_builder.setView(edit_exercise_dialog_view);
        final EditText exercise_name_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_text.setText(current_exercise.getName());
        final EditText exercise_description_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);
        exercise_description_text.setText(current_exercise.getDescription());

        // Handle the positive button press.
        edit_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (exercise_name_text.getText().toString().isEmpty()) {
                    Snackbar.make(findViewById(R.id.edit_exercise_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else {
                    current_exercise.setName(exercise_name_text.getText().toString());
                    current_exercise.setDescription(exercise_description_text.getText().toString());
                    Snackbar.make(findViewById(R.id.edit_exercise_button), "Exercise updated.", Snackbar.LENGTH_LONG).show();

                    ExerciseDetailFragment exercise_detail_fragment = (ExerciseDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
                    exercise_detail_fragment.reload();
                }
            }
        });

        // Handle the negative button press.
        edit_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.edit_exercise_button), "Exercise not updated.", Snackbar.LENGTH_LONG).show();
            }
        });
        AlertDialog edit_exercise_dialog = edit_exercise_dialog_builder.create();
        edit_exercise_dialog.show();
    }
}
