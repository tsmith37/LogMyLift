package edu.wvu.tsmith.logmylift.workout;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 4/27/2018.
 * A dialog used to show workout statistics.
 * @author Tommy Smith
 */

public class WorkoutStatsDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private int dialog_resource;
    private String dialog_title;
    private WorkoutDetailFragment workout_detail_fragment;
    private String workout_duration_text;
    private String lifts_performed_text;
    private String time_per_lift_text;

    /**
     * Constructor.
     * @param context                   The context in which to show the dialog.
     * @param lift_db_helper            The database helper.
     * @param workout_detail_fragment   The fragment to which the workout belongs.
     */
    public WorkoutStatsDialog(Context context, LiftDbHelper lift_db_helper, WorkoutDetailFragment workout_detail_fragment)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.dialog_resource = R.layout.workout_stats_dialog;
        this.dialog_title = this.context.getString(R.string.workout_statistics);
        this.workout_detail_fragment = workout_detail_fragment;
        this.workout_duration_text = this.context.getString(R.string.workout_duration);
        this.lifts_performed_text = this.context.getString(R.string.lifts_performed);
        this.time_per_lift_text = this.context.getString(R.string.average_time_per_lift);
    }

    /**
     * Show the dialog.
     */
    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View workout_stats_dialog_view = li.inflate(this.dialog_resource, null);
        AlertDialog.Builder workout_stats_dialog_builder = new AlertDialog.Builder(context);

        TextView title = new TextView(this.context);
        title.setText(this.dialog_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        workout_stats_dialog_builder.setCustomTitle(title);
        workout_stats_dialog_builder.setView(workout_stats_dialog_view);

        // Set the workout duration text.
        final String workout_duration = String.format(
                "%s %s",
                this.workout_duration_text,
                workout_detail_fragment.current_workout.getReadableDuration(this.lift_db_helper));
        final TextView workout_duration_text_view = workout_stats_dialog_view.findViewById(R.id.workout_duration_text_view);
        workout_duration_text_view.setText(workout_duration);

        // Set the lifts performed count.
        final String lifts_performed_count = String.format(
                "%s %d",
                this.lifts_performed_text,
                workout_detail_fragment.current_workout.getLiftsPerformedCount());
        final TextView lifts_performed_text_view = workout_stats_dialog_view.findViewById(R.id.workout_lifts_count_text_view);
        lifts_performed_text_view.setText(lifts_performed_count);

        // Set the time per lift text.
        final String time_per_lift = String.format(
                "%s %s",
                this.time_per_lift_text,
                workout_detail_fragment.current_workout.getReadableTimePerSet(lift_db_helper));
        final TextView time_per_lift_text_view = workout_stats_dialog_view.findViewById(R.id.average_time_per_lift_text_view);
        time_per_lift_text_view.setText(time_per_lift);

        AlertDialog workout_stats_dialog = workout_stats_dialog_builder.create();
        workout_stats_dialog.show();
    }
}
