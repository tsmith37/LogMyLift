package edu.wvu.tsmith.logmylift;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.dummy.DummyContent;

/**
 * A fragment representing a single Workout detail screen.
 * This fragment is either contained in a {@link WorkoutListActivity}
 * in two-pane mode (on tablets) or a {@link WorkoutDetailActivity}
 * on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
    LiftDbHelper lift_db;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String workout_id = "workout_id";

    // The workout that this fragment should represent.
    public Workout current_workout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WorkoutDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lift_db = new LiftDbHelper(getContext());
        if (getArguments().containsKey(workout_id)) {
            // Load the workout from the database based on the ID bundled with the fragment.
            current_workout = lift_db.selectWorkoutFromWorkoutId(getArguments().getLong(workout_id));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(current_workout.getDescription());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.workout_detail, container, false);

        // Show the workout description in a TextView.
        if (current_workout != null) {
            RecyclerView current_workout_list = (RecyclerView) rootView.findViewById(R.id.current_workout_list);
            RecyclerView.LayoutManager current_workout_layout_manager = new LinearLayoutManager(getContext());
            current_workout_list.setLayoutManager(current_workout_layout_manager);
            ArrayList<Lift> current_workout_lifts = current_workout.getLifts();
            WorkoutHistoryAdapter current_workout_history = new WorkoutHistoryAdapter(current_workout_lifts);
            current_workout_list.setAdapter(current_workout_history);
        }
        return rootView;
    }

    public void reload()
    {
        this.current_workout = lift_db.selectWorkoutFromWorkoutId(getArguments().getLong(workout_id));
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(current_workout.getDescription());
        }
    }
}
