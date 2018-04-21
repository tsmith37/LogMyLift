package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ViewFlipper;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.lift.Lift;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        lift_db_helper = new LiftDbHelper(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                Exercise.showAddExerciseDialog(view.getContext(), findViewById(R.id.add_exercise_button), lift_db_helper, "", new Callable<Long>() {
                    public Long call()
                    {
                        return reloadExerciseList("");
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
            holder.exercise_description_text_view.setText(current_exercise.getDescription());

            // If the exercise has a max effort lift, display it.
            Lift max_effort_lift = lift_db_helper.selectLiftFromLiftId(current_exercise.getMaxLiftId());
            if (max_effort_lift != null)
            {
                holder.max_effort_text_view.setText(getString(R.string.max_effort) + ": " + Integer.toString(max_effort_lift.getWeight()) + " for " + Integer.toString(max_effort_lift.getReps()) + " on " + max_effort_lift.getReadableStartDate());
            }

            // Display the last time the lift was performed if possible.
            Date last_performed_workout_date = lift_db_helper.selectDateFromWorkoutId(current_exercise.getLastWorkoutId());
            if (last_performed_workout_date != null)
            {
                String last_performed_text = getString(R.string.last_performed_on) + " " + date_format.format(last_performed_workout_date);
                holder.last_performed_text_view.setText(last_performed_text);
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
                    showDeleteExerciseDialog(current_exercise, position);
                    holder.view_flipper.setDisplayedChild(0);
                }
            });

            holder.edit_exercise_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditExerciseDialog(current_exercise, position);
                    holder.view_flipper.setDisplayedChild(0);
                }
            });

            holder.exercise_history_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToExerciseHistory(v.getContext(), current_exercise.getExerciseId());
                }
            });

            holder.exercise_info_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPercentMaxCalculatorDialog(current_exercise);
                    holder.view_flipper.setDisplayedChild(0);
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
            final TextView max_effort_text_view;
            final TextView last_performed_text_view;

            final ImageButton delete_exercise_button;
            final ImageButton edit_exercise_button;
            final ImageButton exercise_info_button;
            final ImageButton exercise_history_button;

            ViewHolder(View view) {
                super(view);
                this.exercise_list_view = view;
                this.view_flipper = view.findViewById(R.id.card_view_flipper);

                this.exercise_name_text_view = view.findViewById(R.id.exercise_name_text_view);
                this.exercise_description_text_view = view.findViewById(R.id.exercise_description_text_view);
                this.max_effort_text_view = view.findViewById(R.id.max_effort_text_view);
                this.last_performed_text_view = view.findViewById(R.id.last_performed_text_view);

                this.delete_exercise_button = view.findViewById(R.id.delete_exercise_button);
                this.edit_exercise_button = view.findViewById(R.id.edit_exercise_button);
                this.exercise_info_button = view.findViewById(R.id.exercise_info_button);
                this.exercise_history_button = view.findViewById(R.id.exercise_history_button);
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

    /**
     * Show the dialog to edit an exercise, and act according to the user input.
     * @param current_exercise              The exercise to edit.
     * @param exercise_position_in_adapter  The position of the exercise in the adapter holding the data.
     *                                      This is used to reload the adapter if the exercise is updated.
     */
    private void showEditExerciseDialog(final Exercise current_exercise, final int exercise_position_in_adapter)
    {
        LayoutInflater li = LayoutInflater.from(this);

        // Re-use the add exercise dialog here...
        View edit_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder edit_exercise_dialog_builder = new AlertDialog.Builder(this);
        edit_exercise_dialog_builder.setTitle("Edit Exercise");
        edit_exercise_dialog_builder.setView(edit_exercise_dialog_view);
        final EditText exercise_name_edit_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_edit_text.setText(current_exercise.getName());
        final EditText exercise_description_edit_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);
        exercise_description_edit_text.setText(current_exercise.getDescription());

        // Handle the positive button press.
        edit_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String edited_exercise_name = exercise_name_edit_text.getText().toString();
                if (!edited_exercise_name.isEmpty())
                {
                    current_exercise.setName(lift_db_helper, edited_exercise_name);
                    current_exercise.setDescription(lift_db_helper, exercise_description_edit_text.getText().toString());
                    Snackbar.make(findViewById(R.id.add_exercise_button), R.string.exercise_updated, Snackbar.LENGTH_LONG).show();
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.exercise_list);
                    ExerciseListCardAdapter recycler_view_adapter = (ExerciseListCardAdapter) recyclerView.getAdapter();
                    recycler_view_adapter.notifyItemChanged(exercise_position_in_adapter);
                }
                else
                {
                    Snackbar.make(findViewById(R.id.add_exercise_button), R.string.exercise_name_not_valid, Snackbar.LENGTH_LONG).show();
                }
            }});

        // Handle the negative button press.
        edit_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_exercise_button), R.string.exercise_not_updated, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_exercise_dialog = edit_exercise_dialog_builder.create();
        edit_exercise_dialog.show();
    }

    private void showDeleteExerciseDialog(final Exercise exercise_to_delete, final int exercise_position_in_adapter)
    {
        AlertDialog.Builder delete_exercise_dialog_builder = new AlertDialog.Builder(this);
        delete_exercise_dialog_builder.setTitle("Delete Exercise");
        delete_exercise_dialog_builder.setMessage("Are you sure you want to delete " + exercise_to_delete.getName() + "? This action cannot be undone.");
        delete_exercise_dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exercise_to_delete.delete(lift_db_helper);

                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.exercise_list);
                ExerciseListCardAdapter recycler_view_adapter = (ExerciseListCardAdapter) recyclerView.getAdapter();
                recycler_view_adapter.deleteExercise(exercise_position_in_adapter);
                Snackbar.make(findViewById(R.id.exercise_list), "Exercise deleted.", Snackbar.LENGTH_LONG).show();
            }
        });
        delete_exercise_dialog_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.exercise_list), "Exercise not deleted.", Snackbar.LENGTH_LONG).show();
            }
        });
        delete_exercise_dialog_builder.show();
    }

    private void showPercentMaxCalculatorDialog(final Exercise current_exercise)
    {
        LayoutInflater li = LayoutInflater.from(this);
        View percent_max_calculator_view = li.inflate(R.layout.percent_max_calculator_dialog, null);
        AlertDialog.Builder percent_max_calculator_dialog_builder = new AlertDialog.Builder(this);
        percent_max_calculator_dialog_builder.setTitle("Calculate Percentage of Max - " + current_exercise.getName());
        percent_max_calculator_dialog_builder.setView(percent_max_calculator_view);

        final TextView exercise_description_text_view = percent_max_calculator_view.findViewById(R.id.exercise_description_text_view);
        final NumberPicker percent_max_number_picker = percent_max_calculator_view.findViewById(R.id.percent_number_picker);
        final TextView percent_max_text_view = percent_max_calculator_view.findViewById(R.id.percent_max_text_view);

        final int theoretical_max = lift_db_helper.selectLiftFromLiftId(current_exercise.getMaxLiftId()).calculateMaxEffort();

        percent_max_number_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int percent = newVal * 5;
                Double percentage_of_max = theoretical_max * ((double) (percent / 100.00));
                percent_max_text_view.setText(String.format("%.2f", percentage_of_max));
            }
        });
        percent_max_number_picker.setMaxValue(20);
        percent_max_number_picker.setMinValue(0);
        percent_max_number_picker.setValue(20);
        percent_max_number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        NumberPicker.Formatter max_lift_percentage_formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                int percentage = i * 5;
                return Integer.toString(percentage);
            }
        };
        percent_max_number_picker.setFormatter(max_lift_percentage_formatter);

        AlertDialog percent_max_calculator_dialog = percent_max_calculator_dialog_builder.create();
        percent_max_calculator_dialog.show();
    }

}
