package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 4/29/2018.
 * A dialog to edit an exercise's name and description.
 */

public class EditExerciseDialog
{
    private Context context;
    private View snackbar_parent_view;
    private Exercise exercise;

    /**
     * Constructor for the dialog.
     * @param context               - The context in which to display the dialog.
     * @param snackbar_parent_view  - The parent view to show any needed snackbars.
     * @param exercise              - The exercise to edit.
     */
    public EditExerciseDialog
    (
            Context context,
            View snackbar_parent_view,
            Exercise exercise)
    {
        this.context = context;
        this.snackbar_parent_view = snackbar_parent_view;
        this.exercise = exercise;
    }

    /**
     * Show the dialog to the user.
     */
    public void show(final Callable<Integer> post_edit_function)
    {
        LayoutInflater li = LayoutInflater.from(this.context);

        // Re-use the add exercise dialog. It contains the same fields we want to use here.
        View edit_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder edit_exercise_dialog_builder = new AlertDialog.Builder(this.context);

        // Set the dialog to reflect the current exercise details.
        edit_exercise_dialog_builder.setTitle(R.string.edit_exercise);
        edit_exercise_dialog_builder.setView(edit_exercise_dialog_view);
        final EditText exercise_name_text = edit_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_text.setText(this.exercise.getName());
        final EditText exercise_description_text = edit_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);
        exercise_description_text.setText(this.exercise.getDescription());

        // Handle the positive button press.
        edit_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (exercise_name_text.getText().toString().isEmpty())
                {
                    Snackbar.make(snackbar_parent_view, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Snackbar.make(snackbar_parent_view, "Exercise updated.", Snackbar.LENGTH_LONG).show();

                    try
                    {
                        post_edit_function.call();
                    }
                    catch (Exception ignored) {}
                }
            }
        });

        // Handle the negative button press.
        edit_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Snackbar.make(snackbar_parent_view, "Exercise not updated.", Snackbar.LENGTH_LONG).show();
            }
        });
        AlertDialog edit_exercise_dialog = edit_exercise_dialog_builder.create();
        edit_exercise_dialog.show();
    }
}
