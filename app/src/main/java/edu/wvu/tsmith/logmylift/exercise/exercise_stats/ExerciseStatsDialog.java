package edu.wvu.tsmith.logmylift.exercise.exercise_stats;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.Lift;

public class ExerciseStatsDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private long exercise_id;
    private Exercise exercise;

    public ExerciseStatsDialog(Context context, LiftDbHelper lift_db_helper, long exercise_id)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.exercise_id = exercise_id;
        this.exercise = this.lift_db_helper.selectExerciseFromExerciseId(this.exercise_id);
    }

    public void show()
    {
        // Inflate the dialog with the layout.
        LayoutInflater li = LayoutInflater.from(this.context);
        View exercise_stats_dialog_view = li.inflate(R.layout.exercise_stats_dialog, null);
        AlertDialog.Builder exercise_stats_dialog_builder = new AlertDialog.Builder(context);
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.exercise_stats));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        exercise_stats_dialog_builder.setCustomTitle(title);
        exercise_stats_dialog_builder.setView(exercise_stats_dialog_view);

        TextView exercise_name_text_view = exercise_stats_dialog_view.findViewById(R.id.exercise_name_text_view);
        setNameTextView(exercise_name_text_view);

        TextView max_effort_text_view = exercise_stats_dialog_view.findViewById(R.id.exercise_max_effort_text_view);
        setMaxEffortTextView(max_effort_text_view);

        TextView heaviest_lift_text_view = exercise_stats_dialog_view.findViewById(R.id.exercise_heaviest_lift_text_view);
        setHeaviestLiftTextView(heaviest_lift_text_view);

        TextView lifts_performed_text_view = exercise_stats_dialog_view.findViewById(R.id.lifts_performed_text_view);
        setLiftsPerformedTextView(lifts_performed_text_view);

        TextView workouts_performed_text_view = exercise_stats_dialog_view.findViewById(R.id.workouts_performed_text_view);
        setWorkoutsPerformedTextView(workouts_performed_text_view);

        Button max_effort_trend_button = exercise_stats_dialog_view.findViewById(R.id.max_effort_trend_button);
        setMaxEffortTrendButton(max_effort_trend_button);

        Button max_weight_trend_button = exercise_stats_dialog_view.findViewById(R.id.weight_trend_button);
        setMaxWeightTrendButton(max_weight_trend_button);

        // Display the dialog.
        AlertDialog exercise_stats_dialog = exercise_stats_dialog_builder.create();
        exercise_stats_dialog.show();
    }

    private void setNameTextView(TextView name_text_view)
    {
        name_text_view.setText(this.exercise.getName());
    }

    private void setMaxEffortTextView(TextView max_effort_text_view)
    {
        Lift max_lift = this.lift_db_helper.selectMaxEffortLiftByExercise(this.exercise_id);
        if (max_lift != null)
        {
            String max_effort_text = String.format("%s %d (%d for %d)", this.context.getString(R.string.max_effort), max_lift.calculateMaxEffort(), max_lift.getWeight(), max_lift.getReps());
            max_effort_text_view.setText(max_effort_text);
        }
    }

    private void setHeaviestLiftTextView(TextView heaviest_lift_text_view)
    {
        Lift heaviest_lift = this.lift_db_helper.selectHeaviestLiftByExercise(this.exercise_id);
        if (heaviest_lift != null)
        {
            String heaviest_lift_text = String.format("%s %d for %d", this.context.getString(R.string.heaviest_lift), heaviest_lift.getWeight(), heaviest_lift.getReps());
            heaviest_lift_text_view.setText(heaviest_lift_text);
        }
    }

    private void setLiftsPerformedTextView(TextView lifts_performed_text_view)
    {
        int lifts_performed = this.lift_db_helper.selectCountOfLiftsByExercise(this.exercise_id);
        String lifts_performed_text = String.format("%s %d", this.context.getString(R.string.lifts_performed), lifts_performed);
        lifts_performed_text_view.setText(lifts_performed_text);
    }

    private void setWorkoutsPerformedTextView(TextView workouts_performed_text_view)
    {
        int workouts_performed = this.lift_db_helper.selectCountOfWorkoutsByExercise(this.exercise_id);
        String workouts_performed_text = String.format("%s %d", this.context.getString(R.string.workouts_performed_during), workouts_performed);
        workouts_performed_text_view.setText(workouts_performed_text);
    }

    private void setMaxEffortTrendButton(final Button trend_button)
    {
        trend_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ExerciseTrendDialog trend_dialog = new ExerciseTrendDialog(context, lift_db_helper, exercise_id, ExerciseTrendDialog.TrendType.MAX_EFFORT);
                trend_dialog.show();
            }
        });
    }

    private void setMaxWeightTrendButton(final Button trend_button)
    {
        trend_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ExerciseTrendDialog trend_dialog = new ExerciseTrendDialog(context, lift_db_helper, exercise_id, ExerciseTrendDialog.TrendType.MAX_WEIGHT);
                trend_dialog.show();
            }
        });
    }
}