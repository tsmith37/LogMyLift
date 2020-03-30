package edu.wvu.tsmith.logmylift.exercise;

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

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 4/29/2018.
 * A dialog to edit an exercise's name and description.
 */

public class EditExerciseDialog
{
    private Context context;
    private View snackbarParentView;
    private Exercise exercise;

    /**
     * Constructor for the dialog.
     * @param context               - The context in which to display the dialog.
     * @param snackbarParentView  - The parent view to show any needed snackbars.
     * @param exercise              - The exercise to edit.
     */
    public EditExerciseDialog
    (
            Context context,
            View snackbarParentView,
            Exercise exercise)
    {
        this.context = context;
        this.snackbarParentView = snackbarParentView;
        this.exercise = exercise;
    }

    /**
     * Show the dialog to the user.
     */
    public void show(final Callable<Integer> postEditFunction)
    {
        LayoutInflater li = LayoutInflater.from(this.context);

        // Re-use the add exercise dialog. It contains the same fields we want to use here.
        View editExerciseDialogView = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder editExerciseDialogBuilder = new AlertDialog.Builder(this.context);

        // Set the dialog to reflect the current exercise details.
        this.initTitle(editExerciseDialogBuilder);
        editExerciseDialogBuilder.setView(editExerciseDialogView);

        EditText exerciseNameEditText = editExerciseDialogView.findViewById(R.id.exercise_name_edit_text);
        this.setExerciseNameEditText(exerciseNameEditText);

        EditText exerciseDescriptionText = editExerciseDialogView.findViewById(R.id.exercise_description_edit_text);
        this.setExerciseDescriptionEditText(exerciseDescriptionText);

        AlertDialog editExerciseDialog = editExerciseDialogBuilder.create();

        Button editExerciseButton = editExerciseDialogView.findViewById(R.id.add_exercise_button);
        editExerciseButton.setText(this.context.getString(R.string.edit_exercise));
        this.initEditExerciseButton(editExerciseButton, editExerciseDialog, exerciseNameEditText, exerciseDescriptionText, postEditFunction);

        editExerciseDialog.show();
    }

    private void setExerciseNameEditText(EditText exerciseNameEditText)
    {
        if (exerciseNameEditText != null)
        {
            exerciseNameEditText.setText(this.exercise.getName());
        }
    }

    private void setExerciseDescriptionEditText(EditText exerciseDescriptionEditText)
    {
        if (exerciseDescriptionEditText != null)
        {
            exerciseDescriptionEditText.setText(this.exercise.getDescription());
        }
    }

    private void initTitle(AlertDialog.Builder editExerciseDialogBuilder)
    {
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.edit_exercise));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        editExerciseDialogBuilder.setCustomTitle(title);
    }

    private void initEditExerciseButton(Button editExerciseButton, AlertDialog editExerciseDialog, EditText exerciseNameEditText, EditText exerciseDescriptionEditText, Callable<Integer> postEditFunction)
    {
        if (editExerciseButton != null && editExerciseDialog != null && exerciseNameEditText != null && exerciseDescriptionEditText != null)
        {
            editExerciseButton.setOnClickListener(this.editExercise(editExerciseDialog, exerciseNameEditText, exerciseDescriptionEditText, postEditFunction));
        }
    }

    private View.OnClickListener editExercise(AlertDialog editExerciseDialog, EditText exerciseNameEditText, EditText exerciseDescriptionEditText, Callable<Integer> postEditFunction)
    {
        return v -> {
            if (exerciseNameEditText.getText().toString().isEmpty())
            {
                Snackbar.make(snackbarParentView, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
            }
            else
            {
                exercise.setName(exerciseNameEditText.getText().toString());
                exercise.setDescription(exerciseDescriptionEditText.getText().toString());
                Snackbar.make(snackbarParentView, "Exercise updated.", Snackbar.LENGTH_LONG).show();

                try
                {
                    postEditFunction.call();
                }
                catch (Exception ignored) {}
            }

            editExerciseDialog.cancel();
        };
    }
}
