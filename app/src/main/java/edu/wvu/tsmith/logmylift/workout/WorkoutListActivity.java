package edu.wvu.tsmith.logmylift.workout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * An activity representing a list of Workouts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkoutDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkoutListActivity extends AppCompatActivity
{
    private LiftDbHelper lift_db_helper;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private LoadWorkoutHistoryListParams workout_list_params;

    private RecyclerView recycler_view;

    public static void start(Context context)
    {
        Intent starter = new Intent(context, WorkoutListActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        lift_db_helper = new LiftDbHelper(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Set up the new workout button.
        FloatingActionButton add_workout_button = findViewById(R.id.add_workout_button);
        add_workout_button.setOnClickListener(view -> {
            NewWorkoutDialog new_workout_dialog = new NewWorkoutDialog(view.getContext(), lift_db_helper);
            new_workout_dialog.show();
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        workout_list_params = new LoadWorkoutHistoryListParams();

        // Set up the recycler view containing the workout history list. Initially the workout history
        // list will have the complete set of workouts.
        this.recycler_view = findViewById(R.id.workout_list);
        assert recycler_view != null;
        reloadWorkoutHistoryList(workout_list_params);

        // Set up the to and from calendars. These are used to limit the workout history by date.
        final Calendar from_cal = Calendar.getInstance();
        final Calendar to_cal = Calendar.getInstance();

        // Set up the workout list filter. As the user types into the filter, the workout history will
        // be limited by the names of the workouts.
        final EditText workout_list_filter_edit_text = findViewById(R.id.workout_filter_edit_text);
        if (workout_list_filter_edit_text != null)
        {
            workout_list_filter_edit_text.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                    workout_list_params.filterName(workout_list_filter_edit_text.getText().toString());
                    reloadWorkoutHistoryList(workout_list_params);
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }

        // Set up the clear text filter button.
        ImageButton clear_workout_filter = this.findViewById(R.id.clear_workout_filter);
        if (clear_workout_filter != null)
        {
            clear_workout_filter.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    workout_list_filter_edit_text.setText("");
                    workout_list_params.clearNameFilter();
                    reloadWorkoutHistoryList(workout_list_params);
                }
            });
        }

        // Set up the from date button. This will allow the the user to set the beginning of the workout
        // date filter range.
        final TextView from_text = findViewById(R.id.from_date_text);
        ImageButton from_date_button = this.findViewById(R.id.from_date_button);
        if (from_date_button != null)
        {
            from_date_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DatePickerDialog.OnDateSetListener date_set_listener = new DatePickerDialog.OnDateSetListener()
                    {
                        @Override
                        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay)
                        {
                            from_cal.set(selectedYear, selectedMonth, selectedDay);
                            Date from_date = from_cal.getTime();
                            String from_date_string = new java.text.SimpleDateFormat("MM/dd/yy", Locale.US).format(from_date);
                            from_text.setText("From " + from_date_string);
                        }
                    };

                    final DatePickerDialog date_picker_dialog = new DatePickerDialog(
                            WorkoutListActivity.this,
                            date_set_listener,
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    date_picker_dialog.show();
                }
            });
        }

        // Set up the to date button. This will allow the the user to set the end of the workout
        // date filter range.
        final TextView to_text = findViewById(R.id.to_date_text);
        ImageButton to_date_button = this.findViewById(R.id.to_date_button);
        if (to_date_button != null)
        {
            to_date_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DatePickerDialog.OnDateSetListener date_set_listener = new DatePickerDialog.OnDateSetListener()
                    {
                        @Override
                        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay)
                        {
                            to_cal.set(selectedYear, selectedMonth, selectedDay);
                            Date to_date = to_cal.getTime();
                            String to_date_string = new java.text.SimpleDateFormat("MM/dd/yy", Locale.US).format(to_date);
                            to_text.setText("To " + to_date_string);
                        }
                    };

                    final DatePickerDialog date_picker_dialog = new DatePickerDialog(
                            WorkoutListActivity.this,
                            date_set_listener,
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    date_picker_dialog.show();
                }
            });
        }

        // Filter the workout history by date. This will only work if the to and from dates are set.
        final Button date_go_button = this.findViewById(R.id.select_date_button);
        if (date_go_button != null)
        {
            date_go_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (from_cal.isSet(Calendar.DAY_OF_MONTH) && to_cal.isSet(Calendar.DAY_OF_MONTH))
                    {
                        Date from_date = from_cal.getTime();
                        Date to_date = to_cal.getTime();

                        workout_list_params.filterDate(from_date, to_date, date_go_button);
                        reloadWorkoutHistoryList(workout_list_params);
                    }
                }
            });
        }

        // Clear the workout history date filter.
        Button date_clear_button = this.findViewById(R.id.clear_date_button);
        if (date_clear_button != null)
        {
            date_clear_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    // Reset the to and from date text fields.
                    from_text.setText("From...");
                    to_text.setText("To...");

                    // Clear the to and from date calendars.
                    from_cal.clear();
                    to_cal.clear();

                    workout_list_params.clearDateFilter();
                    reloadWorkoutHistoryList(workout_list_params);
                }
            });
        }

        if (findViewById(R.id.workout_detail_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.workout_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            this.finish();
            return true;
        }
        else if (id == R.id.change_sort_order)
        {
            final SortWorkoutListDialog sort_workout_list_dialog = new SortWorkoutListDialog(this, workout_list_params);
            sort_workout_list_dialog.show(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    workout_list_params = sort_workout_list_dialog.getParams();
                    reloadWorkoutHistoryList(workout_list_params);
                    return null;
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Reloads the workout history list.
     * @param params    The parameters used to select the workout history list.
     */
    private void reloadWorkoutHistoryList(LoadWorkoutHistoryListParams params)
    {
        // Create the recycler view adapter used to populate the workout history list.
        WorkoutHistoryRecyclerViewAdapter workout_history_adapter =
                new WorkoutHistoryRecyclerViewAdapter(lift_db_helper.selectWorkoutList(params));

        // Set the adapter for the recycler view showing the workout history list.
        this.recycler_view.setAdapter(workout_history_adapter);
    }

    /**
     * A recycler view adapter for the workout history list.
     * @author Tommy Smith
     */
    class WorkoutHistoryRecyclerViewAdapter
            extends RecyclerView.Adapter<WorkoutHistoryRecyclerViewAdapter.WorkoutHistoryViewHolder>
    {
        // THe list of workouts.
        private final ArrayList<Workout> workout_list;

        /**
         * Constructs the workout history recycler view adapter.
         * @param workouts  A list of workouts.
         */
        WorkoutHistoryRecyclerViewAdapter(ArrayList<Workout> workouts)
        {
            workout_list = workouts;
        }

        /**
         * Creates the workout history view holder.
         * @param parent    The calling parent view.
         * @param viewType  The view type.
         * @return  The workout history view holder.
         */
        @Override
        public WorkoutHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.workout_list_content, parent, false);
            return new WorkoutHistoryViewHolder(view);
        }

        /**
         * Binds one member of the adapter to a view holder.
         * @param holder    The workout history view holder.
         * @param position  The position in the adapter to bind to the view holder.
         */
        @Override
        public void onBindViewHolder(final WorkoutHistoryViewHolder holder, int position)
        {
            // Set the workout, description, and date of the view holder.
            holder.workout = workout_list.get(position);
            holder.workout_description_text_view.setText(workout_list.get(position).getDescription());
            holder.workout_date_text_view.setText(workout_list.get(position).getReadableStartDate());

            // On a click, go to the workout details.
            holder.workout_list_view.setOnClickListener(new View.OnClickListener()
            {
                /**
                 * Handles the click of the view holder.
                 * @param v The calling view.
                 */
                @Override
                public void onClick(View v)
                {
                    Workout workoutToOpen = holder.workout;
                    workoutToOpen.initExistingLifts();

                    if (mTwoPane)
                    {
                        // Opens a workout detail fragment with the workout.
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(WorkoutDetailFragment.workout_parcel, workoutToOpen);
                        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.workout_detail_container, fragment)
                                .commit();
                    }
                    else
                    {
                        // Open a workout detail activity with the workout.
                        WorkoutDetailActivity.start(v.getContext(), workoutToOpen);
                    }
                }
            });

            // On a long click, allow the user to edit the workout description.
            holder.workout_list_view.setOnLongClickListener(new View.OnLongClickListener()
            {
                /**
                 * Handle the long click of the view holder.
                 * @param v The calling view.
                 * @return  This method always returns false.
                 */
                @Override
                public boolean onLongClick(final View v)
                {
                    final EditWorkoutDialog editWorkoutDialog = new EditWorkoutDialog(v.getContext(), holder.workout);
                    editWorkoutDialog.show(() -> {
                        // Set the description of the workout.
                        holder.workout.setDescription(editWorkoutDialog.workoutDescriptionAfterEditing);

                        // Notify the user that the workout was updated.
                        Snackbar.make(v, "Workout updated.", Snackbar.LENGTH_LONG).show();
                        RecyclerView recyclerView = findViewById(R.id.workout_list);

                        // Notify the recycler view adapter that a member has changed. This forces the recycler
                        // view to redraw the description of the workout.
                        WorkoutHistoryRecyclerViewAdapter recycler_view_adapter = (WorkoutHistoryRecyclerViewAdapter) recyclerView.getAdapter();
                        recycler_view_adapter.notifyDataSetChanged();
                        return null;
                    });
                    return false;
                }
            });
        }

        /**
         * Returns the number of workouts in the list.
         * @return  The number of workouts in the list.
         */
        @Override
        public int getItemCount() {
            return workout_list.size();
        }

        /**
         * A view holder for a workout. The view holder contains the description and date of the workout.
         */
        class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder
        {
            // The list view containing all workouts in the history.
            final View workout_list_view;

            // The text views for the workout.
            final TextView workout_description_text_view;
            final TextView workout_date_text_view;

            // The workout in the view holder.
            public Workout workout;

            /**
             * Constructs the view holder.
             * @param view  - The list view constructing the holder.
             */
            WorkoutHistoryViewHolder(View view)
            {
                super(view);
                this.workout_list_view = view;
                this.workout_description_text_view = view.findViewById(R.id.workout_description_text_view);
                this.workout_date_text_view = view.findViewById(R.id.workout_date_text_view);
            }

            /**
             * Converts the view holder to a string.
             * @return  A string representing the view holder.
             */
            @Override
            public String toString()
            {
                return super.toString() + " '" + workout_description_text_view.getText() + "'";
            }
        }
    }
}
