package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import edu.wvu.tsmith.logmylift.LiftDbHelper;

/**
 * Created by Tommy Smith on 5/7/2018.
 * Dialog to delete an exercise. Used to show a confirmation to the user before proceeding.
 */

public class DeleteExerciseDialog
{
    private Context context;
    private RecyclerView recycler_view;
    private LiftDbHelper lift_db_helper;
    private View snackbar_parent_view;
    private int exercise_position_in_adapter;
    private Exercise exercise;

    /**
     * Constructor for the dialog.
     * @param context                           The context in which the dialog will be shown.
     * @param recycler_view                     The recycler view from which the dialog is called.
     * @param lift_db_helper                    The database helper.
     * @param snackbar_parent_view              The parent view for any displayed snackbars.
     * @param exercise_position_in_adapter      The position of the exercise being deleted in the exercise card adapter.
     * @param exercise                          The exercise to delete.
     */
    public DeleteExerciseDialog(
            Context context,
            RecyclerView recycler_view,
            LiftDbHelper lift_db_helper,
            View snackbar_parent_view,
            int exercise_position_in_adapter,
            Exercise exercise)
    {
        this.context = context;
        this.recycler_view = recycler_view;
        this.lift_db_helper = lift_db_helper;
        this.snackbar_parent_view = snackbar_parent_view;
        this.exercise_position_in_adapter = exercise_position_in_adapter;
        this.exercise = exercise;
    }

    public void show()
    {
        AlertDialog.Builder delete_exercise_dialog_builder = new AlertDialog.Builder(this.context);
        delete_exercise_dialog_builder.setTitle("Delete Exercise");
        delete_exercise_dialog_builder.setMessage("Are you sure you want to delete " + this.exercise.getName() + "? This action cannot be undone.");
        delete_exercise_dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exercise.delete(lift_db_helper);

                ExerciseListActivity.ExerciseListCardAdapter recycler_view_adapter = (ExerciseListActivity.ExerciseListCardAdapter) recycler_view.getAdapter();
                recycler_view_adapter.deleteExercise(exercise_position_in_adapter);
                Snackbar.make(snackbar_parent_view, "Exercise deleted.", Snackbar.LENGTH_LONG).show();
            }
        });
        delete_exercise_dialog_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(snackbar_parent_view, "Exercise not deleted.", Snackbar.LENGTH_LONG).show();
            }
        });
        delete_exercise_dialog_builder.show();

    }
}
