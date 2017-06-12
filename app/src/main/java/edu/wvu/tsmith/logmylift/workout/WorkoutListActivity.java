package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.lift.AddLift;

/**
 * An activity representing a list of Workouts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkoutDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkoutListActivity extends AppCompatActivity {
    private LiftDbHelper lift_db_helper;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        lift_db_helper = new LiftDbHelper(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton add_workout_button = (FloatingActionButton) findViewById(R.id.add_workout_button);
        add_workout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewWorkoutDialog();
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
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(lift_db_helper.selectWorkoutList("")));
    }

    class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ArrayList<Workout> workout_list_values;

        SimpleItemRecyclerViewAdapter(ArrayList<Workout> workouts) {
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

            // On a click, go to the workout details.
            holder.workout_list_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(WorkoutDetailFragment.workout_parcel, holder.workout);
                        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.workout_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WorkoutDetailActivity.class);
                        intent.putExtra(WorkoutDetailFragment.workout_parcel, holder.workout);

                        context.startActivity(intent);
                    }
                }
            });

            // On a long click, allow the user to edit the workout.
            holder.workout_list_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showEditWorkoutDialog(holder.workout);
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return workout_list_values.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View workout_list_view;
            final TextView workout_description_text_view;
            final TextView workout_date_text_view;
            public Workout workout;

            ViewHolder(View view) {
                super(view);
                this.workout_list_view = view;
                this.workout_description_text_view = (TextView) view.findViewById(R.id.workout_description_text_view);
                this.workout_date_text_view = (TextView) view.findViewById(R.id.workout_date_text_view);

            }

            @Override
            public String toString() {
                return super.toString() + " '" + workout_description_text_view.getText() + "'";
            }
        }
    }

    /**
     * Allow the user to start a new workout.
     */
    private void showNewWorkoutDialog()
    {
        LayoutInflater li = LayoutInflater.from(this);
        View add_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder add_workout_dialog_builder = new AlertDialog.Builder(this);
        String new_workout_title = getString(R.string.new_workout) + ": " + new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        add_workout_dialog_builder.setTitle(new_workout_title);
        add_workout_dialog_builder.setView(add_workout_dialog_view);
        final EditText workout_description_text = (EditText) add_workout_dialog_view.findViewById(R.id.workout_description_edit_text);

        // Handle a positive button press.
        add_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Workout new_workout = new Workout(lift_db_helper, workout_description_text.getText().toString());
                Intent workout_intent = new Intent(getBaseContext(), AddLift.class);
                workout_intent.putExtra(LiftDbHelper.WORKOUT_COLUMN_WORKOUT_ID, new_workout.getWorkoutId());
                startActivity(workout_intent);
            }
        });

        // Handle a negative button press.
        add_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_workout_button), "Workout not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_exercise_dialog = add_workout_dialog_builder.create();
        add_exercise_dialog.show();
    }

    /**
     * Allow the user to edit the workout description via a dialog.
     * @param current_workout   The workout to edit.
     */
    private void showEditWorkoutDialog(final Workout current_workout)
    {
        LayoutInflater li = LayoutInflater.from(this);

        // Re-use the add workout dialog here...
        View edit_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(this);
        String new_workout_title = getString(R.string.edit_workout) + ": " + current_workout.getReadableStartDate();
        edit_workout_dialog_builder.setTitle(new_workout_title);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final EditText workout_description_text = (EditText) edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        workout_description_text.setText(current_workout.getDescription());

        // Handle the positive button press.
        edit_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                current_workout.setDescription(lift_db_helper, workout_description_text.getText().toString());
                Snackbar.make(findViewById(R.id.add_workout_button), "Workout updated.", Snackbar.LENGTH_LONG).show();
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.workout_list);
                SimpleItemRecyclerViewAdapter recycler_view_adapter = (SimpleItemRecyclerViewAdapter) recyclerView.getAdapter();
                recycler_view_adapter.notifyDataSetChanged();
            }
        });

        // Handle the negative button press.
        edit_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_workout_button), "Workout not updated.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();
    }
}
