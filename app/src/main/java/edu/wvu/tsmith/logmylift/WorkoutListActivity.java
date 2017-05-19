package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import edu.wvu.tsmith.logmylift.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Workouts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkoutDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkoutListActivity extends AppCompatActivity {
    LiftDbHelper lift_db;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lift_db = new LiftDbHelper(getApplicationContext());
        setContentView(R.layout.activity_workout_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton add_workout_button = (FloatingActionButton) findViewById(R.id.add_workout_button);
        add_workout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View recyclerView = findViewById(R.id.workout_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.workout_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(lift_db.selectWorkoutList("")));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Workout> workout_list_values;

        public SimpleItemRecyclerViewAdapter(List<Workout> workouts) {
            workout_list_values = workouts;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.workout_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.workout = workout_list_values.get(position);
            holder.workout_description_text_view.setText(workout_list_values.get(position).getDescription());
            holder.workout_date_text_view.setText(workout_list_values.get(position).getReadableStartDate());

            holder.workout_list_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putLong(WorkoutDetailFragment.workout_id, holder.workout.getWorkoutId());
                        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.workout_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WorkoutDetailActivity.class);
                        intent.putExtra(WorkoutDetailFragment.workout_id, holder.workout.getWorkoutId());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return workout_list_values.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View workout_list_view;
            public final TextView workout_description_text_view;
            public final TextView workout_date_text_view;
            public Workout workout;

            public ViewHolder(View view) {
                super(view);
                workout_list_view = view;
                workout_description_text_view = (TextView) view.findViewById(R.id.workout_description_text_view);
                workout_date_text_view = (TextView) view.findViewById(R.id.workout_date_text_view);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + workout_description_text_view.getText() + "'";
            }
        }
    }
}
