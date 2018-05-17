package edu.wvu.tsmith.logmylift.exercise;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
    private ArrayList<Lift> current_exercise_lifts;
    private final Activity parent_activity;
    private final LiftDbHelper lift_db_helper;

    ExerciseHistoryCardAdapter(Activity parent_activity, LiftDbHelper lift_db_helper, Exercise current_exercise)
    {
        this.parent_activity = parent_activity;
        this.lift_db_helper = lift_db_helper;
        this.current_exercise = current_exercise;

        if (this.current_exercise != null)
        {
            reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.DATE_DESC);
        }
        else
        {
            this.current_exercise_lifts = null;
        }
    }

    public void reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder order)
    {
        SelectExerciseHistoryParams select_exercise_history_params = new SelectExerciseHistoryParams(this.current_exercise, order);
        this.current_exercise_lifts = this.lift_db_helper.selectExerciseHistoryLifts(select_exercise_history_params);
        notifyDataSetChanged();
    }

    @Override
    public ExerciseHistoryCardAdapter.ExerciseHistoryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_history_card_view, null);
        return new ExerciseHistoryCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseHistoryCardAdapter.ExerciseHistoryCardViewHolder holder, int position) {
        Lift current_lift = current_exercise_lifts.get(position);

        holder.date_text_view.setText(current_lift.getReadableStartDate());
        String weight = String.format("Weight: %d", current_lift.getWeight());
        holder.weight_text_view.setText(weight);
        String reps = String.format("Reps: %d", current_lift.getReps());
        holder.reps_text_view.setText(reps);
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());

        String comment = current_lift.getComment();
        if (comment.equals(""))
        {
            holder.comment_text_view.setVisibility(View.GONE);
        }
        else
        {
            holder.comment_text_view.setText(comment);
        }

        if (current_exercise.getMaxLiftId() != current_lift.getLiftId())
        {
            holder.max_effort_text_view.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (current_exercise_lifts != null)
        {
            return current_exercise_lifts.size();
        }
        return 0;
    }

    void reloadExerciseDetails()
    {
        Toolbar toolbar = parent_activity.findViewById(R.id.detail_toolbar);
        if (toolbar != null)
        {
            toolbar.setTitle(current_exercise.getName());
        }
    }

    static class ExerciseHistoryCardViewHolder extends RecyclerView.ViewHolder {
        final TextView date_text_view;
        final TextView weight_text_view;
        final TextView reps_text_view;
        final TextView lift_time_text_view;
        final TextView comment_text_view;
        final TextView max_effort_text_view;

        /**
         * Constrcutor for the view holder.
         * @param view  View that holds the data.
         */
        ExerciseHistoryCardViewHolder(View view) {
            super(view);
            this.date_text_view = view.findViewById(R.id.date_text_view);
            this.weight_text_view = view.findViewById(R.id.weight_text_view);
            this.reps_text_view = view.findViewById(R.id.reps_text_view);
            this.lift_time_text_view = view.findViewById(R.id.lift_time_text_view);
            this.comment_text_view = view.findViewById(R.id.comment_text_view);
            this.max_effort_text_view = view.findViewById(R.id.max_effort_text_view);
        }
    }
}
