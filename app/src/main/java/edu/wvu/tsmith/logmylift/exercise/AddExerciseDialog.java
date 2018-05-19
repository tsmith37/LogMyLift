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
    private LiftDbHelper lift_db_helper;
    private String exercise_name_hint;
    private View snackbar_parent_view;

    /**
     * Constructor for the dialog.
     * @param context               - The context in which to display the dialog.
     * @param lift_db_helper        - The database helper used for the dialog.
     * @param snackbar_parent_view  - The parent view to show any needed snackbars.
     * @param exercise_name_hint    - A hint to display to the user as a suggested exercise name.
     */
    public AddExerciseDialog
    (
            Context context,
            LiftDbHelper lift_db_helper,
            View snackbar_parent_view,
            String exercise_name_hint)
    {
        this.context = context;
        this.exercise_name_hint = exercise_name_hint;
        this.snackbar_parent_view = snackbar_parent_view;
        this.lift_db_helper = lift_db_helper;
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
    public void show(final Callable<Integer> post_add_function)
    {
        // Inflate the dialog with the layout.
        LayoutInflater li = LayoutInflater.from(this.context);
        View add_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder add_exercise_dialog_builder = new AlertDialog.Builder(context);
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.add_exercise));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        add_exercise_dialog_builder.setCustomTitle(title);
        add_exercise_dialog_builder.setView(add_exercise_dialog_view);

        // Get the exercise name text box. Display any hint to the user.
        final EditText exercise_name_edit_text = add_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_edit_text.setText(exercise_name_hint);

        // Get the exercise description text box.
        final EditText exercise_description_edit_text = add_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);

        final AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();

        // Handle the positive button press.
        Button add_exercise_button = add_exercise_dialog_view.findViewById(R.id.add_exercise_button);
        if (add_exercise_button != null)
        {
            add_exercise_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Check that a name has been inserted.
                    if (exercise_name_edit_text.getText().toString().isEmpty())
                    {
                        Snackbar.make(snackbar_parent_view, "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                    }
                    else
                    {
                        try
                        {
                            // Create the new exercise.
                            Exercise new_exercise = new Exercise(lift_db_helper, exercise_name_edit_text.getText().toString(), exercise_description_edit_text.getText().toString());
                            Snackbar.make(snackbar_parent_view, "Exercise added.", Snackbar.LENGTH_LONG).show();

                            // Set the selected exercise as the new one.
                            lift_db_helper.updateSelectedExercise(new_exercise.getExerciseId());

                            try
                            {
                                post_add_function.call();
                            }
                            catch (Exception ignored) {};
                        }
                        catch (Exception e)
                        {
                            // Are there any other reasons for an exception to happen here?
                            Snackbar.make(snackbar_parent_view, "Exercise already exists.", Snackbar.LENGTH_LONG).show();
                        }

                    }

                    add_exercise_dialog.cancel();
                }
            });
        }
        add_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        // Display the dialog.
        add_exercise_dialog.show();
    }
}
