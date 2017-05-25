package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.lift.Lift;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by tmssm on 5/19/2017.
 * The adapter linking the data from a workout to the user (specifically, via a RecyclerView). This
 * holds every lift in a workout and displays it via the current workout layout. It offers the option
 * to edit any of the lifts via a dialog on click, and delete any of the lifts via a long click.
 */

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {
    private ArrayList<Lift> lifts_in_workout_history;
    private Activity parent_activity;

    public WorkoutHistoryAdapter(Activity parent_activity, ArrayList<Lift> lifts_in_workout_history)
    {
        this.parent_activity = parent_activity;
        this.lifts_in_workout_history = lifts_in_workout_history;
    }

    /**
     * Holder of the workout history view.
     */
    static class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView exercise_name_text_view;
        TextView weight_and_reps_text_view;
        TextView lift_time_text_view;
        TextView comment_text_view;
        IWorkoutHistoryViewHolderClicks workout_history_listener;
        WorkoutHistoryAdapter workout_history_adapter;

        WorkoutHistoryViewHolder(View view, WorkoutHistoryAdapter workout_history_adapter, IWorkoutHistoryViewHolderClicks workout_history_listener) {
            super(view);
            this.workout_history_listener = workout_history_listener;
            this.exercise_name_text_view = (TextView) view.findViewById(R.id.exercise_name);
            this.exercise_name_text_view.setOnClickListener(this);
            this.exercise_name_text_view.setOnLongClickListener(this);
            this.weight_and_reps_text_view= (TextView) view.findViewById(R.id.weight_and_reps);
            this.weight_and_reps_text_view.setOnClickListener(this);
            this.weight_and_reps_text_view.setOnLongClickListener(this);
            this.lift_time_text_view= (TextView) view.findViewById(R.id.lift_time);
            this.lift_time_text_view.setOnClickListener(this);
            this.lift_time_text_view.setOnLongClickListener(this);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment);
            this.comment_text_view.setOnClickListener(this);
            this.comment_text_view.setOnLongClickListener(this);
            this.workout_history_adapter = workout_history_adapter;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            workout_history_listener.editLift(v, this.getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v)
        {
            workout_history_listener.deleteLift(v, this.workout_history_adapter, this.getAdapterPosition());
            return false;
        }

        interface IWorkoutHistoryViewHolderClicks
        {
            void editLift(View caller, int position);
            void deleteLift(View caller, WorkoutHistoryAdapter workout_history_adapter, int position);
        }
    }

    @Override
    public WorkoutHistoryAdapter.WorkoutHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_layout, parent, false);
        return new WorkoutHistoryViewHolder(view, this, new WorkoutHistoryViewHolder.IWorkoutHistoryViewHolderClicks() {
            @Override
            public void editLift(View caller, int position)
            {
                showEditLiftDialog(lifts_in_workout_history.get(position), position);
            }

            @Override
            public void deleteLift(View caller, WorkoutHistoryAdapter workout_history_adapter, int position)
            {
                showDeleteLiftDialog(workout_history_adapter, lifts_in_workout_history.get(position), position);
            }
        });
    }

    @Override
    public void onBindViewHolder(WorkoutHistoryAdapter.WorkoutHistoryViewHolder holder, final int position) {
        Lift current_lift = lifts_in_workout_history.get(position);
        holder.exercise_name_text_view.setText(current_lift.getExercise().getName());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());
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

    /**
     * ALlows the user to edit a lift weight, reps, ro comment via a popup dialog.
     * @param lift_to_edit              Lift for the user to edit.
     * @param lift_position_in_adapter  Position in the adapter of the lift to edit. This is helpful
     *                                  because the adapter can be notified of a change at this position,
     *                                  allowing it to reflect the user changes.
     */
    private void showEditLiftDialog(final Lift lift_to_edit, final int lift_position_in_adapter)
    {
        LayoutInflater li = LayoutInflater.from(parent_activity);
        final View edit_lift_dialog_view = li.inflate(R.layout.edit_lift_dialog, null);
        AlertDialog.Builder edit_lift_dialog_builder = new AlertDialog.Builder(parent_activity);
        edit_lift_dialog_builder.setTitle(lift_to_edit.getExercise().getName());
        edit_lift_dialog_builder.setView(edit_lift_dialog_view);
        final EditText weight_text = (EditText) edit_lift_dialog_view.findViewById(R.id.weight_input);
        final EditText reps_text = (EditText) edit_lift_dialog_view.findViewById(R.id.reps_input);
        final EditText comment_text = (EditText) edit_lift_dialog_view.findViewById(R.id.comment_input);
        weight_text.setText(Integer.toString(lift_to_edit.getWeight()));
        reps_text.setText(Integer.toString(lift_to_edit.getReps()));
        comment_text.setText(lift_to_edit.getComment());

        // Handle the positive button press.
        edit_lift_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int weight = -1;
                try {weight = Integer.parseInt(weight_text.getText().toString());}
                catch (Throwable ignored) {}
                int reps = -1;
                try {reps = Integer.parseInt(reps_text.getText().toString());}
                catch (Throwable ignored) {}

                if ((weight > 0) && (reps > 0))
                {
                    lift_to_edit.setReps(reps);
                    lift_to_edit.setWeight(weight);
                    lift_to_edit.setComment(comment_text.getText().toString());
                    notifyItemChanged(lift_position_in_adapter);
                    Snackbar.make(parent_activity.findViewById(R.id.exercise_name), "Lift updated.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Snackbar.make(parent_activity.findViewById(R.id.exercise_name), "Lift not valid.", Snackbar.LENGTH_LONG).show();
                }
                InputMethodManager input_method_manager = (InputMethodManager) parent_activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                input_method_manager.hideSoftInputFromWindow(edit_lift_dialog_view.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        // Handle the negative button press.
        edit_lift_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.exercise_name), "Lift not updated.", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog edit_dialog = edit_lift_dialog_builder.create();
        edit_dialog.show();
    }

    /**
     * Allow the user to delete a lift. Shows a confirmation dialog and then handles the deleting of
     * the lift from the workout.
     * @param lift_to_delete            Lift to delete.
     * @param lift_position_in_adapter  The position of the lift to delete in the adapter. This allows
     *                                  the lift to be graphically removed from the user.
     * @return                          Not really sure what the point of this is...always true.
     */
    private boolean showDeleteLiftDialog(final WorkoutHistoryAdapter workout_history_adapter, final Lift lift_to_delete, final int lift_position_in_adapter)
    {
        AlertDialog.Builder delete_lift_dialog_builder = new AlertDialog.Builder(parent_activity);
        delete_lift_dialog_builder.setTitle("Delete Lift");
        delete_lift_dialog_builder.setMessage("Are you sure you want to delete this lift?");
        delete_lift_dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lift_to_delete.delete();
                Snackbar delete_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.exercise_name), "Lift deleted.", Snackbar.LENGTH_LONG);
                delete_lift_snackbar.setAction(R.string.undo_text, new UndoDeleteLiftListener(workout_history_adapter, lift_to_delete));
                delete_lift_snackbar.show();
                notifyItemRemoved(lift_position_in_adapter);
                dialog.dismiss();
            }
        });

        delete_lift_dialog_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.exercise_name), "Lift not deleted.", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog delete_dialog = delete_lift_dialog_builder.create();
        delete_dialog.show();
        return true;
    }

    private class UndoDeleteLiftListener implements View.OnClickListener
    {
        Lift current_lift;
        WorkoutHistoryAdapter workout_history_adapter;

        UndoDeleteLiftListener(WorkoutHistoryAdapter workout_history_adapter, Lift current_lift)
        {
            super();
            this.current_lift = current_lift;
            this.workout_history_adapter = workout_history_adapter;
        }

        @Override
        public void onClick(View v)
        {
            LiftDbHelper lift_db_helper = new LiftDbHelper(parent_activity.getBaseContext());
            lift_db_helper.insertLift(current_lift);
            workout_history_adapter.notifyDataSetChanged();
        }
    }
}
