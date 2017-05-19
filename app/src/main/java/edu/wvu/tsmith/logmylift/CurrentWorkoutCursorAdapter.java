package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by tmssm on 3/31/2017.
 */

// This class extends a CursorAdapter for information in the current workout.
public class CurrentWorkoutCursorAdapter extends CursorAdapter {
    public CurrentWorkoutCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.current_workout_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView exercise_text_view = (TextView) view.findViewById(R.id.exercise_name);
        TextView weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps);
        TextView comment_text_view = (TextView) view.findViewById(R.id.comment);

        String weight = cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_WEIGHT));
        String reps = cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_REPS));
        String weight_and_reps = "Weight: " + weight + ". Reps: " + reps + ".";

        exercise_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.EXERCISE_COLUMN_NAME)));
        weight_and_reps_text_view.setText(weight_and_reps);
        comment_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_COMMENT)));
    }
}
