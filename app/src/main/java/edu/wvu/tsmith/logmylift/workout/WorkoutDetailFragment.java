package edu.wvu.tsmith.logmylift.workout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.AddLiftDialog;
import edu.wvu.tsmith.logmylift.lift.AddLiftParams;

/**
 * A fragment representing a single Workout detail screen.
 * This fragment is either contained in a {@link WorkoutListActivity}
 * in two-pane mode (on tablets) or a {@link WorkoutDetailActivity}
 * on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
    private WorkoutHistoryCardAdapter current_workout_history;
    private RecyclerView current_workout_list;
    private boolean enable_edit;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String workout_parcel = "workout";
    public static final String enable_edit_key = "enable_edit";

    // The workout that this fragment should represent.
    public Workout current_workout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WorkoutDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(workout_parcel))
        {
            // Load the workout from the database based on the ID bundled with the fragment.
            current_workout = getArguments().getParcelable(workout_parcel);
        }

        if (getArguments().containsKey(enable_edit_key))
        {
            enable_edit = getArguments().getBoolean(enable_edit_key);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.workout_detail, container, false);

        // Show the workout description in a TextView.
        if (current_workout != null)
        {
            this.current_workout_list = rootView.findViewById(R.id.current_workout_list);
            RecyclerView.LayoutManager current_workout_layout_manager = new LinearLayoutManager(getContext());
            current_workout_list.setLayoutManager(current_workout_layout_manager);
            LiftDbHelper lift_db_helper = new LiftDbHelper(getContext());

            // If no exercises are available, add three default ones.
            if (lift_db_helper.selectExerciseCount() == 0)
            {
                Exercise bench_press = new Exercise.Builder()
                        .name(getString(R.string.bench_press))
                        .description(getString(R.string.bench_press_description))
                        .toCreate(true)
                        .liftDbHelper(lift_db_helper)
                        .build();
                Exercise squat = new Exercise.Builder()
                        .name(getString(R.string.squat))
                        .description(getString(R.string.squat_description))
                        .toCreate(true)
                        .liftDbHelper(lift_db_helper)
                        .build();
                Exercise deadlift = new Exercise.Builder()
                        .name(getString(R.string.deadlift))
                        .description(getString(R.string.deadlift_description))
                        .toCreate(true)
                        .liftDbHelper(lift_db_helper)
                        .build();
            }

            // Create a card adapter for the workout history.
            current_workout_history = new WorkoutHistoryCardAdapter(
                    this.getActivity(),
                    lift_db_helper,
                    current_workout_list,
                    current_workout,
                    enable_edit);

            // Load the description of the workout.
            current_workout_history.reloadWorkoutDescription();

            // Setup the list of lifts in the workout with the history adapter.
            current_workout_list.setAdapter(current_workout_history);
        }

        return rootView;
    }

    /**
     * Show the add lift dialog.
     * Note that this method is really just a pass through to the card adapter.
     */
    public void showAddLiftDialog(LiftDbHelper lift_db_helper, AddLiftParams params)
    {
        AddLiftDialog add_lift_dialog = new AddLiftDialog(
                getContext(),
                current_workout_list,
                lift_db_helper,
                current_workout,
                current_workout_history.getLifts(),
                params);
        add_lift_dialog.show();
    }

    /**
     * Set the description of the workout.
     * Note that this method is really just a pass through to the card adapter.
     * @param description   The workout's description.
     */
    public void setWorkoutDescription(String description)
    {
        current_workout_history.setWorkoutDescription(description);
    }
}
