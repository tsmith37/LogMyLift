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
    private LiftDbHelper liftDbHelper;
    private long exerciseId;
    private Exercise exercise;

    public ExerciseStatsDialog(Context context, LiftDbHelper liftDbHelper, long exerciseId)
    {
        this.context = context;
        this.liftDbHelper = liftDbHelper;
        this.exerciseId = exerciseId;
        this.exercise = this.liftDbHelper.selectExerciseFromExerciseId(this.exerciseId);
    }

    public void show()
    {
        // Inflate the dialog with the layout.
        LayoutInflater li = LayoutInflater.from(this.context);
        View exerciseStatsDialogView = li.inflate(R.layout.exercise_stats_dialog, null);
        AlertDialog.Builder exerciseStatsDialogBuilder = new AlertDialog.Builder(context);
        this.initTitle(exerciseStatsDialogBuilder);

        exerciseStatsDialogBuilder.setView(exerciseStatsDialogView);

        TextView exerciseNameTextView = exerciseStatsDialogView.findViewById(R.id.exercise_name_text_view);
        this.setNameTextView(exerciseNameTextView);

        TextView maxEffortTextView = exerciseStatsDialogView.findViewById(R.id.exercise_max_effort_text_view);
        this.setMaxEffortTextView(maxEffortTextView);

        TextView heaviestLiftTextView = exerciseStatsDialogView.findViewById(R.id.exercise_heaviest_lift_text_view);
        this.setHeaviestLiftTextView(heaviestLiftTextView);

        TextView liftsPerformedTextView = exerciseStatsDialogView.findViewById(R.id.lifts_performed_text_view);
        this.setLiftsPerformedTextView(liftsPerformedTextView);

        TextView workoutsPerformedTextView = exerciseStatsDialogView.findViewById(R.id.workouts_performed_text_view);
        this.setWorkoutsPerformedTextView(workoutsPerformedTextView);

        Button maxEffortTrendButton = exerciseStatsDialogView.findViewById(R.id.max_effort_trend_button);
        this.setMaxEffortTrendButton(maxEffortTrendButton);

        Button maxWeightTrendButton = exerciseStatsDialogView.findViewById(R.id.weight_trend_button);
        this.setMaxWeightTrendButton(maxWeightTrendButton);

        // Display the dialog.
        AlertDialog exercise_stats_dialog = exerciseStatsDialogBuilder.create();
        exercise_stats_dialog.show();
    }

    private void initTitle(AlertDialog.Builder exerciseStatsDialogBuilder)
    {
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.exercise_stats));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        exerciseStatsDialogBuilder.setCustomTitle(title);
    }

    private void setNameTextView(TextView nameTextView)
    {
        if (nameTextView != null)
        {
            nameTextView.setText(this.exercise.getName());
        }
    }

    private void setMaxEffortTextView(TextView maxEffortTextView)
    {
        Lift maxLift = this.liftDbHelper.selectMaxEffortLiftByExercise(this.exerciseId);
        if (maxLift != null && maxEffortTextView != null)
        {
            String maxEffortText = String.format("%s %d (%d for %d)", this.context.getString(R.string.max_effort), maxLift.calculateMaxEffort(), maxLift.getWeight(), maxLift.getReps());
            maxEffortTextView.setText(maxEffortText);
        }
    }

    private void setHeaviestLiftTextView(TextView heaviestLiftTextView)
    {
        Lift heaviestLift = this.liftDbHelper.selectHeaviestLiftByExercise(this.exerciseId);
        if (heaviestLift != null && heaviestLiftTextView != null)
        {
            String heaviest_lift_text = String.format("%s %d for %d", this.context.getString(R.string.heaviest_lift), heaviestLift.getWeight(), heaviestLift.getReps());
            heaviestLiftTextView.setText(heaviest_lift_text);
        }
    }

    private void setLiftsPerformedTextView(TextView liftsPerformedTextView)
    {
        int liftsPerformed = this.liftDbHelper.selectCountOfLiftsByExercise(this.exerciseId);
        String liftsPerformedText = String.format("%s %d", this.context.getString(R.string.lifts_performed), liftsPerformed);

        if (liftsPerformedTextView != null)
        {
            liftsPerformedTextView.setText(liftsPerformedText);
        }
    }

    private void setWorkoutsPerformedTextView(TextView workoutsPerformedTextView)
    {
        int workoutsPerformed = this.liftDbHelper.selectCountOfWorkoutsByExercise(this.exerciseId);
        String workoutsPerformedText = String.format("%s %d", this.context.getString(R.string.workouts_performed_during), workoutsPerformed);

        if (workoutsPerformedTextView != null)
        {
            workoutsPerformedTextView.setText(workoutsPerformedText);
        }
    }

    private void setMaxEffortTrendButton(Button trendButton)
    {
        if (trendButton != null)
        {
            trendButton.setOnClickListener(v -> {
                ExerciseTrendDialog trendDialog = new ExerciseTrendDialog(context, liftDbHelper, exerciseId, ExerciseTrendDialog.TrendType.MAX_EFFORT);
                trendDialog.show();
            });
        }
    }

    private void setMaxWeightTrendButton(Button trendButton)
    {
        if (trendButton != null)
        {
            trendButton.setOnClickListener(v -> {
                ExerciseTrendDialog trendDialog = new ExerciseTrendDialog(context, liftDbHelper, exerciseId, ExerciseTrendDialog.TrendType.MAX_WEIGHT);
                trendDialog.show();
            });
        }
    }
}