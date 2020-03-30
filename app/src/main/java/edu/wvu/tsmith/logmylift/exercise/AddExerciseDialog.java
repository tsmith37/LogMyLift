package edu.wvu.tsmith.logmylift.exercise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
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
 * Dialog to add a new exercise. This sets the currently selected exercise to the new exercise. The
 * dialog can take a "hint", which it will display to the user as a suggested exercise name.
 */

public class AddExerciseDialog
{
    private Context context;
    private LiftDbHelper liftDbHelper;
    private String exerciseNameHint;
    private View snackbarParentView;

    private Callable<Integer> postRunFunction;

    /**
     * Constructor for the dialog.
     * @param context               - The context in which to display the dialog.
     * @param liftDbHelper          - The database helper used for the dialog.
     * @param snackbarParentView    - The parent view to show any needed snackbars.
     * @param exerciseNameHint      - A hint to display to the user as a suggested exercise name.
     */
    public AddExerciseDialog
    (
            Context context,
            LiftDbHelper liftDbHelper,
            View snackbarParentView,
            String exerciseNameHint)
    {
        this.context = context;
        this.exerciseNameHint = exerciseNameHint;
        this.snackbarParentView = snackbarParentView;
        this.liftDbHelper = liftDbHelper;
    }

    /**
     * Show the dialog to the user without a post-add function.
     */
    public void show()
    {
        // Set the post add function to null. That exception will be caught and no error will be thrown.
        this.show(null);
    }

    /**
     * Show the dialog to the user with a post-add function.
     */
    public void show(final Callable<Integer> postAddFunction)
    {
        // Inflate the dialog with the layout.
        LayoutInflater li = LayoutInflater.from(this.context);
        View addExerciseDialogView = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder addExerciseDialogBuilder = new AlertDialog.Builder(context);
        this.initTitle(addExerciseDialogBuilder);

        addExerciseDialogBuilder.setView(addExerciseDialogView);

        // Get the exercise name text box. Display any hint to the user.
        EditText exerciseNameEditText = addExerciseDialogView.findViewById(R.id.exercise_name_edit_text);
        this.initExerciseNameEditText(exerciseNameEditText, exerciseNameHint);

        // Get the exercise description text box.
        EditText exerciseDescriptionEditText = addExerciseDialogView.findViewById(R.id.exercise_description_edit_text);

        AlertDialog addExerciseDialog = addExerciseDialogBuilder.create();

        // Handle the positive button press.
        Button addExerciseButton = addExerciseDialogView.findViewById(R.id.add_exercise_button);
        this.initAddExerciseButton(addExerciseDialog, addExerciseButton, exerciseNameEditText, exerciseDescriptionEditText);
        this.postRunFunction = postAddFunction;

        addExerciseDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
        });

        // Display the dialog.
        addExerciseDialog.show();
    }

    private void initTitle(AlertDialog.Builder addExerciseDialogBuilder)
    {
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.add_exercise));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        addExerciseDialogBuilder.setCustomTitle(title);
    }

    private void initExerciseNameEditText(EditText exerciseNameEditText, String exerciseNameHint)
    {
        if (exerciseNameEditText != null)
        {
            exerciseNameEditText.setText(exerciseNameHint);
        }
    }

    private void initAddExerciseButton(AlertDialog this_dialog, Button addExerciseButton, EditText exerciseNameEditText, EditText exerciseDescriptionEditText)
    {
        if (addExerciseButton != null && exerciseNameEditText != null && exerciseDescriptionEditText != null)
        {
            addExerciseButton.setOnClickListener(this.addExerciseClickListener(this_dialog, exerciseNameEditText, exerciseDescriptionEditText));
        }
    }

    private View.OnClickListener addExerciseClickListener(AlertDialog this_dialog, EditText exerciseNameEditText, EditText exerciseDescriptionEditText)
    {
        return v -> {
            // Check that a name has been inserted.
            if (exerciseNameEditText.getText().toString().isEmpty())
            {
                Snackbar.make(snackbarParentView, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
            }
            else
            {
                try
                {
                    // Create the new exercise.
                    Exercise newExercise = new Exercise.Builder()
                            .name(exerciseNameEditText.getText().toString())
                            .description(exerciseDescriptionEditText.getText().toString())
                            .liftDbHelper(liftDbHelper)
                            .toCreate(true)
                            .build();
                    Snackbar.make(snackbarParentView, "Exercise added.", Snackbar.LENGTH_LONG).show();

                    // Set the selected exercise as the new one.
                    liftDbHelper.updateSelectedExercise(newExercise.getExerciseId());

                    try
                    {
                        postRunFunction.call();
                    }
                    catch (Exception ignored) {};
                }
                catch (Exception e)
                {
                    // Are there any other reasons for an exception to happen here?
                    Snackbar.make(snackbarParentView, "Exercise already exists.", Snackbar.LENGTH_LONG).show();
                }

            }

            this_dialog.cancel();
        };
    }
}
