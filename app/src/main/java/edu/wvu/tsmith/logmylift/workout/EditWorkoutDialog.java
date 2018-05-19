package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

        TextView title = new TextView(this.context);
        String edit_workout_title = String.format(
                "%s: %s",
                this.edit_workout_text,
                this.workout.getReadableStartDate());
        title.setText(edit_workout_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        edit_workout_dialog_builder.setCustomTitle(title);

        // Set the edit workout dialog to reflect the current workout details.
        edit_workout_dialog_builder.setView(edit_workout_dialog_view);
        final EditText workout_description_text = edit_workout_dialog_view.findViewById(R.id.workout_description_edit_text);
        this.workout_description_before_editing = workout.getDescription();
        workout_description_text.setText(this.workout_description_before_editing);

        final AlertDialog edit_workout_dialog = edit_workout_dialog_builder.create();

        Button edit_workout_button = edit_workout_dialog_view.findViewById(R.id.edit_workout_button);
        edit_workout_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                workout_description_after_editing = workout_description_text.getText().toString();
                try
                {
                    post_edit_function.call();
                }
                catch (Exception ignored) {};

                edit_workout_dialog.cancel();
            }
        });

        edit_workout_dialog.getWindow().setBackgroundDrawableResource(R.color.lightGray);
        edit_workout_dialog.show();
    }
}
