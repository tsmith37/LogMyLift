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

/**
 * A fragment representing a single Workout detail screen.
 * This fragment is either contained in a {@link WorkoutListActivity}
 * in two-pane mode (on tablets) or a {@link WorkoutDetailActivity}
 * on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
    private WorkoutHistoryCardAdapter current_workout_history;
    private RecyclerView current_workout_list;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String workout_parcel = "workout";

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
                new Exercise(lift_db_helper, getString(R.string.bench_press), getString(R.string.bench_press_description));
                new Exercise(lift_db_helper, getString(R.string.squat), getString(R.string.squat_description));
                new Exercise(lift_db_helper, getString(R.string.deadlift), getString(R.string.deadlift_description));
            }

            // Create a card adapter for the workout history.
            current_workout_history = new WorkoutHistoryCardAdapter(this.getActivity(), lift_db_helper, current_workout_list, current_workout);

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
    public void showAddLiftDialog()
    {
        // Create parameters to add a new lift.
        WorkoutHistoryCardAdapter.AddLiftParams add_lift_params = new WorkoutHistoryCardAdapter.AddLiftParams(getContext(), current_workout_list);

        // Show the add lift dialog.
        current_workout_history.showAddLiftDialog(add_lift_params);
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

    /**
     * Change the current exercise to the most recent exercise.
     * Note that this method is really just a pass through to the card adapter.
     * @return  This method always returns 0.
     */
    public long changeCurrentExerciseToMostRecent()
    {
        current_workout_history.changeCurrentExerciseToMostRecent();
        return 0;
    }
}
