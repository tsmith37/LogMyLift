package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Date;
import java.util.Locale;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.lift.AddLift;

/**
 * Created by Tommy Smith on 5/7/2018.
 * A dialog used to start a new workout.
 */

public class NewWorkoutDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private View snackbar_parent_view;

    public NewWorkoutDialog(Context context, LiftDbHelper lift_db_helper, View snackbar_parent_view)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.snackbar_parent_view = snackbar_parent_view;
    }

    public void show()
    {
        // Creates the new workout dialog.
        LayoutInflater li = LayoutInflater.from(this.context);
        View add_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder add_workout_dialog_builder = new AlertDialog.Builder(this.context);

        // The title of the dialog contains the date that the workout is being created.
        String new_workout_title = this.context.getString(R.string.new_workout) + ": " + new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        add_workout_dialog_builder.setTitle(new_workout_title);
        add_workout_dialog_builder.setView(add_workout_dialog_view);
        final EditText workout_description_text = add_workout_dialog_view.findViewById(R.id.workout_description_edit_text);

        // Handle a positive button press.
        add_workout_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Create the new workout.
                Workout new_workout = new Workout(lift_db_helper, workout_description_text.getText().toString());

                // Start the new activity with the ability to add a lift to a workout.
                Intent workout_intent = new Intent(context, AddLift.class);
                workout_intent.putExtra(WorkoutDetailFragment.workout_parcel, new_workout);
                context.startActivity(workout_intent);
            }
        });

        // Handle a negative button press.
        add_workout_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Notify the user that the workout was not added.
                Snackbar.make(snackbar_parent_view, "Workout not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_workout_dialog = add_workout_dialog_builder.create();
        add_workout_dialog.show();
    }
}
