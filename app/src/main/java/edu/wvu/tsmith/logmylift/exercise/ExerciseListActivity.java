package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.exercise_stats.ExerciseStatsDialog;
import edu.wvu.tsmith.logmylift.lift.Lift;

/**
 * An activity representing a list of Exercises. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ExerciseDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ExerciseListActivity extends AppCompatActivity {
    private static final java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private LiftDbHelper lift_db_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        lift_db_helper = new LiftDbHelper(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final FloatingActionButton add_exercise_button = findViewById(R.id.add_exercise_button);
        add_exercise_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddExerciseDialog add_exercise_dialog = new AddExerciseDialog(view.getContext(), lift_db_helper, view, "");
                add_exercise_dialog.show(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        reloadExerciseList("");
                        return null;
                    }
                });
            }
        });

        reloadExerciseList("");

        if (findViewById(R.id.exercise_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            /*
      Whether or not the activity is in two-pane mode, i.e. running on a tablet
      device.
     */
            boolean mTwoPane = true;
        }

        final EditText exercise_filter_edit_text = this.findViewById(R.id.exercise_filter_edit_text);
        if (exercise_filter_edit_text != null)
        {
            exercise_filter_edit_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    reloadExerciseList(exercise_filter_edit_text.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        ImageButton clear_exercise_filter = this.findViewById(R.id.clear_exercise_filter);
        if (clear_exercise_filter != null) {
            clear_exercise_filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exercise_filter_edit_text != null)
                    {
                        exercise_filter_edit_text.setText("");
                    }
                }
            });
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

    class ExerciseListCardAdapter extends RecyclerView.Adapter<ExerciseListCardAdapter.ViewHolder>
    {
        private final ArrayList<Exercise> exercise_list_values;

        ExerciseListCardAdapter(ArrayList<Exercise> exercises)
        {
            exercise_list_values = exercises;
        }

        @Override
        public ExerciseListCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.exercise_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ExerciseListCardAdapter.ViewHolder holder, final int position) {
            // Display the exercise.
            final Exercise current_exercise = exercise_list_values.get(position);
            holder.exercise_name_text_view.setText(current_exercise.getName());

            String description = current_exercise.getDescription();
            if (description.equals(""))
            {
                holder.exercise_description_text_view.setVisibility(View.GONE);
            }
            else
            {
                holder.exercise_description_text_view.setText(current_exercise.getDescription());
                holder.exercise_description_text_view.setVisibility(View.VISIBLE);
            }

            // If the exercise has a training weight, display it.
            int training_weight = current_exercise.getTrainingWeight(lift_db_helper);
            if (training_weight > 0)
            {
                holder.training_weight_text_view.setText("Training Weight" + ": " + Integer.toString(training_weight));
                holder.training_weight_text_view.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.training_weight_text_view.setVisibility(View.GONE);
            }

            // If the exercise has a max effort lift, display it.
            Lift max_effort_lift = lift_db_helper.selectMaxEffortLiftByExercise(current_exercise.getExerciseId());
            if (max_effort_lift != null)
            {
                holder.max_effort_text_view.setText(getString(R.string.max_effort) + ": " + Integer.toString(max_effort_lift.getWeight()) + " for " + Integer.toString(max_effort_lift.getReps()) + " on " + max_effort_lift.getReadableStartDate());
                holder.max_effort_text_view.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.max_effort_text_view.setVisibility(View.GONE);
            }

            // If the exercise has a max weight lift, display it.
            Lift max_weight_lift = lift_db_helper.selectHeaviestLiftByExercise(current_exercise.getExerciseId());
            if (max_weight_lift != null)
            {
                holder.max_weight_text_view.setText(getString(R.string.heaviest_lift) + " " + Integer.toString(max_weight_lift.getWeight()) + " for " + Integer.toString(max_weight_lift.getReps()) + " on " + max_weight_lift.getReadableStartDate());
                holder.max_weight_text_view.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.max_weight_text_view.setVisibility(View.GONE);
            }

            // Display the last time the lift was performed if possible.
            Date last_performed_workout_date = lift_db_helper.selectDateFromWorkoutId(current_exercise.getLastWorkoutId());
            if (last_performed_workout_date != null)
            {
                String last_performed_text = getString(R.string.last_performed_on) + " " + date_format.format(last_performed_workout_date);
                holder.last_performed_text_view.setText(last_performed_text);
                holder.last_performed_text_view.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.last_performed_text_view.setVisibility(View.GONE);
            }

            holder.exercise_list_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (holder.view_flipper.getDisplayedChild() == 1 && !hasFocus)
                    {
                        holder.view_flipper.setDisplayedChild(0);
                    }
                }
            });
            holder.exercise_list_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.view_flipper.setDisplayedChild(1);
                    return false;
                }
            });

            holder.delete_exercise_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecyclerView recycler_view = findViewById(R.id.exercise_list);
                    DeleteExerciseDialog delete_exercise_dialog = new DeleteExerciseDialog(v.getContext(), recycler_view, lift_db_helper, v, position, current_exercise);
                    delete_exercise_dialog.show();
                    holder.view_flipper.setDisplayedChild(0);
                }
            });

            holder.edit_exercise_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditExerciseDialog edit_exercise_dialog = new EditExerciseDialog(v.getContext(), v, current_exercise);
                    edit_exercise_dialog.show(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            RecyclerView recyclerView = findViewById(R.id.exercise_list);
                            ExerciseListCardAdapter recycler_view_adapter = (ExerciseListCardAdapter) recyclerView.getAdapter();
                            recycler_view_adapter.notifyItemChanged(position);
                            return null;
                        }
                    });
                    holder.view_flipper.setDisplayedChild(0);
                }
            });

            holder.exercise_history_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToExerciseHistory(v.getContext(), current_exercise.getExerciseId());
                }
            });

            holder.exercise_stats_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExerciseStatsDialog exercise_stats_dialog = new ExerciseStatsDialog(v.getContext(), lift_db_helper, current_exercise.getExerciseId());
                    exercise_stats_dialog.show();
                }
            });
        }

        public void deleteExercise(int exercise_position_in_adapter)
        {
            exercise_list_values.remove(exercise_position_in_adapter);
            notifyItemRemoved(exercise_position_in_adapter);
        }

        @Override
        public int getItemCount()
        {
            if (exercise_list_values != null)
            {
                return exercise_list_values.size();
            }
            else
            {
                return 0;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View exercise_list_view;
            final ViewFlipper view_flipper;

            final TextView exercise_name_text_view;
            final TextView exercise_description_text_view;
            final TextView training_weight_text_view;
            final TextView max_effort_text_view;
            final TextView max_weight_text_view;
            final TextView last_performed_text_view;

            final ImageButton delete_exercise_button;
            final ImageButton edit_exercise_button;
            final ImageButton exercise_history_button;
            final ImageButton exercise_stats_button;

            ViewHolder(View view) {
                super(view);
                this.exercise_list_view = view;
                this.view_flipper = view.findViewById(R.id.card_view_flipper);

                this.exercise_name_text_view = view.findViewById(R.id.exercise_name_text_view);
                this.exercise_description_text_view = view.findViewById(R.id.exercise_description_text_view);
                this.training_weight_text_view = view.findViewById(R.id.training_weight_text_view);
                this.max_effort_text_view = view.findViewById(R.id.max_effort_text_view);
                this.max_weight_text_view = view.findViewById(R.id.max_weight_text_view);
                this.last_performed_text_view = view.findViewById(R.id.last_performed_text_view);

                this.delete_exercise_button = view.findViewById(R.id.delete_exercise_button);
                this.edit_exercise_button = view.findViewById(R.id.edit_exercise_button);
                this.exercise_history_button = view.findViewById(R.id.exercise_history_button);
                this.exercise_stats_button = view.findViewById(R.id.exercise_stats_button);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + exercise_name_text_view.getText() + "'";
            }

        }
    }

    private long reloadExerciseList(String filter)
    {
        View view = findViewById(R.id.exercise_list);
        assert view != null;
        RecyclerView recycler_view = (RecyclerView) view;
        recycler_view.setAdapter(new ExerciseListCardAdapter(lift_db_helper.selectExerciseList(filter)));
        return 1;
    }

    private void goToExerciseHistory(Context context, long exercise_id)
    {
        Intent intent = new Intent(context, ExerciseDetailActivity.class);
        intent.putExtra(ExerciseDetailFragment.exercise_id, exercise_id);

        context.startActivity(intent);
    }
}
