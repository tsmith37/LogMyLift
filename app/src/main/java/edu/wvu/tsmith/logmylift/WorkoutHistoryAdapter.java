package edu.wvu.tsmith.logmylift;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tmssm on 5/19/2017.
 */

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {
    private ArrayList<Lift> lifts_in_workout_history;

    public WorkoutHistoryAdapter(ArrayList<Lift> lifts_in_workout_history)
    {
        this.lifts_in_workout_history = lifts_in_workout_history;
    }

    public static class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView exercise_name_text_view;
        public TextView weight_and_reps_text_view;
        public TextView comment_text_view;

        public WorkoutHistoryViewHolder(View view) {
            super(view);
            this.exercise_name_text_view = (TextView) view.findViewById(R.id.exercise_name);
            this.weight_and_reps_text_view= (TextView) view.findViewById(R.id.weight_and_reps);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment);
        }
    }

    @Override
    public WorkoutHistoryAdapter.WorkoutHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_layout, null);
        WorkoutHistoryViewHolder view_holder = new WorkoutHistoryViewHolder(view);
        return view_holder;
    }

    @Override
    public void onBindViewHolder(WorkoutHistoryAdapter.WorkoutHistoryViewHolder holder, int position) {
        Lift current_lift = lifts_in_workout_history.get(position);

        holder.exercise_name_text_view.setText(current_lift.getExercise().getName());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    @Override
    public int getItemCount() {
        if (lifts_in_workout_history != null)
        {
            return lifts_in_workout_history.size();
        }
        return 0;
    }
}
