package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 4/27/2018.
 */

public class EditWorkoutDialog
{
    private Context context;
    private int edit_workout_dialog_resource;
    private WorkoutDetailFragment workout_detail_fragment;
    private String edit_workout_text;
    private View snackbar_parent_view;

    public EditWorkoutDialog(Context context, WorkoutDetailFragment workout_detail_fragment, View snackbar_parent_view)
    {
        this.context = context;
        this.edit_workout_dialog_resource = R.layout.edit_workout_dialog;
        this.workout_detail_fragment = workout_detail_fragment;
        this.edit_workout_text = this.context.getString(R.string.edit_workout);
        this.snackbar_parent_view = snackbar_parent_view;
    }

    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View edit_workout_dialog_view = li.inflate(this.edit_workout_dialog_resource, null);
        AlertDialog.Builder edit_workout_dialog_builder = new AlertDialog.Builder(context);

        // Set the edit workout dialog to reflect the current workout details.
        String edit_workout_title = String.format(
                "%s: %s",
                this.edit_workout_text,
                this.workout_detail_fragment.current_workout.getReadableStartDate());
        edit_workout_dialog_builder.setTitle(edit_workout_title);
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final EditText workout_description_text = edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        final String workout_before_editing_description = workout_detail_fragment.current_workout.getDescription();
        workout_description_text.setText(workout_before_editing_description);

        // Change the workout details.
        edit_workout_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar update_workout_snackbar = Snackbar.make(snackbar_parent_view, R.string.workout_updated, Snackbar.LENGTH_LONG);
                update_workout_snackbar.setAction(R.string.undo, new UndoUpdateWorkoutListener(workout_before_editing_description, workout_detail_fragment));
                update_workout_snackbar.show();
                workout_detail_fragment.setWorkoutDescription(workout_description_text.getText().toString());
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

    // Undo updating the workout.
    private class UndoUpdateWorkoutListener implements View.OnClickListener
    {
        final String old_description;
        final WorkoutDetailFragment workout_detail_fragment;

        UndoUpdateWorkoutListener(String old_description, WorkoutDetailFragment workout_detail_fragment)
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
}
