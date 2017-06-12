package edu.wvu.tsmith.logmylift.exercise;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.lift.Lift;

/**
 * Created by Tommy Smith on 6/10/2017.
 * This class serves as an adapter to the CardView displaying the history of an exercise.
 */

class ExerciseHistoryCardAdapter extends RecyclerView.Adapter<ExerciseHistoryCardAdapter.ExerciseHistoryCardViewHolder> {
    private final Exercise current_exercise;
    private final ArrayList<Lift> current_exercise_lifts;
    private final Activity parent_activity;
    private final LiftDbHelper lift_db_helper;
    private final TextView exercise_description_text_view;

    ExerciseHistoryCardAdapter(Activity parent_activity, LiftDbHelper lift_db_helper, TextView exercise_description_text_view, Exercise current_exercise)
    {
        this.parent_activity = parent_activity;
        this.lift_db_helper = lift_db_helper;
        this.current_exercise = current_exercise;

        if (this.current_exercise != null)
        {
            this.current_exercise_lifts = this.lift_db_helper.selectExerciseHistoryLifts(this.current_exercise);
        }
        else
        {
            this.current_exercise_lifts = null;
        }

        this.exercise_description_text_view = exercise_description_text_view;
    }

    @Override
    public ExerciseHistoryCardAdapter.ExerciseHistoryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_history_card_view, null);
        return new ExerciseHistoryCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseHistoryCardAdapter.ExerciseHistoryCardViewHolder holder, int position) {
        Lift current_lift = current_exercise_lifts.get(position);

        // If this lift is the max of the exercise, display that along with the date.
        String date_text_view_text = "";
        if (current_exercise.getMaxLiftId() == current_lift.getLiftId())
        {
            date_text_view_text += "Max Effort: ";
        }

        // Display the rest of the lift.
        date_text_view_text += current_lift.getReadableStartDate();
        holder.date_text_view.setText(date_text_view_text);
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    @Override
    public int getItemCount() {
        if (current_exercise_lifts != null)
        {
            return current_exercise_lifts.size();
        }
        return 0;
    }

    void setExerciseName(String name)
    {
        current_exercise.setName(lift_db_helper, name);
    }

    void setExerciseDescription(String description)
    {
        current_exercise.setDescription(lift_db_helper, description);
    }

    void reloadExerciseDetails()
    {
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) parent_activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(current_exercise.getName());
        }

        exercise_description_text_view.setText(current_exercise.getDescription());
    }

    static class ExerciseHistoryCardViewHolder extends RecyclerView.ViewHolder {
        final TextView date_text_view;
        final TextView weight_and_reps_text_view;
        final TextView lift_time_text_view;
        final TextView comment_text_view;

        /**
         * Constrcutor for the view holder.
         * @param view  View that holds the data.
         */
        ExerciseHistoryCardViewHolder(View view) {
            super(view);
            this.date_text_view = (TextView) view.findViewById(R.id.date_text_view);
            this.weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps_text_view);
            this.lift_time_text_view = (TextView) view.findViewById(R.id.lift_time_text_view);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment_text_view);
        }
    }
}
