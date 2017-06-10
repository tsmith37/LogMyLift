package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.Lift;

/**
 * Created by tmssm on 5/25/2017.
 */

public class WorkoutHistoryCardAdapter extends RecyclerView.Adapter<WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder> {
    private Workout current_workout;
    private Activity parent_activity;
    private Exercise current_exercise;
    private boolean current_exercise_input_correct;

    public WorkoutHistoryCardAdapter(Activity parent_activity, Workout current_workout) {
        this.parent_activity = parent_activity;
        this.current_workout = current_workout;
    }

    @Override
    public WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_card_view, parent, false);
        return new WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder(view, new WorkoutHistoryCardViewHolder.IWorkoutHistoryViewHolderClicks() {
            @Override
            public void editLift(View caller, int position) {
                showEditLiftDialog(current_workout.getLifts().get(position), position);
            }
        });
    }

    @Override
    public void onBindViewHolder(WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Lift current_lift = current_workout.getLifts().get(position);
        holder.exercise_name_text_view.setText(current_lift.getExercise().getName());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    @Override
    public int getItemCount() {
        if (current_workout.getLifts() != null) {
            return current_workout.getLifts().size();
        }
        return 0;
    }

    static class WorkoutHistoryCardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView exercise_name_text_view;
        TextView weight_and_reps_text_view;
        TextView lift_time_text_view;
        TextView comment_text_view;
        IWorkoutHistoryViewHolderClicks workout_history_listener;

        WorkoutHistoryCardViewHolder(View view, IWorkoutHistoryViewHolderClicks workout_history_listener) {
            super(view);
            this.exercise_name_text_view = (TextView) view.findViewById(R.id.exercise_name_text);
            this.weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps);
            this.lift_time_text_view = (TextView) view.findViewById(R.id.lift_time);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment);
            view.setOnLongClickListener(this);
            this.workout_history_listener = workout_history_listener;
        }

        @Override
        public boolean onLongClick(View v) {
            workout_history_listener.editLift(v, this.getAdapterPosition());
            return false;
        }

        interface IWorkoutHistoryViewHolderClicks {
            void editLift(View caller, int position);
        }
    }

    /**
     * ALlows the user to edit a lift weight, reps, ro comment via a popup dialog.
     *
     * @param lift_to_edit             Lift for the user to edit.
     * @param lift_position_in_adapter Position in the adapter of the lift to edit. This is helpful
     *                                 because the adapter can be notified of a change at this position,
     *                                 allowing it to reflect the user changes.
     */
    private void showEditLiftDialog(final Lift lift_to_edit, final int lift_position_in_adapter) {
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
                try {
                    weight = Integer.parseInt(weight_text.getText().toString());
                } catch (Throwable ignored) {
                }
                int reps = -1;
                try {
                    reps = Integer.parseInt(reps_text.getText().toString());
                } catch (Throwable ignored) {
                }

                if ((weight > 0) && (reps > 0)) {
                    Snackbar edit_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift updated.", Snackbar.LENGTH_LONG);
                    edit_lift_snackbar.setAction(R.string.undo_text, new UndoEditLiftListener(lift_position_in_adapter, lift_to_edit, lift_to_edit.getWeight(), lift_to_edit.getReps(), lift_to_edit.getComment()));
                    lift_to_edit.setReps(reps);
                    lift_to_edit.setWeight(weight);
                    lift_to_edit.setComment(comment_text.getText().toString());
                    notifyItemChanged(lift_position_in_adapter);
                    edit_lift_snackbar.show();
                } else {
                    Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift not valid.", Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift not updated.", Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog edit_dialog = edit_lift_dialog_builder.create();
        edit_dialog.show();
    }

    public void showAddLiftDialog(Context parent_context) {
        LayoutInflater li = LayoutInflater.from(parent_context);
        final View add_lift_dialog_view = li.inflate(R.layout.add_lift_dialog_layout, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(parent_context);
        add_lift_dialog_builder.setTitle(R.string.add_lift_button_text);
        add_lift_dialog_builder.setView(add_lift_dialog_view);
        current_exercise_input_correct = false;
        final LiftDbHelper lift_db_helper = new LiftDbHelper(parent_context);

        final SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                parent_context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        exercise_adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                current_exercise_input_correct = false;
                return lift_db_helper.selectExercisesCursor(constraint.toString());
            }
        });

        exercise_adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                int column_index = cursor.getColumnIndex(LiftDbHelper.EXERCISE_COLUMN_NAME);
                return cursor.getString(column_index);
            }
        });

        final AutoCompleteTextView exercise_input = (AutoCompleteTextView) add_lift_dialog_view.findViewById(R.id.exercise_input);
        exercise_input.setAdapter(exercise_adapter);

        exercise_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeCurrentExercise(lift_db_helper, id);
            }
        });

        // Handle the positive button press.
        add_lift_dialog_builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!current_exercise_input_correct) {
                    Exercise potential_exercise_from_input = lift_db_helper.selectExerciseFromName(exercise_input.getText().toString());
                    if (potential_exercise_from_input != null) {
                        current_exercise = potential_exercise_from_input;
                        current_exercise_input_correct = true;
                    } else {
                        Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                int weight = -1;
                EditText weight_text = (EditText) add_lift_dialog_view.findViewById(R.id.weight_input);
                try {
                    weight = Integer.parseInt(weight_text.getText().toString());
                } catch (Throwable ignored) {
                }
                int reps = -1;
                EditText reps_text = (EditText) add_lift_dialog_view.findViewById(R.id.reps_input);
                try {
                    reps = Integer.parseInt(reps_text.getText().toString());
                } catch (Throwable ignored) {
                }

                if ((weight > 0) && (reps > 0)) {
                    EditText comment_text = (EditText) add_lift_dialog_view.findViewById(R.id.comment_input);
                    String comment = comment_text.getText().toString();
                    current_workout.addLift(current_exercise, reps, weight, comment);
                    notifyItemInserted(0);
                } else {
                    Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift not valid.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Handle the negative button press.
        add_lift_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_lift_dialog = add_lift_dialog_builder.create();
        add_lift_dialog.show();
    }

    public void deleteLift(int lift_position_in_adapter)
    {
        final Lift lift_to_delete = current_workout.getLifts().get(lift_position_in_adapter);
        current_workout.removeLiftInMemory(lift_to_delete.getLiftId());
        notifyItemRemoved(lift_position_in_adapter);
        Snackbar delete_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift deleted.", Snackbar.LENGTH_LONG);
        delete_lift_snackbar.setAction(R.string.undo_text, new UndoDeleteLiftListener(this, lift_position_in_adapter, lift_to_delete));
        delete_lift_snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                if (Snackbar.Callback.DISMISS_EVENT_ACTION != event)
                {
                    lift_to_delete.delete();
                }
            }
        });
        delete_lift_snackbar.show();
    }

    private class UndoDeleteLiftListener implements View.OnClickListener {
        WorkoutHistoryCardAdapter current_workout_history;
        int lift_position_in_adapter;
        Lift current_lift;

        UndoDeleteLiftListener(WorkoutHistoryCardAdapter current_workout_history, int lift_position_in_adapter, Lift current_lift) {
            super();
            this.current_workout_history = current_workout_history;
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.current_lift = current_lift;
        }

        @Override
        public void onClick(View v) {
            current_workout_history.notifyItemInserted(lift_position_in_adapter);
            current_workout.reAddLift(current_lift, lift_position_in_adapter);
        }
    }

    private class UndoEditLiftListener implements View.OnClickListener {
        int lift_position_in_adapter;
        Lift current_lift;
        int old_weight;
        int old_reps;
        String old_comment;

        UndoEditLiftListener(int lift_position_in_adapter, Lift current_lift, int old_weight, int old_reps, String old_comment) {
            super();
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.current_lift = current_lift;
            this.old_weight = old_weight;
            this.old_reps = old_reps;
            this.old_comment = old_comment;
        }

        @Override
        public void onClick(View v) {
            current_lift.setWeight(old_weight);
            current_lift.setReps(old_reps);
            current_lift.setComment(old_comment);
            notifyItemChanged(lift_position_in_adapter);
        }
    }

    private void changeCurrentExercise(LiftDbHelper lift_db_helper, long exercise_id)
    {
        current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
    }
}

