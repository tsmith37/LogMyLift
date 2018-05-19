package edu.wvu.tsmith.logmylift.workout;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;

/**
 * Created by Tommy Smith on 4/27/2018.
 * A dialog used to show the suggested exercises during a workout.
 */

public class SuggestedExercisesDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private Workout workout;
    private AutoCompleteTextView exercise_text_view;

    /**
     * Constructor.
     * @param context               The context in which to show the dialog.
     * @param lift_db_helper        The database helper.
     * @param workout               The workout for which to show suggested exercises.
     * @param exercise_text_view    The text view to insert the user's choice of suggested exercise into.
     */
    public SuggestedExercisesDialog(
            Context context,
            LiftDbHelper lift_db_helper,
            Workout workout,
            AutoCompleteTextView exercise_text_view)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.workout = workout;
        this.exercise_text_view = exercise_text_view;
    }

    /**
     * Show the dialog without a post-selection function.
     */
    public void show()
    {
        this.show(null);
    }

    /**
     * Show the dialog, with a post-selection function.
     */
    public void show(final Callable<Integer> post_selection_function)
    {
        // Create the suggested exercise dialog.
        LayoutInflater li = LayoutInflater.from(this.context);
        final View suggested_exercises_dialog_view = li.inflate(R.layout.suggested_exercises_dialog, null);
        AlertDialog.Builder suggested_exercises_dialog_builder = new AlertDialog.Builder(this.context);

        TextView title = new TextView(this.context);
        String edit_workout_title = this.context.getString(R.string.suggested_exercises_text);
        title.setText(edit_workout_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        suggested_exercises_dialog_builder.setCustomTitle(title);
        suggested_exercises_dialog_builder.setView(suggested_exercises_dialog_view);

        // Get the list of similar exercise's IDs.
        ArrayList<Long> similar_exercise_ids = workout.getSimilarExercises();
        final ArrayList<Exercise> similar_exercises = new ArrayList<>();

        // Iterate through the similar exercises and add them to the dialog.
        int similar_exercise_index = 0;
        while (true)
        {
            // There are no more similar exercises.
            if (similar_exercise_index >= similar_exercise_ids.size())
            {
                break;
            }

            // Don't show more than 5 similar exercises.
            if (similar_exercise_index >= 5)
            {
                break;
            }

            // Create the similar exercise from the exercise ID.
            Exercise similar_exercise = this.lift_db_helper.selectExerciseFromExerciseId(similar_exercise_ids.get(similar_exercise_index));
            similar_exercises.add(similar_exercise);
            ++similar_exercise_index;
        }

        // Create a list view from the suggested exercises.
        final ListView suggested_exercises_list_view = suggested_exercises_dialog_view.findViewById(R.id.suggested_exercises_list_view);
        SimilarExercisesListAdapter similar_exercise_list_adapter = new SimilarExercisesListAdapter(this.context, R.layout.suggested_exercise_detail, similar_exercises);
        suggested_exercises_list_view.setAdapter(similar_exercise_list_adapter);
        final AlertDialog suggested_exercises_dialog = suggested_exercises_dialog_builder.create();

        // When any exercise is clicked, set the text view's input with the name of the exercise.
        suggested_exercises_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                exercise_text_view.setText(similar_exercises.get(position).getName());
                exercise_text_view.dismissDropDown();
                suggested_exercises_dialog.cancel();
                try
                {
                    post_selection_function.call();
                }
                catch (Exception ignored) {};
            }
        });
        suggested_exercises_dialog.show();
    }
}
