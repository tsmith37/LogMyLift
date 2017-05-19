package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by tmssm on 5/15/2017.
 */

public class ExerciseHistoryCursorAdapter  extends CursorAdapter {
    public ExerciseHistoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.exercise_history_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView date_text_view = (TextView) view.findViewById(R.id.lift_date);
        TextView weight_text_view = (TextView) view.findViewById(R.id.weight);
        TextView reps_text_view = (TextView) view.findViewById(R.id.reps);
        TextView comment_text_view = (TextView) view.findViewById(R.id.comment);
        String readable_date = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cursor.getLong(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_START_DATE)));
        date_text_view.setText(readable_date);
        weight_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_WEIGHT)));
        reps_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_REPS)));
        comment_text_view.setText(cursor.getString(cursor.getColumnIndexOrThrow(LiftDbHelper.LIFT_COLUMN_COMMENT)));
    }
}
