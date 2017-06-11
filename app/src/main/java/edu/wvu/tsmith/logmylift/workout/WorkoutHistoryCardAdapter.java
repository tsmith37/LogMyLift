package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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
 * Created by Tommy Smith on 5/25/2017.
 * Adapter to support placing every lift in a workout into a CardView. This allows for implementation
 * of editing each lift, deleting each lift, adding a new lift, and adding an exercise. Adding a new
 * lift or exercise is not done view the CardView, so these operations must be interfaced with via
 * the calling activity.
 * @author Tommy Smith
 */

class WorkoutHistoryCardAdapter extends RecyclerView.Adapter<WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder> {
    private final Workout current_workout;
    private final Activity parent_activity;

    // Keep track of the current exercise if a new lift has been added. In addition, if the new lift
    // dialog has been used to type in an exercise but an actual exercise hasn't been selected, that
    // will be tracked too so that a new lift isn't added with an incorrect or invalid exercise.
    private Exercise current_exercise;
    private boolean current_exercise_input_correct;

    WorkoutHistoryCardAdapter(Activity parent_activity, Workout current_workout) {
        this.parent_activity = parent_activity;
        this.current_workout = current_workout;
    }

    @Override
    public WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_card_view, parent, false);

        // On a long press, edit the lift.
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

        // Display the current lift's properties.
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
        final TextView exercise_name_text_view;
        final TextView weight_and_reps_text_view;
        final TextView lift_time_text_view;
        final TextView comment_text_view;
        final IWorkoutHistoryViewHolderClicks workout_history_listener;

        WorkoutHistoryCardViewHolder(View view, IWorkoutHistoryViewHolderClicks workout_history_listener) {
            super(view);
            this.exercise_name_text_view = (TextView) view.findViewById(R.id.exercise_name_text_view);
            this.weight_and_reps_text_view = (TextView) view.findViewById(R.id.weight_and_reps_text_view);
            this.lift_time_text_view = (TextView) view.findViewById(R.id.lift_time_text_view);
            this.comment_text_view = (TextView) view.findViewById(R.id.comment_text_view);
            view.setOnLongClickListener(this);
            this.workout_history_listener = workout_history_listener;
        }

        //  On long click, edit the lift.
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
     * Allows the user to edit a lift weight, reps, ro comment via a popup dialog.
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

        // Reflect the current lift's properties in the edit lift dialog.
        edit_lift_dialog_builder.setTitle(lift_to_edit.getExercise().getName());
        edit_lift_dialog_builder.setView(edit_lift_dialog_view);
        final EditText weight_text = (EditText) edit_lift_dialog_view.findViewById(R.id.weight_edit_text);
        final EditText reps_text = (EditText) edit_lift_dialog_view.findViewById(R.id.reps_edit_text);
        final EditText comment_text = (EditText) edit_lift_dialog_view.findViewById(R.id.comment_edit_text);
        String weight = "";
        try {weight = Integer.toString(lift_to_edit.getWeight());} catch (Exception ignored) {}
        weight_text.setText(weight);
        String reps = "";
        try {reps = Integer.toString(lift_to_edit.getReps());} catch (Exception ignored) {}
        reps_text.setText(reps);
        comment_text.setText(lift_to_edit.getComment());

        // Handle the positive button press.
        edit_lift_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int weight = -1;
                try {
                    weight = Integer.parseInt(weight_text.getText().toString());
                } catch (Throwable ignored) {}
                int reps = -1;
                try {
                    reps = Integer.parseInt(reps_text.getText().toString());
                } catch (Throwable ignored) {}

                if ((weight > 0) && (reps > 0)) {
                    Snackbar edit_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_updated, Snackbar.LENGTH_LONG);
                    edit_lift_snackbar.setAction(R.string.undo, new UndoEditLiftListener(lift_position_in_adapter, lift_to_edit, lift_to_edit.getWeight(), lift_to_edit.getReps(), lift_to_edit.getComment()));
                    lift_to_edit.setReps(reps);
                    lift_to_edit.setWeight(weight);
                    lift_to_edit.setComment(comment_text.getText().toString());
                    notifyItemChanged(lift_position_in_adapter);
                    edit_lift_snackbar.show();
                } else {
                    Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
                }

                InputMethodManager input_method_manager = (InputMethodManager) parent_activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                input_method_manager.hideSoftInputFromWindow(edit_lift_dialog_view.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        // Handle the negative button press.
        edit_lift_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_not_updated, Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog edit_dialog = edit_lift_dialog_builder.create();
        edit_dialog.show();
    }

    /**
     * Allows the user to add a lift via a popup dialog. This dialog will allow the user to select
     * an exercise, input a weight, reps, and a comment.
     * @param parent_context    Context to inflate the dialog.
     */
    void showAddLiftDialog(final Context parent_context) {
        LayoutInflater li = LayoutInflater.from(parent_context);
        final View add_lift_dialog_view = li.inflate(R.layout.add_lift_dialog, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(parent_context);
        add_lift_dialog_builder.setTitle(R.string.add_lift);
        add_lift_dialog_builder.setView(add_lift_dialog_view);
        current_exercise_input_correct = false;
        final LiftDbHelper lift_db_helper = new LiftDbHelper(parent_context);

        // Set up the exercise adapter, with nothing in it.
        final SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                parent_context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // When the exercise input is changed, query the database for potential matches. Assume that
        // the current input is not a real exercise. If an exercise is selected from the input, then
        // it will be corrected.
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

        // Fill in the exercise input from the previous exercise, if it exists.
        if (current_exercise != null)
        {
            exercise_input.setText(current_exercise.getName());
        }

        // If an item is clicked for the exercise input, select the current exercise.
        exercise_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeCurrentExercise(lift_db_helper, id);
            }
        });

        // Handle the positive button press.
        add_lift_dialog_builder.setPositiveButton(R.string.add_lift, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check if the exercise input currently contains an actual exercise.
                if (!current_exercise_input_correct)
                {
                    // Check if the inputted text actually does match an exercise.
                    Exercise potential_exercise_from_input = lift_db_helper.selectExerciseFromName(exercise_input.getText().toString());
                    if (potential_exercise_from_input != null)
                    {
                        current_exercise = potential_exercise_from_input;
                        current_exercise_input_correct = true;
                    }
                    else
                    {
                        // Offer to the user via Snackbar to add an exercise named the same as the
                        // input to the add lift dialog.
                        Snackbar exercise_name_invalid = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.exercise_name_not_valid, Snackbar.LENGTH_LONG);
                        exercise_name_invalid.setAction(R.string.add_exercise, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showAddExerciseDialog(parent_context, lift_db_helper, exercise_input.getText().toString());
                            }
                        });
                        exercise_name_invalid.show();
                        return;
                    }
                }

                // Check that the weight and reps are valid for the new lift.
                int weight = -1;
                EditText weight_text = (EditText) add_lift_dialog_view.findViewById(R.id.weight_edit_text);
                try
                {
                    weight = Integer.parseInt(weight_text.getText().toString());
                }
                catch (Throwable ignored) {}

                int reps = -1;
                EditText reps_text = (EditText) add_lift_dialog_view.findViewById(R.id.reps_edit_text);
                try
                {
                    reps = Integer.parseInt(reps_text.getText().toString());
                }
                catch (Throwable ignored) {}

                if ((weight > 0) && (reps > 0))
                {
                    // Add the lift.
                    EditText comment_text = (EditText) add_lift_dialog_view.findViewById(R.id.comment_edit_text);
                    String comment = comment_text.getText().toString();
                    current_workout.addLift(current_exercise, reps, weight, comment);
                    notifyItemInserted(0);
                }
                else
                {
                    Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Handle the negative button press.
        add_lift_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_not_added, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_lift_dialog = add_lift_dialog_builder.create();
        add_lift_dialog.show();
    }

    /**
     * Delete the lift and remove it from the adapter.
     * @param lift_position_in_adapter  Lift position in the adapter to delete.
     */
    void deleteLift(int lift_position_in_adapter)
    {
        // Get the lift to delete.
        final Lift lift_to_delete = current_workout.getLifts().get(lift_position_in_adapter);

        // Remove the lift in memory and notify the adapter.. It is not removed from the database until
        // the Snackbar offering to undo the operation is dismissed. This is done because inserting
        // the lift back in causes all sorts of problems. If the database removal doesn't happen soon
        // enough after exiting the parent activity, this can cause the lift to not actually get removed.
        // But this is an acceptable price to pay for being able to undo the operation, I think, because
        // it is pretty unlikely to happen.
        current_workout.removeLiftInMemory(lift_to_delete.getLiftId());
        notifyItemRemoved(lift_position_in_adapter);

        // Allow for the lift deletion to be reverted.
        Snackbar delete_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), "Lift deleted.", Snackbar.LENGTH_LONG);
        delete_lift_snackbar.setAction(R.string.undo, new UndoDeleteLiftListener(this, lift_position_in_adapter, lift_to_delete));
        delete_lift_snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                // Only actually delete after the Snackbar is dismissed unless it is due to the action.
                // Ie, don't delete it the user pressed undo.
                if (Snackbar.Callback.DISMISS_EVENT_ACTION != event)
                {
                    lift_to_delete.delete();
                }
            }
        });
        delete_lift_snackbar.show();
    }

    /**
     * Allow the user to add an exercise via a popup dialog.
     * @param current_context       Context in which to display the dialog.
     * @param lift_db_helper        LiftDb to actually add the exercise into the database.
     * @param exercise_name_hint    Hint for the exercise name. This will be displayed in the new
     *                              exercise's name field.
     */
    void showAddExerciseDialog(final Context current_context, final LiftDbHelper lift_db_helper, String exercise_name_hint)
    {
        LayoutInflater li = LayoutInflater.from(current_context);
        View add_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder add_exercise_dialog_builder = new AlertDialog.Builder(current_context);
        add_exercise_dialog_builder.setTitle(R.string.create_exercise);
        add_exercise_dialog_builder.setView(add_exercise_dialog_view);
        final EditText exercise_name_text = (EditText) add_exercise_dialog_view.findViewById(R.id.exercise_name_edit_text);
        exercise_name_text.setText(exercise_name_hint);
        final EditText exercise_description_text = (EditText) add_exercise_dialog_view.findViewById(R.id.exercise_description_edit_text);

        // Handle the positive button press.
        add_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (exercise_name_text.getText().toString().isEmpty())
                {
                    Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    new Exercise(lift_db_helper, exercise_name_text.getText().toString(), exercise_description_text.getText().toString());
                    Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), "Exercise added.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Handle the negative button press.
        add_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), "Exercise not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();
        add_exercise_dialog.show();
    }

    // Allow the user to undo a delete lift operation.
    private class UndoDeleteLiftListener implements View.OnClickListener {
        final WorkoutHistoryCardAdapter current_workout_history;
        final int lift_position_in_adapter;
        final Lift current_lift;

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

    // Allow the user to undo a lift edit operation.
    private class UndoEditLiftListener implements View.OnClickListener {
        final int lift_position_in_adapter;
        final Lift current_lift;
        final int old_weight;
        final int old_reps;
        final String old_comment;

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

    // Change the current exercise.
    private void changeCurrentExercise(LiftDbHelper lift_db_helper, long exercise_id)
    {
        current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
    }
}

