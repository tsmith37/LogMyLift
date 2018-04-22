package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.exercise.ExerciseDetailActivity;
import edu.wvu.tsmith.logmylift.exercise.ExerciseDetailFragment;
import edu.wvu.tsmith.logmylift.lift.Lift;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Tommy Smith on 5/25/2017.
 * Adapter to support placing every lift in a workout into a CardView. This allows for implementation
 * of editing each lift, deleting each lift, adding a new lift, adding an exercise, and copying a lift.
 * Adding a new lift or exercise is not done view the CardView, so these operations must be interfaced
 * with via the calling activity.
 * @author Tommy Smith
 */

class WorkoutHistoryCardAdapter extends RecyclerView.Adapter<WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder>
{
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

    /**
     * Constructs the CardView for the history of the workout.
     * @param parent_activity   The calling activity of the adapter.
     * @param lift_db_helper    Database helper to support the insertion, deletion, copy of lifts.
     * @param recycler_view     The recycler view that the card adapter will populate.
     * @param current_workout   The workout used to populate the card adapter.
     */
    WorkoutHistoryCardAdapter(
            Activity parent_activity,
            LiftDbHelper lift_db_helper,
            RecyclerView recycler_view,
            Workout current_workout)
    {
        this.parent_activity = parent_activity;
        this.lift_db_helper = lift_db_helper;
        this.recycler_view = recycler_view;
        this.current_workout = current_workout;
        if (this.current_workout != null)
        {
            // Get the lifts in the current workout.
            this.current_workout_lifts = this.current_workout.getLifts(lift_db_helper);
        }
        else
        {
            // The current workout doesn't exist, so there are no lifts/
            this.current_workout_lifts = null;
        }
    }

    /**
     * Creates a view holder for the workout history card view.
     * @param parent    The parent view group.
     * @param viewType  The view type.
     * @return  The view holder for the workout history card view.
     */
    @Override
    public WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_card_view, parent, false);

        // Implement the interface required when clicking any position in the workout history card view.
        return new WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder(view, new WorkoutHistoryCardViewHolder.IWorkoutHistoryViewHolderClicks()
        {
            /**
             * Edits the lift at the given position.
             * @param position  The lift's position in the adapter to edit.
             */
            @Override
            public void editLift(int position)
            {
                // Show the dialog to edit the lift.
                showEditLiftDialog(current_workout_lifts.get(position), position);
            }

            /**
             * Copies the lift at the given position.
             * @param position  The lift's position in the adapter to copy.
             */
            @Override
            public void copyLift(int position)
            {
                Lift lift_to_copy = current_workout_lifts.get(position);

                // Show a dialog to add a new lift. The new lift is identical to the lift that is being
                // copied.
                AddLiftParams add_lift_params = new AddLiftParams(
                        parent.getContext(),
                        recycler_view,
                        lift_to_copy);
                showAddLiftDialog(add_lift_params);
            }

            /**
             * Deletes the lift at the given position.
             * @param position  The lift's position in the adapter to delete.
             */
            @Override
            public void deleteLift(int position)
            {
                deleteLiftAtPosition(position);
            }

            /**
             * Go to the information of the exercise based on the lift at the given position.
             * @param position  The lift's position in the adapter. The exercise done by this lift
             *                  will be the information displayed.
             */
            @Override
            public void goToExerciseInfo(int position)
            {
                // Start a new exercise detail activity with the exercise ID of the exercise done by the lift.
                Lift lift_at_position = current_workout_lifts.get(position);
                long exercise_id = lift_at_position.getExercise().getExerciseId();
                Intent intent = new Intent(parent.getContext(), ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailFragment.exercise_id, exercise_id);
                parent.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Bind the card view's holder to the lift at the given position.
     * @param holder    The workout history card view holder.
     * @param position  The position in the adapter to bind.
     */
    @Override
    public void onBindViewHolder(WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder holder, int position)
    {
        // Get the lift at the current position of the adapter.
        position = holder.getAdapterPosition();
        Lift current_lift = current_workout_lifts.get(position);

        // Display the current lift's properties.
        holder.exercise_name_text_view.setText(current_lift.getExercise().getName());
        String weight_and_reps = "Weight: " + Integer.toString(current_lift.getWeight()) + ". Reps: " + Integer.toString(current_lift.getReps()) + ".";
        holder.lift_time_text_view.setText(current_lift.getReadableStartTime());
        holder.weight_and_reps_text_view.setText(weight_and_reps);
        holder.comment_text_view.setText(current_lift.getComment());
    }

    /**
     * Get the number of lifts in the current workout.
     * @return  The number of lifts in the current workout.
     */
    @Override
    public int getItemCount()
    {
        if (current_workout_lifts != null)
        {
            return current_workout_lifts.size();
        }

        // There are no lifts in the workout.
        return 0;
    }

    /**
     * Holds the workout history in a recycler view.
     * @author Tommy Smith
     */
    static class WorkoutHistoryCardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
    {
        final TextView exercise_name_text_view;
        final TextView weight_and_reps_text_view;
        final TextView lift_time_text_view;
        final TextView comment_text_view;
        final ViewFlipper view_flipper;

        /**
         * Constructs the view holder.
         * @param view                      The view that will be populated by the workout history card adapter.
         * @param workout_history_listener  The interface used to edit or interact with each lift in
         *                                  the workout history.
         */
        WorkoutHistoryCardViewHolder(final View view, final IWorkoutHistoryViewHolderClicks workout_history_listener)
        {
            super(view);

            // Set the name, weight & reps, time, and comment text view.
            this.exercise_name_text_view = view.findViewById(R.id.exercise_name_text_view);
            this.weight_and_reps_text_view = view.findViewById(R.id.weight_and_reps_text_view);
            this.lift_time_text_view = view.findViewById(R.id.lift_time_text_view);
            this.comment_text_view = view.findViewById(R.id.comment_text_view);

            // If the user clicks anywhere off the card view, flip back to the exercise name, weight &
            // reps, comment, and time.
            view.setOnLongClickListener(this);
            this.view_flipper = view.findViewById(R.id.card_view_flipper);
            view.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (view_flipper.getDisplayedChild() == 1 && !hasFocus)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });

            // If the user clicks the edit lift button, use the listener to edit the lift at the given position.
            ImageButton edit_lift_button = view.findViewById(R.id.edit_lift_button);
            edit_lift_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    workout_history_listener.editLift(getAdapterPosition());
                    if (view_flipper.getDisplayedChild() == 1)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });

            // If the user clicks the copy lift button, use the listener to copy the lift at the given position.
            ImageButton copy_lift_button = view.findViewById(R.id.copy_lift_button);
            copy_lift_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    workout_history_listener.copyLift(getAdapterPosition());
                    if (view_flipper.getDisplayedChild() == 1)
                    {
                        view_flipper.setDisplayedChild(0);
                    }
                }
            });

            // If the user clicks the exercise info button, use the listener to go to the exercise
            // information of the given lift.
            ImageButton exercise_info_button = view.findViewById(R.id.exercise_info_button);
            exercise_info_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    workout_history_listener.goToExerciseInfo(getAdapterPosition());
                }
            });

            // If the user clicks the delete lift button, use the listener to delete the lift at the given position.
            ImageButton delete_lift_button = view.findViewById(R.id.delete_lift_button);
            delete_lift_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    workout_history_listener.deleteLift(getAdapterPosition());
                }
            });
        }

        // On long click, flip the view from displaying the exercise name, weight & reps, time, and
        // comment to displaying the delete/edit/copy/exercise info buttons.
        @Override
        public boolean onLongClick(final View v)
        {
            v.requestFocus();
            view_flipper.setDisplayedChild(1);
            return false;
        }

        // A holder must implement these interfaces.
        interface IWorkoutHistoryViewHolderClicks
        {
            void editLift(int position);
            void copyLift(int position);
            void deleteLift(int position);
            void goToExerciseInfo(int position);
        }
    }

    /**
     * Allows the user to edit a lift weight, reps, or comment via a popup dialog.
     * @param lift_to_edit             Lift for the user to edit.
     * @param lift_position_in_adapter Position in the adapter of the lift to edit. This is helpful
     *                                 because the adapter can be notified of a change at this position,
     *                                 allowing it to reflect the user changes.
     */
    private void showEditLiftDialog(final Lift lift_to_edit, final int lift_position_in_adapter)
    {
        // Create the dialog to edit the lift.
        LayoutInflater li = LayoutInflater.from(parent_activity);
        final View edit_lift_dialog_view = li.inflate(R.layout.edit_lift_dialog, null);
        AlertDialog.Builder edit_lift_dialog_builder = new AlertDialog.Builder(parent_activity);

        // Reflect the current lift's properties in the edit lift dialog.
        edit_lift_dialog_builder.setTitle(lift_to_edit.getExercise().getName());
        edit_lift_dialog_builder.setView(edit_lift_dialog_view);
        final EditText weight_text = edit_lift_dialog_view.findViewById(R.id.weight_edit_text);
        final EditText reps_text = edit_lift_dialog_view.findViewById(R.id.reps_edit_text);
        final EditText comment_text = edit_lift_dialog_view.findViewById(R.id.comment_edit_text);

        // Make sure the weight is an integer.
        String weight = "";
        try
        {
            weight = Integer.toString(lift_to_edit.getWeight());
        }
        catch (Exception ignored) {}
        weight_text.setText(weight);

        // Make sure the number of reps is an integer.
        String reps = "";
        try
        {
            reps = Integer.toString(lift_to_edit.getReps());
        }
        catch (Exception ignored) {}
        reps_text.setText(reps);

        // Set the comment.
        comment_text.setText(lift_to_edit.getComment());

        // Handle the positive button press.
        edit_lift_dialog_builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Check that the weight is valid (i.e., an integer).
                int weight = -1;
                try
                {
                    weight = Integer.parseInt(weight_text.getText().toString());
                }
                catch (Throwable ignored) {}

                // Check that the reps are valid.
                int reps = -1;
                try
                {
                    reps = Integer.parseInt(reps_text.getText().toString());
                }
                catch (Throwable ignored) {}

                // The weight and the reps must be greater than 0.
                if ((weight > 0) && (reps > 0))
                {
                    // Create the parameters to edit the lift.
                    EditLiftParams edit_lift_params = new EditLiftParams(
                            current_workout_lifts.get(lift_position_in_adapter),
                            weight,
                            reps,
                            comment_text.getText().toString(),
                            lift_position_in_adapter,
                            true);
                    // Start the edit lift operation in the background.
                    new EditLiftOperation().execute(edit_lift_params);
                }
                else
                {
                    // The lift isn't valid.
                    Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
                }

                // Hide the keyboard.
                InputMethodManager input_method_manager = (InputMethodManager) parent_activity.getSystemService(INPUT_METHOD_SERVICE);
                input_method_manager.hideSoftInputFromWindow(edit_lift_dialog_view.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        // Handle the negative button press.
        edit_lift_dialog_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Let the user know that the lift was not updated.
                Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.lift_not_updated, Snackbar.LENGTH_LONG).show();
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
    void showAddLiftDialog(final AddLiftParams params)
    {
        // Create the dialog to add a new lift.
        LayoutInflater li = LayoutInflater.from(params.parent_context);
        final View add_lift_dialog_view = li.inflate(R.layout.add_lift_dialog, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(params.parent_context);
        add_lift_dialog_builder.setTitle(R.string.add_lift);
        add_lift_dialog_builder.setView(add_lift_dialog_view);

        // The current exercise input does not reflect an exercise.
        current_exercise_input_correct = false;

        // Create a new database helper.
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
        exercise_adapter.setFilterQueryProvider(new FilterQueryProvider()
        {
            @Override
            public Cursor runQuery(CharSequence constraint)
            {
                current_exercise_input_correct = false;
                return lift_db_helper.selectExercisesCursor(constraint.toString());
            }
        });

        exercise_adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter()
        {
            @Override
            public CharSequence convertToString(Cursor cursor)
            {
                int column_index = cursor.getColumnIndex(LiftDbHelper.EXERCISE_COLUMN_NAME);
                return cursor.getString(column_index);
            }
        });

        // If default inputs are selected, place them into the dialog now.
        boolean default_inputs_selected =
                (!params.default_exercise_name.equals("") && params.default_weight != -1 && params.default_reps != -1);
        boolean current_exercise_selected = (current_exercise != null);
        if (default_inputs_selected)
        {
            exercise_input.setText(params.default_exercise_name);
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
        clear_exercise_input.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                exercise_input.setText("");
            }
        });

        // If an item is clicked for the exercise input, select that exercise.
        exercise_input.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                changeCurrentExercise(id);
            }
        });

        // Set up the suggested exercises dialog.
        final Button suggested_exercises_button = add_lift_dialog_view.findViewById(R.id.suggested_exercises_button);
        if (suggested_exercises_button != null)
        {
            suggested_exercises_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showSuggestedExercisesDialog(params.parent_context, exercise_input);
                }
            });
        }

        // Handle the positive button press.
        add_lift_dialog_builder.setPositiveButton(R.string.add_lift, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
                        Snackbar exercise_name_invalid = Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.exercise_name_not_valid, Snackbar.LENGTH_LONG);
                        exercise_name_invalid.setAction(R.string.add_exercise, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Exercise.showAddExerciseDialog(
                                        params.parent_context,
                                        parent_activity.findViewById(R.id.add_lift_button),
                                        lift_db_helper,
                                        exercise_input.getText().toString(), new Callable<Long>()
                                        {
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
                    Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Handle the negative button press.
        add_lift_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.lift_not_added, Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_lift_dialog = add_lift_dialog_builder.create();
        add_lift_dialog.show();
    }

    /**
     * Show a dialog with suggested exercises.
     * @param parent_context        The context of the parent activity.
     * @param exercise_text_view    The text view to place the suggested exercise name.
     */
    private void showSuggestedExercisesDialog(final Context parent_context, final AutoCompleteTextView exercise_text_view)
    {
        // Create the suggested exercise dialog.
        LayoutInflater li = LayoutInflater.from(parent_context);
        final View suggested_exercises_dialog_view = li.inflate(R.layout.suggested_exercises_dialog, null);
        AlertDialog.Builder suggested_exercises_dialog_builder = new AlertDialog.Builder(parent_context);
        suggested_exercises_dialog_builder.setTitle(R.string.suggested_exercises_text);
        suggested_exercises_dialog_builder.setView(suggested_exercises_dialog_view);

        // Get the list of similar exercise's IDs.
        ArrayList<Long> similar_exercise_ids = current_workout.getSimilarExercises();
        final ArrayList<Exercise> similar_exercises = new ArrayList<>();

        // Iterate through the similar exercises and add them to the dialog.
        int similar_exercise_index = 0;
        while (true)
        {
            // There are no more similar exercises.
            if (similar_exercise_index >= similar_exercise_ids.size())
            {
                break;
            }

            // Don't show more than 5 similar exercises.
            if (similar_exercise_index >= 5)
            {
                break;
            }

            // Create the similar exercise from the exercise ID.
            Exercise similar_exercise = lift_db_helper.selectExerciseFromExerciseId(similar_exercise_ids.get(similar_exercise_index));
            similar_exercises.add(similar_exercise);
            ++similar_exercise_index;
        }

        // Create a list view from the suggested exercises.
        final ListView suggested_exercises_list_view = suggested_exercises_dialog_view.findViewById(R.id.suggested_exercises_list_view);
        SimilarExercisesListAdapter similar_exercise_list_adapter = new SimilarExercisesListAdapter(parent_context, R.layout.suggested_exercise_detail, similar_exercises);
        suggested_exercises_list_view.setAdapter(similar_exercise_list_adapter);
        final AlertDialog suggested_exercises_dialog = suggested_exercises_dialog_builder.create();

        // When any exercise is clicked, set the text view's input with the name of the exercise.
        suggested_exercises_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
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
        Snackbar delete_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), "Lift deleted.", Snackbar.LENGTH_LONG);
        delete_lift_snackbar.setAction(R.string.undo, new UndoDeleteLiftListener(this, lift_position_in_adapter, lift_to_delete));
        delete_lift_snackbar.addCallback(new Snackbar.Callback()
        {
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

    /**
     * Change the current exercise.
     * @param exercise_id   The exercise ID of the new current exercise.
     */
    private void changeCurrentExercise(long exercise_id)
    {
        current_exercise = lift_db_helper.selectExerciseFromExerciseId(exercise_id);
    }

    /**
     * Change the current exercise to the most recent.
     * @return Returns a 0. This is done because the calling function needs a return value.
     */
    long changeCurrentExerciseToMostRecent()
    {
        current_exercise = lift_db_helper.selectMostRecentExercise();
        return 0;
    }

    /**
     * Parameters needed to add a new lift to the workout.
     * @author  Tommy Smith
     */
    static class AddLiftParams
    {
        // The context of the parent activity.
        final Context parent_context;

        // The recycler view used to show the lifts in the workout.
        final RecyclerView recycler_view;

        // The exercise name to use as a default when the dialog is created. This is used when a lift
        // is copied so that the exercise name is identical to the copied lift.
        final String default_exercise_name;

        // The weight to use as a default when the dialog is created. This is used when a lift is copied
        // so that the weight is identical to the copied lift.
        final int default_weight;

        // The reps to use as a default when the dialog is created. This is used when a lift is copied
        // so that the reps are identical to the copied lift.
        final int default_reps;

        // The comment to use as a default when the dialog is created. This is used when a lift is
        // copied so that the comments are identical to the copied lift.
        final String default_comment;

        /**
         * Construct parameters used to add a new lift, copied from an existing lift.
         * @param parent_context    The context of the parent activity.
         * @param recycler_view     The recycler view used to show the lifts in the workout.
         * @param default_lift      The existing lift to copy.
         */
        AddLiftParams(
                Context parent_context,
                RecyclerView recycler_view,
                Lift default_lift)
        {
            this.parent_context = parent_context;
            this.recycler_view = recycler_view;

            // Copy the name, weight, reps, and comment from the existing lift.
            this.default_exercise_name = default_lift.getExercise().getName();
            this.default_weight = default_lift.getWeight();
            this.default_reps = default_lift.getReps();
            this.default_comment = default_lift.getComment();
        }

        /**
         * Construct parameters used to add a new lift.
         * @param parent_context    The context of the parent activity.
         * @param recycler_view     The recycler view used to show the lifts in the workout.
         */
        AddLiftParams(
                Context parent_context,
                RecyclerView recycler_view)
        {
            this.parent_context = parent_context;
            this.recycler_view = recycler_view;

            // Use a blank string as the exercise name and comment, and -1 as the weight and reps
            // (these are invalid values).
            this.default_exercise_name = "";
            this.default_weight = -1;
            this.default_reps = -1;
            this.default_comment = "";
        }
    }

    /**
     * Parameters needed to edit a lift.
     * @author  Tommy Smith
     */
    private class EditLiftParams
    {
        // The lift to edit.
        final Lift lift;

        // The parameters of the lift after editing.
        final int weight;
        final int reps;
        final String comment;

        // The position of the lift in the workout history card adapter.
        final int lift_position_in_adapter;

        // Whether or not to allow the edit to be undone.
        final boolean allow_undo;

        /**
         * Construct the parameters to edit a lift.
         * @param lift                      The lift to edit.
         * @param weight                    The new weight of the lift.
         * @param reps                      The new reps of the lift.
         * @param comment                   The new comment of the lift.
         * @param lift_position_in_adapter  The position of the lift in the card adapter.
         * @param allow_undo                Whether or not to allow the edit to be undone.
         */
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

    /**
     * An asynchronous operation to edit a lift. The operation is done asynchronously so as not to
     * slow the GUI operation of the recycler view while the database is updating.
     * @author Tommy Smith
     */
    private class EditLiftOperation extends AsyncTask<EditLiftParams, Integer, Boolean>
    {
        // The lift's position in the card adapter.
        int lift_position_in_adapter;

        // The previous weight, reps, and comment for the lift. This is stored so that the edit can be
        // retroactively undone.
        int old_lift_weight;
        int old_lift_reps;
        String old_lift_comment;

        // Whether or not the edit can be undone.
        boolean allow_undo;

        @Override
        protected Boolean doInBackground(EditLiftParams... params)
        {
            lift_position_in_adapter = params[0].lift_position_in_adapter;
            old_lift_weight = params[0].lift.getWeight();
            old_lift_reps = params[0].lift.getReps();
            old_lift_comment = params[0].lift.getComment();
            allow_undo = params[0].allow_undo;
            params[0].lift.update(lift_db_helper, params[0].weight, params[0].reps, params[0].comment);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            if (result)
            {
                notifyItemChanged(lift_position_in_adapter);
                if (allow_undo)
                {
                    // Notify the user that the lift has been updated. Allow the action to be undone.
                    Snackbar edit_lift_snackbar = Snackbar.make(parent_activity.findViewById(R.id.add_lift_button), R.string.lift_updated, Snackbar.LENGTH_LONG);
                    edit_lift_snackbar.setAction(R.string.undo, new UndoEditLiftListener(lift_position_in_adapter, old_lift_weight, old_lift_reps, old_lift_comment));
                    edit_lift_snackbar.show();
                }
            }
        }
    }

    /**
     * Updates the description of the workout.
     * @param description   The description of the workout.
     */
    void setWorkoutDescription(String description)
    {
        // Update the workout's description in the database.
        current_workout.setDescription(lift_db_helper, description);

        // Reload the description shown in the toolbar.
        reloadWorkoutDescription();
    }

    /**
     * Reloads the workout's description in the toolbar.
     */
    void reloadWorkoutDescription()
    {
        CollapsingToolbarLayout appBarLayout = parent_activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null)
        {
            appBarLayout.setTitle(current_workout.getDescription());
        }
    }

    /**
     * Scroll to the top of the workout history.
     * @param recycler_view The recycler view showing the workout history.
     */
    private void goToTop(RecyclerView recycler_view)
    {
        RecyclerView.LayoutManager layout_manager = recycler_view.getLayoutManager();
        layout_manager.smoothScrollToPosition(recycler_view, new RecyclerView.State(), 0);
    }

    /**
     * An interface to allow the user to undo deleting a lift.
     * Note that this interface does not insert the lift back into the database. It is assumed that
     * the delete operation does not remove the lift from the database until the option to undo the
     * delete is declined by the user. This is because inserting the lift back into the database was
     * deemed to produce unwanted slowdowns during testing.
     * @author Tommy Smith
     */
    private class UndoDeleteLiftListener implements View.OnClickListener {
        // The card adapter that the lift has been removed from.
        final WorkoutHistoryCardAdapter current_workout_history;

        // The position in the card adapter of the lift.
        final int lift_position_in_adapter;

        // The lift being deleted.
        final Lift current_lift;

        /**
         * Constructor of the undo listener.
         * @param current_workout_history   The workout history card adapter.
         * @param lift_position_in_adapter  The lift's (previous) position in the given adapter.
         * @param current_lift              The lift being deleted.
         */
        UndoDeleteLiftListener(WorkoutHistoryCardAdapter current_workout_history, int lift_position_in_adapter, Lift current_lift)
        {
            super();
            this.current_workout_history = current_workout_history;
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.current_lift = current_lift;
        }

        /**
         * Undo the deletion of the lift. This is done by adding the lift back into the workout (at
         * its old position) and updating the card adapter.
         * @param v The view calling the listener.
         */
        @Override
        public void onClick(View v)
        {
            // Add the lift back into the array member representing the lifts in the current workout.
            current_workout_lifts.add(lift_position_in_adapter, current_lift);

            // The card adapter is notified about the item being (re-)inserted. This must be done so
            // that the view of the workout history matches what is now stored in memory.
            current_workout_history.notifyItemInserted(lift_position_in_adapter);

            // Updates the workout member to contain the lift again.
            current_workout.reAddLift(current_lift, lift_position_in_adapter);
        }
    }

    /**
     * Provides an interface to undo an edit to a lift.
     * @author Tommy Smith
     */
    private class UndoEditLiftListener implements View.OnClickListener
    {
        // The lift's position in the card adapter.
        final int lift_position_in_adapter;

        // The weight, reps, and comment of the lift before editing.
        final int old_weight;
        final int old_reps;
        final String old_comment;

        /**
         * Constructor for the undo edit lift listener.
         * @param lift_position_in_adapter  The lift's position in the card adapter.
         * @param old_weight                The weight of the lift before editing.
         * @param old_reps                  The reps of the lift before editing.
         * @param old_comment               The comment of the lift before editing.
         */
        UndoEditLiftListener(int lift_position_in_adapter, int old_weight, int old_reps, String old_comment)
        {
            super();
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.old_weight = old_weight;
            this.old_reps = old_reps;
            this.old_comment = old_comment;
        }

        /**
         * Undoes the lift's edit.
         * @param v The view calling the listener.
         */
        @Override
        public void onClick(View v)
        {
            // To undo the edit, just re-edit the lift back to the old parameters.
            EditLiftParams undo_edit_lift_params = new EditLiftParams(
                    current_workout_lifts.get(lift_position_in_adapter),
                    old_weight,
                    old_reps,
                    old_comment,
                    lift_position_in_adapter,
                    false);

            // Asynchronously undo the edit, because it is database-bound. This is done to avoid slowing
            // the GUI.
            new EditLiftOperation().execute(undo_edit_lift_params);
        }
    }
}