package edu.wvu.tsmith.logmylift.workout;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.ExerciseDetailActivity;
import edu.wvu.tsmith.logmylift.exercise.ExerciseDetailFragment;
import edu.wvu.tsmith.logmylift.lift.AddLiftDialog;
import edu.wvu.tsmith.logmylift.lift.AddLiftParams;
import edu.wvu.tsmith.logmylift.lift.EditLiftDialog;
import edu.wvu.tsmith.logmylift.lift.Lift;

/**
 * Created by Tommy Smith on 5/25/2017.
 * Adapter to support placing every lift in a workout into a CardView. This allows for implementation
 * of editing each lift, deleting each lift, adding a new lift, adding an exercise, and copying a lift.
 * Adding a new lift or exercise is not done view the CardView, so these operations must be interfaced
 * with via the calling activity.
 * @author Tommy Smith
 */

public class WorkoutHistoryCardAdapter extends RecyclerView.Adapter<WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder> {
    private final Workout current_workout;
    private final ArrayList<Lift> current_workout_lifts;
    private final Activity parent_activity;
    private final LiftDbHelper lift_db_helper;
    private final RecyclerView recycler_view;

    /**
     * Constructs the CardView for the history of the workout.
     *
     * @param parent_activity The calling activity of the adapter.
     * @param lift_db_helper  Database helper to support the insertion, deletion, copy of lifts.
     * @param recycler_view   The recycler view that the card adapter will populate.
     * @param current_workout The workout used to populate the card adapter.
     */
    WorkoutHistoryCardAdapter(
            Activity parent_activity,
            LiftDbHelper lift_db_helper,
            RecyclerView recycler_view,
            Workout current_workout) {
        this.parent_activity = parent_activity;
        this.lift_db_helper = lift_db_helper;
        this.recycler_view = recycler_view;
        this.current_workout = current_workout;
        if (this.current_workout != null) {
            // Get the lifts in the current workout.
            this.current_workout_lifts = this.current_workout.getLifts(lift_db_helper);
        } else {
            // The current workout doesn't exist, so there are no lifts/
            this.current_workout_lifts = null;
        }
    }

    /**
     * Creates a view holder for the workout history card view.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return The view holder for the workout history card view.
     */
    @Override
    public WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_workout_card_view, parent, false);

        // Implement the interface required when clicking any position in the workout history card view.
        return new WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder(view, new WorkoutHistoryCardViewHolder.IWorkoutHistoryViewHolderClicks() {
            /**
             * Edits the lift at the given position.
             * @param position  The lift's position in the adapter to edit.
             */
            @Override
            public void editLift(int position) {
                // Show the dialog to edit the lift.
                EditLiftDialog edit_lift_dialog = new EditLiftDialog(
                        parent.getContext(),
                        lift_db_helper,
                        current_workout_lifts.get(position),
                        current_workout_lifts,
                        recycler_view,
                        position,
                        view);
                edit_lift_dialog.show();
            }

            /**
             * Copies the lift at the given position.
             * @param position  The lift's position in the adapter to copy.
             */
            @Override
            public void copyLift(int position) {
                Lift lift_to_copy = current_workout_lifts.get(position);

                // Show a dialog to add a new lift. The new lift is identical to the lift that is being
                // copied.
                AddLiftParams add_lift_params = AddLiftParams.createFromCopiedLift(lift_to_copy);
                AddLiftDialog add_lift_dialog = new AddLiftDialog(
                        parent.getContext(),
                        recycler_view,
                        lift_db_helper,
                        current_workout,
                        current_workout_lifts,
                        add_lift_params);
                add_lift_dialog.show();
            }

            /**
             * Deletes the lift at the given position.
             * @param position  The lift's position in the adapter to delete.
             */
            @Override
            public void deleteLift(int position) {
                deleteLiftAtPosition(position);
            }

            /**
             * Go to the information of the exercise based on the lift at the given position.
             * @param position  The lift's position in the adapter. The exercise done by this lift
             *                  will be the information displayed.
             */
            @Override
            public void goToExerciseInfo(int position) {
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
     *
     * @param holder   The workout history card view holder.
     * @param position The position in the adapter to bind.
     */
    @Override
    public void onBindViewHolder(WorkoutHistoryCardAdapter.WorkoutHistoryCardViewHolder holder, int position) {
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
     *
     * @return The number of lifts in the current workout.
     */
    @Override
    public int getItemCount() {
        if (current_workout_lifts != null) {
            return current_workout_lifts.size();
        }

        // There are no lifts in the workout.
        return 0;
    }

    public ArrayList<Lift> getLifts()
    {
        return this.current_workout_lifts;
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
}