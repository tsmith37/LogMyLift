package edu.wvu.tsmith.logmylift;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tmssm on 5/20/2017.
 */

public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseHistoryViewHolder> {
    private ArrayList<Lift> lifts_in_exercise_history;

    public ExerciseHistoryAdapter(ArrayList<Lift> lifts_in_exercise_history)
    {
        this.lifts_in_exercise_history = lifts_in_exercise_history;
    }

    public static class ExerciseHistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView date_text_view;
        public TextView weight_and_reps_text_view;
        public TextView comment_text_view;

        public ExerciseHistoryViewHolder(View view) {
            super(view);
            this.date_text_view = (TextView) view.findViewById(R.id.date);
            this.weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment);
        }
    }

    @Override
    public ExerciseHistoryAdapter.ExerciseHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_history_layout, null);
        ExerciseHistoryViewHolder view_holder = new ExerciseHistoryViewHolder(view);
        return view_holder;
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
