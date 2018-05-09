package edu.wvu.tsmith.logmylift.workout;

import android.view.View;

/**
 * Created by Tommy Smith on 5/7/2018.
 * A class used to undo a workout update.
 */

public class UndoEditWorkoutListener implements  View.OnClickListener
{
    private String old_description;
    private WorkoutDetailFragment workout_detail_fragment;

    /**
     * Constructor.
     * @param old_description           The description of the workout pre-update.
     * @param workout_detail_fragment   The fragment containing the workout.
     */
    public UndoEditWorkoutListener(String old_description, WorkoutDetailFragment workout_detail_fragment)
    {
        super();
        this.old_description = old_description;
        this.workout_detail_fragment = workout_detail_fragment;
    }

    @Override
    public void onClick(View v)
    {
        this.workout_detail_fragment.setWorkoutDescription(old_description);
    }
}
