package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.lift.Lift;

import static android.content.Context.INPUT_METHOD_SERVICE;

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
    private final ArrayList<Lift> current_workout_lifts;
    private final Activity parent_activity;
    private final LiftDbHelper lift_db_helper;
    private final RecyclerView recycler_view;

    // Keep track of the current exercise if a new lift has been added. In addition, if the new lift
    // dialog has been used to type in an exercise but an actual exercise hasn't been selected, that
    // will be tracked too so that a new lift isn't added with an incorrect or invalid exercise.
    private Exercise current_exercise;
    private boolean current_exercise_input_correct;

    WorkoutHistoryCardAdapter(Activity parent_activity, LiftDbHelper lift_db_helper, RecyclerView recycler_view, Workout current_workout) {
        this.parent_activity = parent_activity;
        this.lift_db_helper = lift_db_helper;
        this.recycler_view = recycler_view;
        this.current_workout = current_workout;
        if (this.current_workout != null)
        {
            this.current_workout_lifts = this.current_workout.getLifts(lift_db_helper);
        }
        else
        {
            this.current_workout_lifts = null;
        }
    }

    @Override
    public WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_card_view, parent, false);

        // On a long press, edit the lift.
        return new WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder(view, new WorkoutHistoryCardViewHolder.IWorkoutHistoryViewHolderClicks() {
            @Override
            public void editLift(int position) { showEditLiftDialog(current_workout_lifts.get(position), position); }

            @Override
            public void copyLift(int position)
            {
                Lift lift_to_copy = current_workout_lifts.get(position);
                AddLiftParams add_lift_params = new AddLiftParams(
                        parent.getContext(),
                        recycler_view,
                        lift_to_copy);
                showAddLiftDialog(add_lift_params);
            }

            @Override
            public void deleteLift(int position) {
                deleteLiftAtPosition(position);
            }
        });
    }

    @Override
    public void onBindViewHolder(WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Lift current_lift = current_workout_lifts.get(position);

        // Display the current lift's properties.
        holder.exercise_name_text_view.setText(current_lift.getExercise().getName());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    @Override
    public int getItemCount()
    {
        if (current_workout_lifts != null)
        {
            return current_workout_lifts.size();
        }
        return 0;
    }

    static class WorkoutHistoryCardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        final TextView exercise_name_text_view;
        final TextView weight_and_reps_text_view;
        final TextView lift_time_text_view;
        final TextView comment_text_view;
        final ViewFlipper view_flipper;

        WorkoutHistoryCardViewHolder(final View view, final IWorkoutHistoryViewHolderClicks workout_history_listener) {
            super(view);
            this.exercise_name_text_view = view.findViewById(R.id.exercise_name_text_view);
            this.weight_and_reps_text_view = view.findViewById(R.id.weight_and_reps_text_view);
            this.lift_time_text_view = view.findViewById(R.id.lift_time_text_view);
            this.comment_text_view = view.findViewById(R.id.comment_text_view);
            view.setOnLongClickListener(this);

            this.view_flipper = view.findViewById(R.id.card_view_flipper);
            ImageButton edit_lift_button = view.findViewById(R.id.edit_lift_button);
            edit_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workout_history_listener.editLift(getAdapterPosition());
                    if (view_flipper.getDisplayedChild() == 1)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });

            ImageButton copy_lift_button = view.findViewById(R.id.copy_lift_button);
            copy_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workout_history_listener.copyLift(getAdapterPosition());
                    if (view_flipper.getDisplayedChild() == 1)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });

            ImageButton delete_lift_button = view.findViewById(R.id.delete_lift_button);
            delete_lift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workout_history_listener.deleteLift(getAdapterPosition());
                }
            });

            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (view_flipper.getDisplayedChild() == 1 && !hasFocus)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });
        }

        //  On long click, edit the lift.
        @Override
        public boolean onLongClick(final View v) {
            v.requestFocus();
            view_flipper.setDisplayedChild(1);
            return false;
        }

        interface IWorkoutHistoryViewHolderClicks {
            void editLift(int position);
            void copyLift(int position);
            void deleteLift(int position);
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
        final EditText weight_text = edit_lift_dialog_view.findViewById(R.id.weight_edit_text);
        final EditText reps_text = edit_lift_dialog_view.findViewById(R.id.reps_edit_text);
        final EditText comment_text = edit_lift_dialog_view.findViewById(R.id.comment_edit_text);
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
                    EditLiftParams edit_lift_params = new EditLiftParams(current_workout_lifts.get(lift_position_in_adapter), weight, reps, comment_text.getText().toString(), lift_position_in_adapter, true);
                    new EditLiftOperation().execute(edit_lift_params);
                } else {
                    Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
                }

                InputMethodManager input_method_manager = (InputMethodManager) parent_activity.getSystemService(INPUT_METHOD_SERVICE);
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
     * @param params    Parameters to use for the add lift dialog.
     */
    void showAddLiftDialog(final AddLiftParams params) {
        LayoutInflater li = LayoutInflater.from(params.parent_context);
        final View add_lift_dialog_view = li.inflate(R.layout.add_lift_dialog, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(params.parent_context);
        add_lift_dialog_builder.setTitle(R.string.add_lift);
        add_lift_dialog_builder.setView(add_lift_dialog_view);
        current_exercise_input_correct = false;
        final LiftDbHelper lift_db_helper = new LiftDbHelper(params.parent_context);

        // Set up the exercise adapter, with nothing in it.
        final SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                params.parent_context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // Find the exercise text input.
        final AutoCompleteTextView exercise_input = add_lift_dialog_view.findViewById(R.id.exercise_input);
        exercise_input.setAdapter(exercise_adapter);

        // Find the weight text input.
        final EditText weight_text = add_lift_dialog_view.findViewById(R.id.weight_edit_text);

        // Find the reps text input.
        final EditText reps_text = add_lift_dialog_view.findViewById(R.id.reps_edit_text);

        // Find the comment text input.
        final EditText comment_text = add_lift_dialog_view.findViewById(R.id.comment_edit_text);

        // When the exercise input is changed, query the database for potential matches. Assume that
        // the no exercise is currently selected. If an exercise is defaulted, already selected, or
        // selected from the input, then it will be corrected.
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

        // If default inputs are selected, place them into the dialog now.
        boolean default_inputs_selected =
                (!params.default_lift_name.equals("") && params.default_weight != -1 && params.default_reps != -1);
        boolean current_exercise_selected = (current_exercise != null);
        if (default_inputs_selected)
        {
            exercise_input.setText(params.default_lift_name);
            weight_text.setText(Integer.toString(params.default_weight));
            reps_text.setText(Integer.toString(params.default_reps));
            comment_text.setText(params.default_comment);

            // The current exercise isn't actually selected yet, so specify that the current exercise
            // input is not correct.
            current_exercise_input_correct = false;

            // Don't focus on the exercise input.
            try
            {
                weight_text.requestFocus();
            }
            catch (Throwable ignored) {}
        }
        // If there is an exercise already selected, place it into the dialog now.
        else if (current_exercise_selected)
        {
            exercise_input.setText(current_exercise.getName());
            current_exercise_input_correct = true;

            // Don't focus on the exercise input.
            try
            {
                weight_text.requestFocus();
            }
            catch (Throwable ignored) {}
        }

        // Set the clear exercise button to clear the exercise input.
        ImageButton clear_exercise_input = add_lift_dialog_view.findViewById(R.id.clear_exercise_button);
        clear_exercise_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exercise_input.setText("");
            }
        });

        // If an item is clicked for the exercise input, select that exercise.
        exercise_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeCurrentExercise(id);
            }
        });

        // Set up the suggested exercises dialog.
        final Button suggested_exercises_button = add_lift_dialog_view.findViewById(R.id.suggested_exercises_button);
        if (suggested_exercises_button != null)
        {
            suggested_exercises_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSuggestedExercisesDialog(params.parent_context, exercise_input);
                }
            });
        }

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
                                Exercise.showAddExerciseDialog(
                                        params.parent_context,
                                        parent_activity.findViewById(R.id.add_lift_button),
                                        lift_db_helper,
                                        exercise_input.getText().toString(), new Callable<Long>() {
                                    public Long call()
                                    {
                                        return changeCurrentExerciseToMostRecent();
                                    }
                                });
                            }
                        });
                        exercise_name_invalid.show();
                        return;
                    }
                }

                // Check that the weight and reps are valid for the new lift.
                int weight = -1;
                try
                {
                    weight = Integer.parseInt(weight_text.getText().toString());
                }
                catch (Throwable ignored) {}

                int reps = -1;
                try
                {
                    reps = Integer.parseInt(reps_text.getText().toString());
                }
                catch (Throwable ignored) {}

                if ((weight > 0) && (reps > 0))
                {
                    // Add the lift.
                    String comment = comment_text.getText().toString();
                    current_workout_lifts.add(0, current_workout.addLift(lift_db_helper, current_exercise, reps, weight, comment));
                    notifyItemInserted(0);
                    goToTop(recycler_view);
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

    private void showSuggestedExercisesDialog(final Context parent_context, final AutoCompleteTextView exercise_text_view)
    {
        LayoutInflater li = LayoutInflater.from(parent_context);
        final View suggested_exercises_dialog_view = li.inflate(R.layout.suggested_exercises_dialog, null);
        AlertDialog.Builder suggested_exercises_dialog_builder = new AlertDialog.Builder(parent_context);
        suggested_exercises_dialog_builder.setTitle(R.string.suggested_exercises_text);
        suggested_exercises_dialog_builder.setView(suggested_exercises_dialog_view);

        ArrayList<Long> similar_exercise_ids = current_workout.getSimilarExercises();
        final ArrayList<Exercise> similar_exercises = new ArrayList<>();
        int similar_exercise_index = 0;
        while (true)
        {
            if (similar_exercise_index >= similar_exercise_ids.size())
            {
                break;
            }

            if (similar_exercise_index >= 5)
            {
                break;
            }

            Exercise similar_exercise = lift_db_helper.selectExerciseFromExerciseId(similar_exercise_ids.get(similar_exercise_index));
            similar_exercises.add(similar_exercise);
            ++similar_exercise_index;
        }

        final ListView suggested_exercises_list_view = suggested_exercises_dialog_view.findViewById(R.id.suggested_exercises_list_view);
        SimilarExercisesListAdapter similar_exercise_list_adapter = new SimilarExercisesListAdapter(parent_context, R.layout.suggested_exercise_detail, similar_exercises);
        suggested_exercises_list_view.setAdapter(similar_exercise_list_adapter);
        final AlertDialog suggested_exercises_dialog = suggested_exercises_dialog_builder.create();
        suggested_exercises_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                exercise_text_view.setText(similar_exercises.get(position).getName());
                exercise_text_view.dismissDropDown();
                suggested_exercises_dialog.cancel();
            }
        });
        suggested_exercises_dialog.show();
    }

    /**
     * Delete the lift and remove it from the adapter.
     * @param lift_position_in_adapter  Lift position in the adapter to delete.
     */
    private void deleteLiftAtPosition(int lift_position_in_adapter)
    {
        // Get the lift to delete.
        final Lift lift_to_delete = current_workout_lifts.get(lift_position_in_adapter);

        // Remove the lift in memory and notify the adapter. It is not removed from the database until
        // the Snackbar offering to undo the operation is dismissed. This is done because inserting
        // the lift back in causes all sorts of problems. If the database removal doesn't happen soon
        // enough after exiting the parent activity, this can cause the lift to not actually get removed.
        // But this is an acceptable price to pay for being able to undo the operation, I think, because
        // it is pretty unlikely to happen.
        current_workout.removeLiftInMemory(lift_to_delete.getLiftId());
        current_workout_lifts.remove(lift_position_in_adapter);
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
                    lift_to_delete.delete(lift_db_helper);
                }
            }
        });
        delete_lift_snackbar.show();
    }

    // Change the current exercise.
    private void changeCurrentExercise(long exercise_id)
    {
        current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
    }

    long changeCurrentExerciseToMostRecent()
    {
        current_exercise = lift_db_helper.selectMostRecentExercise();
        return 0;
    }

    static class AddLiftParams
    {
        final Context parent_context;
        final RecyclerView recycler_view;
        final String default_lift_name;
        final int default_weight;
        final int default_reps;
        final String default_comment;

        AddLiftParams(
                Context parent_context,
                RecyclerView recycler_view,
                Lift default_lift)
        {
            this.parent_context = parent_context;
            this.recycler_view = recycler_view;
            this.default_lift_name = default_lift.getExercise().getName();
            this.default_weight = default_lift.getWeight();
            this.default_reps = default_lift.getReps();
            this.default_comment = default_lift.getComment();
        }

        AddLiftParams(
                Context parent_context,
                RecyclerView recycler_view)
        {
            this.parent_context = parent_context;
            this.recycler_view = recycler_view;
            this.default_lift_name = "";
            this.default_weight = -1;
            this.default_reps = -1;
            this.default_comment = "";
        }
    }
    private class EditLiftParams {
        final Lift lift;
        final int weight;
        final int reps;
        final String comment;
        final int lift_position_in_adapter;
        final boolean allow_undo;

        EditLiftParams(Lift lift, int weight, int reps, String comment, int lift_position_in_adapter, boolean allow_undo)
        {
            this.lift = lift;
            this.weight = weight;
            this.reps = reps;
            this.comment = comment;
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.allow_undo = allow_undo;
        }
    }

    private class EditLiftOperation extends AsyncTask<EditLiftParams, Integer, Boolean>
    {
        int lift_position_in_adapter;
        int old_lift_weight;
        int old_lift_reps;
        String old_lift_comment;
        boolean allow_undo;

        @Override
        protected Boolean doInBackground(EditLiftParams... params) {
            lift_position_in_adapter = params[0].lift_position_in_adapter;
            old_lift_weight = params[0].lift.getWeight();
            old_lift_reps = params[0].lift.getReps();
            old_lift_comment = params[0].lift.getComment();
            allow_undo = params[0].allow_undo;
            params[0].lift.update(lift_db_helper, params[0].weight, params[0].reps, params[0].comment);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result)
            {
                notifyItemChanged(lift_position_in_adapter);
                if (allow_undo)
                {
                    Snackbar edit_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.edit_workout_button), R.string.lift_updated, Snackbar.LENGTH_LONG);
                    edit_lift_snackbar.setAction(R.string.undo, new UndoEditLiftListener(lift_position_in_adapter, old_lift_weight, old_lift_reps, old_lift_comment));
                    edit_lift_snackbar.show();
                }
            }
        }
    }

    void setWorkoutDescription(String description)
    {
        current_workout.setDescription(lift_db_helper, description);
        reloadWorkoutDescription();
    }

    void reloadWorkoutDescription()
    {
        CollapsingToolbarLayout appBarLayout = parent_activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(current_workout.getDescription());
        }
    }

    private void goToTop(RecyclerView recycler_view)
    {
        RecyclerView.LayoutManager layout_manager = recycler_view.getLayoutManager();
        layout_manager.smoothScrollToPosition(recycler_view, new RecyclerView.State(), 0);
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
            current_workout_lifts.add(lift_position_in_adapter, current_lift);
            current_workout_history.notifyItemInserted(lift_position_in_adapter);
            current_workout.reAddLift(current_lift, lift_position_in_adapter);
        }
    }

    // Allow the user to undo a lift edit operation.
    private class UndoEditLiftListener implements View.OnClickListener {
        final int lift_position_in_adapter;
        final int old_weight;
        final int old_reps;
        final String old_comment;

        UndoEditLiftListener(int lift_position_in_adapter, int old_weight, int old_reps, String old_comment) {
            super();
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.old_weight = old_weight;
            this.old_reps = old_reps;
            this.old_comment = old_comment;
        }

        @Override
        public void onClick(View v) {
            EditLiftParams undo_edit_lift_params = new EditLiftParams(
                    current_workout_lifts.get(lift_position_in_adapter),
                    old_weight,
                    old_reps,
                    old_comment,
                    lift_position_in_adapter,
                    false);
            new EditLiftOperation().execute(undo_edit_lift_params);
        }
    }
}

