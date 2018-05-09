package edu.wvu.tsmith.logmylift.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.R;

/**
 * An activity representing a single Exercise detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ExerciseListActivity}.
 */
public class ExerciseDetailActivity extends AppCompatActivity
{
    ExerciseDetailFragment exercise_detail_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exercise_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        exercise_detail_fragment = new ExerciseDetailFragment();

        FloatingActionButton edit_exercise_button = findViewById(R.id.edit_exercise_button);
        edit_exercise_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditExerciseDialog edit_exercise_dialog = new EditExerciseDialog(view.getContext(), view, exercise_detail_fragment.current_exercise);
                edit_exercise_dialog.show(new Callable<Integer>()
                {
                    @Override
                    public Integer call() throws Exception
                    {
                        exercise_detail_fragment.setExerciseName(exercise_detail_fragment.current_exercise.getName());
                        exercise_detail_fragment.setExerciseDescription(exercise_detail_fragment.current_exercise.getDescription());
                        return null;
                    }
                });
            }
        });

        final FloatingActionButton sort_exercise_history_button = findViewById(R.id.sort_exercise_history_button);
        sort_exercise_history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SortExerciseHistoryDialog sort_exercise_history_dialog = new SortExerciseHistoryDialog(ExerciseDetailActivity.this, exercise_detail_fragment);
                sort_exercise_history_dialog.show();
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
            exercise_detail_fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.exercise_detail_container, exercise_detail_fragment, "detail_fragment")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
}
