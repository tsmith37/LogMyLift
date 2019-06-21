package edu.wvu.tsmith.logmylift.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import edu.wvu.tsmith.logmylift.exercise.exercise_stats.ExerciseStatsDialog;

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
        View parent_view = findViewById(R.id.exercise_detail_container);

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
        else if (id == R.id.exercise_history_sort_menu_item)
        {
            SortExerciseHistoryDialog sort_exercise_history_dialog = new SortExerciseHistoryDialog(ExerciseDetailActivity.this, exercise_detail_fragment);
            sort_exercise_history_dialog.show();
        }
        else if (id == R.id.edit_exercise_menu_item)
        {
            findViewById(R.id.edit_exercise_menu_item);
            EditExerciseDialog edit_exercise_dialog = new EditExerciseDialog(parent_view.getContext(), parent_view, exercise_detail_fragment.current_exercise);
            edit_exercise_dialog.show(new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    exercise_detail_fragment.reloadExerciseDescription();
                    return null;
                }
            });
        }
        else if (id == R.id.exercise_description_menu_item)
        {
            AlertDialog alert_dialog = new AlertDialog.Builder(ExerciseDetailActivity.this).create();
            alert_dialog.setTitle(R.string.exercise_description);
            alert_dialog.setMessage(exercise_detail_fragment.current_exercise.getDescription());
            alert_dialog.show();
        }
        else if (id == R.id.percent_max_menu_item)
        {
            PercentMaxCalculatorDialog max_calculator_dialog = new PercentMaxCalculatorDialog(parent_view.getContext(), exercise_detail_fragment.current_exercise);
            max_calculator_dialog.show();
        }
        else if (id == R.id.exercise_stats_menu_item)
        {
            ExerciseStatsDialog exercise_stats_dialog = new ExerciseStatsDialog(parent_view.getContext(), new LiftDbHelper(parent_view.getContext()), exercise_detail_fragment.current_exercise.getExerciseId());
            exercise_stats_dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.exercise_detail_menu, menu);
        return true;
    }
}
