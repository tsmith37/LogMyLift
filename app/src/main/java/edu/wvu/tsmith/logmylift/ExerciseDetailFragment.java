    package edu.wvu.tsmith.logmylift;

import android.app.Activity;
import android.database.Cursor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.dummy.DummyContent;

/**
 * A fragment representing a single Exercise detail screen.
 * This fragment is either contained in a {@link ExerciseListActivity}
 * in two-pane mode (on tablets) or a {@link ExerciseDetailActivity}
 * on handsets.
 */
public class ExerciseDetailFragment extends Fragment {
    LiftDbHelper lift_db;

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

        lift_db = new LiftDbHelper(getContext());
        if (getArguments().containsKey(exercise_id)) {
            // Load the exercise from the database based on the ID bundled with the fragment.
            current_exercise = lift_db.selectExerciseFromExerciseId(getArguments().getLong(exercise_id));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(current_exercise.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.exercise_detail, container, false);

        // Show the exercise description a TextView.
        if (current_exercise != null) {
            ((TextView) rootView.findViewById(R.id.exercise_detail)).setText(current_exercise.getDescription());
            TextView max_lift_text_view = (TextView) rootView.findViewById(R.id.max_lift_text);
            Lift max_lift = lift_db.selectLiftFromLiftId(current_exercise.getMaxLiftId());
            if (max_lift != null)
            {
                String max_lift_description = "Max effort: " + Integer.toString(max_lift.getWeight()) + " for " + Integer.toString(max_lift.getReps()) + " on " + max_lift.getReadableStartDate() + ". " + max_lift.getComment();
                max_lift_text_view.setText(max_lift_description);
            }

            // Show the exercise history in a list.
            ListView exercise_history_list = (ListView) rootView.findViewById(R.id.exercise_history_list);
            Cursor exercise_history_cursor = lift_db.selectExerciseHistoryCursor(current_exercise, 10);
            final ExerciseHistoryCursorAdapter exercise_history_adapter = new ExerciseHistoryCursorAdapter(
                    getContext(),
                    exercise_history_cursor);
            if (exercise_history_list != null)
            {
                exercise_history_list.setAdapter(exercise_history_adapter);
            }
        }
        return rootView;
    }

    public void reload()
    {
        this.current_exercise = lift_db.selectExerciseFromExerciseId(getArguments().getLong(exercise_id));
        View current_view = this.getView();
        TextView exercise_detail_text_view = (TextView) current_view.findViewById(R.id.exercise_detail);
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(current_exercise.getName());
        }
        exercise_detail_text_view.setText(current_exercise.getDescription());
    }
}
