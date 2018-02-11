package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;

/**
 * Created by Tommy Smith on 12/13/2017.
 * Adapter to support showing exercises similar to those in a workout in a dialog.
 * @author  Tommy Smith
 */
class SimilarExercisesListAdapter extends ArrayAdapter<Exercise>
{
    /**
     * Constructor for the adapter.
     * @param context       The parent activity's context.
     * @param resource      The resource to use to draw the adapter's contents.
     * @param exerciseList  A list of exercises.
     */
    SimilarExercisesListAdapter(Context context, int resource, List<Exercise> exerciseList)
    {
        super(context, resource, exerciseList);
    }

    /**
     * Draws the contents of the adapter to the view.
     * @param position      The position in the adapter of the exercise.
     * @param convertView   The view to draw the contents of the exercise.
     * @param parent        The calling parent's view.
     * @return  The view, containing the exercise at the given adapter's position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            // Inflate the exercise detail layout.
            LayoutInflater li;
            li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.suggested_exercise_detail, null);
        }

        // Get the exercise at the given position in the adapter.
        Exercise exercise = getItem(position);
        if (exercise != null)
        {
            // Set the exercise name's text into the view in the suggested exercise layout.
            TextView exercise_name_text_view = v.findViewById(R.id.suggested_exercise_name);
            if (exercise_name_text_view != null)
            {
                exercise_name_text_view.setText(exercise.getName());
            }
        }

        // Return the view.
        return v;
    }
}