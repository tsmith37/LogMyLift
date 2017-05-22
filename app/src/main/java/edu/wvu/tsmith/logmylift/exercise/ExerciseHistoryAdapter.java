package edu.wvu.tsmith.logmylift.exercise;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by tmssm on 5/20/2017.
 * Adapter to represent the history of an exercise. The data itself is represented in an ArrayList
 * of Lift objects, and it will always be shown using the exercise history layout.
 */
class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseHistoryViewHolder> {
    // Data that the adapter should provide access to.
    private ArrayList<Lift> lifts_in_exercise_history;

    /**
     * Constructor of the adapter.
     * @param lifts_in_exercise_history All lifts in the history of the exercise.
     */
    ExerciseHistoryAdapter(ArrayList<Lift> lifts_in_exercise_history)
    {
        this.lifts_in_exercise_history = lifts_in_exercise_history;
    }

    /**
     * Holder used for the view.
     */
    static class ExerciseHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView date_text_view;
        TextView weight_and_reps_text_view;
        TextView comment_text_view;

        /**
         * Constrcutor for the view holder.
         * @param view  View that holds the data.
         */
        ExerciseHistoryViewHolder(View view) {
            super(view);
            this.date_text_view = (TextView) view.findViewById(R.id.date);
            this.weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment);
        }
    }

    @Override
    public ExerciseHistoryAdapter.ExerciseHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_history_layout, null);
        return new ExerciseHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseHistoryAdapter.ExerciseHistoryViewHolder holder, int position) {
        Lift current_lift = lifts_in_exercise_history.get(position);

        holder.date_text_view.setText(current_lift.getReadableStartDate());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    @Override
    public int getItemCount() {
        if (lifts_in_exercise_history != null)
        {
            return  lifts_in_exercise_history.size();
        }
        return 0;
    }
}
