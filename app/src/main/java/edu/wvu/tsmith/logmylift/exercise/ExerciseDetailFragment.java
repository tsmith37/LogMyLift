    package edu.wvu.tsmith.logmylift.exercise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

    /**
 * A fragment representing a single Exercise detail screen.
 * This fragment is either contained in a {@link ExerciseListActivity}
 * in two-pane mode (on tablets) or a {@link ExerciseDetailActivity}
 * on handsets.
 */
public class ExerciseDetailFragment extends Fragment {
    private ExerciseHistoryCardAdapter current_exercise_history;
    private LiftDbHelper lift_db_helper;
        /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String exercise_id = "exercise_id";

    // The exercise that this fragment should represent.
    public Exercise current_exercise;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ExerciseDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lift_db_helper = new LiftDbHelper(getContext());
        if (getArguments().containsKey(exercise_id)) {
            // Load the exercise from the database based on the ID bundled with the fragment.
            current_exercise = lift_db_helper.selectExerciseFromExerciseId(getArguments().getLong(exercise_id));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.exercise_detail, container, false);

        // Show the exercise description a TextView.
        if (current_exercise != null)
        {
            current_exercise_history = new ExerciseHistoryCardAdapter(this.getActivity(), lift_db_helper, current_exercise);
            current_exercise_history.reloadExerciseDetails();

            // Show the exercise history in a list.
            RecyclerView exercise_history_list = rootView.findViewById(R.id.exercise_history_list);
            RecyclerView.LayoutManager exercise_history_layout_manager = new LinearLayoutManager(getContext());
            exercise_history_list.setLayoutManager(exercise_history_layout_manager);
            exercise_history_list.setAdapter(current_exercise_history);
        }
        return rootView;
    }

    public void reloadExerciseDescription()
    {
        current_exercise_history.reloadExerciseDetails();
    }

    public void reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder order)
    {
        current_exercise_history.reloadExerciseHistory(order);
    }
}
