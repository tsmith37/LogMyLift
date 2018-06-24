package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    public NewWorkoutDialog(Context context, LiftDbHelper lift_db_helper)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
    }

    public void show()
    {
        // Creates the new workout dialog.
        LayoutInflater li = LayoutInflater.from(this.context);
        View add_workout_dialog_view = li.inflate(R.layout.add_workout_dialog, null);
        AlertDialog.Builder add_workout_dialog_builder = new AlertDialog.Builder(this.context);

        TextView title = new TextView(this.context);
        String add_workout_title = this.context.getString(R.string.add_workout);
        title.setText(add_workout_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        add_workout_dialog_builder.setCustomTitle(title);

        add_workout_dialog_builder.setView(add_workout_dialog_view);

        final AlertDialog add_workout_dialog = add_workout_dialog_builder.create();
        final EditText workout_description_text = add_workout_dialog_view.findViewById(R.id.workout_description_edit_text);

        Button add_workout_button = add_workout_dialog_view.findViewById(R.id.add_workout_button);
        add_workout_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Clear the last exercise that was performed.
                lift_db_helper.removeSelectedExercise();

                // Create the new workout.
                Workout new_workout = new Workout(lift_db_helper, workout_description_text.getText().toString());

                // Start the new activity with the ability to add a lift to a workout.
                Intent workout_intent = new Intent(context, AddLift.class);
                workout_intent.putExtra(WorkoutDetailFragment.workout_parcel, new_workout);
                context.startActivity(workout_intent);

                add_workout_dialog.cancel();
            }
        });

        add_workout_dialog.getWindow().setBackgroundDrawableResource(R.color.lightGray);
        add_workout_dialog.show();
    }
}
