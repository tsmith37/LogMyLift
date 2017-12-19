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

class SimilarExercisesListAdapter extends ArrayAdapter<Exercise> {
    public SimilarExercisesListAdapter(Context context, int resourceId)
    {
        super(context, resourceId);
    }

    SimilarExercisesListAdapter(Context context, int resource, List<Exercise> exerciseList)
    {
        super(context, resource, exerciseList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;

        if (v == null)
        {
            LayoutInflater li;
            li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.suggested_exercise_detail, null);
        }

        Exercise exercise = getItem(position);

        if (exercise != null)
        {
            TextView exercise_name_text_view = (TextView) v.findViewById(R.id.suggested_exercise_name);

            if (exercise_name_text_view != null)
            {
                exercise_name_text_view.setText(exercise.getName());
            }
        }

        return v;
    }
}
