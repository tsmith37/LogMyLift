package edu.wvu.tsmith.logmylift.workout;

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
 * Created by Tommy Smith on 4/27/2018.
 * A dialog used to edit a workout's description.
 */

public class EditWorkoutDialog
{
    private Context context;
    private int edit_workout_dialog_resource;
    private Workout workout;
    private String edit_workout_text;
    private View snackbar_parent_view;
    public String workout_description_before_editing;
    public String workout_description_after_editing;

    /**
     * Constructor for the dialog.
     * @param context                   The context in which to show the dialog.
     * @param workout                   The workout to edit.
     * @param snackbar_parent_view      The parent view used to show the snackbar.
     */
    public EditWorkoutDialog(Context context, Workout workout, View snackbar_parent_view)
    {
        this.context = context;
        this.edit_workout_dialog_resource = R.layout.edit_workout_dialog;
        this.workout = workout;
        this.edit_workout_text = this.context.getString(R.string.edit_workout);
        this.snackbar_parent_view = snackbar_parent_view;
    }

    /**
     * Show the dialog.
     */
    public void show(final Callable<Integer> post_edit_function)
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View edit_workout_dialog_view = li.inflate(this.edit_workout_dialog_resource, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(context);

        // Set the edit workout dialog to reflect the current workout details.
        String edit_workout_title = String.format(
                "%s: %s",
                this.edit_workout_text,
                this.workout.getReadableStartDate());
        edit_workout_dialog_builder.setTitle(edit_workout_title);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final EditText workout_description_text = edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        this.workout_description_before_editing = workout.getDescription();
        workout_description_text.setText(this.workout_description_before_editing);

        // Change the workout details.
        edit_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                workout_description_after_editing = workout_description_text.getText().toString();
                try
                {
                    post_edit_function.call();
                }
                catch (Exception ignored) {};
            }
        });

        // Cancel the changes.
        edit_workout_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(snackbar_parent_view, R.string.workout_not_updated, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();
        edit_workout_dialog.show();

    }
}
