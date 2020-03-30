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
    private Workout workout;
    public String workoutDescriptionBeforeEditing;
    public String workoutDescriptionAfterEditing;

    /**
     * Constructor for the dialog.
     * @param context                   The context in which to show the dialog.
     * @param workout                   The workout to edit.
     */
    public EditWorkoutDialog(Context context, Workout workout)
    {
        this.context = context;
        this.workout = workout;
    }

    /**
     * Show the dialog.
     */
    public void show(Callable<Integer> postEditFunction)
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View editWorkoutDialogView = li.inflate(R.layout.edit_workout_dialog, null);
        AlertDialog.Builder editWorkoutDialogBuilder = new AlertDialog.Builder(context);

        this.initTitle(editWorkoutDialogBuilder);

        // Set the edit workout dialog to reflect the current workout details.
        editWorkoutDialogBuilder.setView(editWorkoutDialogView);
        EditText workoutDescriptionEditText = editWorkoutDialogView.findViewById(R.id.workout_description_edit_text);
        this.workoutDescriptionBeforeEditing = workout.getDescription();
        this.setWorkoutDescriptionEditText(workoutDescriptionEditText);

        AlertDialog editWorkoutDialog = editWorkoutDialogBuilder.create();

        Button editWorkoutButton = editWorkoutDialogView.findViewById(R.id.edit_workout_button);
        this.initEditWorkoutButton(editWorkoutButton, workoutDescriptionEditText, editWorkoutDialog, postEditFunction);

        editWorkoutDialog.getWindow().setBackgroundDrawableResource(R.color.lightGray);
        editWorkoutDialog.show();
    }

    private void initTitle(AlertDialog.Builder editWorkoutDialogBuilder)
    {
        TextView title = new TextView(this.context);
        String edit_workout_title = String.format(
                "%s: %s",
                this.context.getString(R.string.edit_workout),
                this.workout.getReadableStartDate());
        title.setText(edit_workout_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        editWorkoutDialogBuilder.setCustomTitle(title);
    }

    private void setWorkoutDescriptionEditText(EditText workoutDescriptionEditText)
    {
        if (workoutDescriptionEditText != null)
        {
            workoutDescriptionEditText.setText(this.workoutDescriptionBeforeEditing);
        }
    }

    private void initEditWorkoutButton(Button editWorkoutButton, EditText workoutDescriptionEditText, AlertDialog editWorkoutDialog, Callable<Integer> postEditFunction)
    {
        if (editWorkoutButton != null && workoutDescriptionEditText != null && editWorkoutDialog != null)
        {
            editWorkoutButton.setOnClickListener(this.editWorkout(workoutDescriptionEditText, editWorkoutDialog, postEditFunction));
        }
    }

    private View.OnClickListener editWorkout(EditText workoutDescriptionEditText, AlertDialog editWorkoutDialog, Callable<Integer> postEditFunction)
    {
        return v -> {
            workoutDescriptionAfterEditing = workoutDescriptionEditText.getText().toString();
            try
            {
                postEditFunction.call();
            }
            catch (Exception ignored) {};

            editWorkoutDialog.cancel();
        };
    }
}
