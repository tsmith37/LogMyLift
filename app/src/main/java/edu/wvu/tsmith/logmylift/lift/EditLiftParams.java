package edu.wvu.tsmith.logmylift.lift;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Tommy Smith on 4/29/2018.
 * Parameters to edit a lift.
 */

public class EditLiftParams
{
    // The lift to edit.
    Lift lift;

    // The parameters of the lift after editing.
    int weight;
    int reps;
    String comment;

    // The position of the lift in the workout history card adapter.
    RecyclerView recycler_view;
    int lift_position_in_adapter;

    // Whether or not to allow the edit to be undone.
    boolean allow_undo;

    View snackbar_parent_view;

    /**
     * Construct the parameters to edit a lift.
     * @param lift                      The lift to edit.
     * @param weight                    The new weight of the lift.
     * @param reps                      The new reps of the lift.
     * @param comment                   The new comment of the lift.
     * @param recycler_view             The recycler view.
     * @param lift_position_in_adapter  The position of the lift in the card adapter.
     * @param allow_undo                Whether or not to allow the edit to be undone.
     * @param snackbar_parent_view      The parent view to show a snackbar.
     */
    public EditLiftParams(
            Lift lift,
            int weight,
            int reps,
            String comment,
            RecyclerView recycler_view,
            int lift_position_in_adapter,
            boolean allow_undo,
            View snackbar_parent_view)
    {
        this.lift = lift;
        this.weight = weight;
        this.reps = reps;
        this.comment = comment;
        this.recycler_view = recycler_view;
        this.lift_position_in_adapter = lift_position_in_adapter;
        this.allow_undo = allow_undo;
        this.snackbar_parent_view = snackbar_parent_view;
    }
}
