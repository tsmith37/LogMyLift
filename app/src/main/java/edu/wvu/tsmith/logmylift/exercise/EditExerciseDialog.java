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
    private View snackbar_parent_view;
    private Exercise exercise;
    private LiftDbHelper lift_db_helper;

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
        this.lift_db_helper = new LiftDbHelper(this.context);
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

        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.edit_exercise));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        edit_exercise_dialog_builder.setCustomTitle(title);

        // Set the dialog to reflect the current exercise details.
        edit_exercise_dialog_builder.setView(edit_exercise_dialog_view);

        final EditText exercise_name_text = edit_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_text.setText(this.exercise.getName());
        final EditText exercise_description_text = edit_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);
        exercise_description_text.setText(this.exercise.getDescription());

        final AlertDialog edit_exercise_dialog = edit_exercise_dialog_builder.create();

        Button edit_exercise_button = edit_exercise_dialog_view.findViewById(R.id.add_exercise_button);
        if (edit_exercise_button != null)
        {
            edit_exercise_button.setText(R.string.edit_exercise);
            edit_exercise_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (exercise_name_text.getText().toString().isEmpty())
                    {
                        Snackbar.make(snackbar_parent_view, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                    }
                    else
                    {
                        exercise.setName(lift_db_helper, exercise_name_text.getText().toString());
                        exercise.setDescription(lift_db_helper, exercise_description_text.getText().toString());
                        Snackbar.make(snackbar_parent_view, "Exercise updated.", Snackbar.LENGTH_LONG).show();

                        try {
                            post_edit_function.call();
                        } catch (Exception ignored) {
                        }
                    }

                    edit_exercise_dialog.cancel();
                }
            });
        }

        edit_exercise_dialog.show();
    }
}
