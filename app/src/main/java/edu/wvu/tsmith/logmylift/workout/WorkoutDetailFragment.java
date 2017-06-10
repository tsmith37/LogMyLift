package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * A fragment representing a single Workout detail screen.
 * This fragment is either contained in a {@link WorkoutListActivity}
 * in two-pane mode (on tablets) or a {@link WorkoutDetailActivity}
 * on handsets.
 */
public class WorkoutDetailFragment extends Fragment {
    LiftDbHelper lift_db;
    View root_view;
    WorkoutHistoryCardAdapter current_workout_history;

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
            reloadWorkoutDetails();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.workout_detail, container, false);
        this.root_view = rootView;

        // Show the workout description in a TextView.
        if (current_workout != null) {
            RecyclerView current_workout_list = (RecyclerView) rootView.findViewById(R.id.current_workout_list);
            RecyclerView.LayoutManager current_workout_layout_manager = new LinearLayoutManager(getContext());
            current_workout_list.setLayoutManager(current_workout_layout_manager);
            current_workout_history = new WorkoutHistoryCardAdapter(this.getActivity(), current_workout);
            current_workout_list.setAdapter(current_workout_history);
            initializeSwipe(current_workout_list);
        }

        return rootView;
    }

    /**
     * Reload the workout details.
     */
    public void reloadWorkoutDetails()
    {
        this.current_workout = lift_db.selectWorkoutFromWorkoutId(getArguments().getLong(workout_id));
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(current_workout.getDescription());
        }
    }

    public void showAddLiftDialog()
    {
        current_workout_history.showAddLiftDialog(getContext());
    }

    private void initializeSwipe(RecyclerView recycler_view) {
        ItemTouchHelper.SimpleCallback simple_callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                current_workout_history.deleteLift(viewHolder.getAdapterPosition());
            }
        };

        ItemTouchHelper item_touch_helper = new ItemTouchHelper(simple_callback);
        item_touch_helper.attachToRecyclerView(recycler_view);
    }

    /*public void reloadWorkoutList()
    {
        if (root_view != null) {
            RecyclerView current_workout_list = (RecyclerView) root_view.findViewById(R.id.current_workout_list);
            ArrayList<Lift> current_workout_lifts = current_workout.getLifts();
            final WorkoutHistoryCardAdapter current_workout_history = new WorkoutHistoryCardAdapter(this.getActivity(), current_workout_lifts);
            if (current_workout_list != null) {
                current_workout_list.setAdapter(current_workout_history);
            }
        }
    }*/
}
