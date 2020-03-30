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
    private RecyclerView recyclerView;
    private View snackbarParentView;
    private int exercisePositionInAdapter;
    private Exercise exercise;

    /**
     * Constructor for the dialog.
     * @param context                           The context in which the dialog will be shown.
     * @param recyclerView                     The recycler view from which the dialog is called.
     * @param snackbarParentView              The parent view for any displayed snackbars.
     * @param exercisePositionInAdapter      The position of the exercise being deleted in the exercise card adapter.
     * @param exercise                          The exercise to delete.
     */
    public DeleteExerciseDialog(
            Context context,
            RecyclerView recyclerView,
            View snackbarParentView,
            int exercisePositionInAdapter,
            Exercise exercise)
    {
        this.context = context;
        this.recyclerView = recyclerView;
        this.snackbarParentView = snackbarParentView;
        this.exercisePositionInAdapter = exercisePositionInAdapter;
        this.exercise = exercise;
    }

    public void show()
    {
        AlertDialog.Builder delete_exercise_dialog_builder = new AlertDialog.Builder(this.context);
        delete_exercise_dialog_builder.setTitle("Delete Exercise");
        delete_exercise_dialog_builder.setMessage("Are you sure you want to delete " + this.exercise.getName() + "? This action cannot be undone.");
        delete_exercise_dialog_builder.setPositiveButton("Yes", this.deleteExercise());

        delete_exercise_dialog_builder.setNegativeButton("No", this.cancelDeleteExercise());
        delete_exercise_dialog_builder.show();

    }

    private DialogInterface.OnClickListener deleteExercise()
    {
        DialogInterface.OnClickListener deleteExerciseListener = (dialogInterface, i) -> {
            exercise.delete();

            ExerciseListActivity.ExerciseListCardAdapter recycler_view_adapter = (ExerciseListActivity.ExerciseListCardAdapter) recyclerView.getAdapter();
            recycler_view_adapter.deleteExercise(exercisePositionInAdapter);
            Snackbar.make(snackbarParentView, "Exercise deleted.", Snackbar.LENGTH_LONG).show();
        };

        return deleteExerciseListener;
    }

    private DialogInterface.OnClickListener cancelDeleteExercise()
    {
        DialogInterface.OnClickListener cancelDeleteExerciseListener = (dialogInterface, i) ->
        {
            Snackbar.make(snackbarParentView, "Exercise not deleted.", Snackbar.LENGTH_LONG).show();
        };

        return cancelDeleteExerciseListener;
    }
}
