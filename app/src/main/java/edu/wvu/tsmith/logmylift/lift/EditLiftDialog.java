package edu.wvu.tsmith.logmylift.lift;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Tommy Smith on 4/29/2018.
 * Dialog to edit a lift.
 */

public class EditLiftDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private Lift lift_to_edit;
    private ArrayList<Lift> current_workout_lifts;
    private RecyclerView recycler_view;
    private int lift_position_in_adapter;
    private View snackbar_parent_view;

    public EditLiftDialog(
            Context context,
            LiftDbHelper lift_db_helper,
            Lift lift_to_edit,
            ArrayList<Lift> current_workout_lifts,
            RecyclerView recycler_view,
            int lift_position_in_adapter,
            View snackbar_parent_view)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.lift_to_edit = lift_to_edit;
        this.current_workout_lifts = current_workout_lifts;
        this.recycler_view = recycler_view;
        this.lift_position_in_adapter = lift_position_in_adapter;
        this.snackbar_parent_view = snackbar_parent_view;
    }

    public void show()
    {
        // Create the dialog to edit the lift.
        LayoutInflater li = LayoutInflater.from(this.context);
        View editLiftDialogView = li.inflate(R.layout.edit_lift_dialog, null);
        AlertDialog.Builder editLiftDialogBuilder = new AlertDialog.Builder(this.context);
        this.initTitle(editLiftDialogBuilder);

        // Reflect the current lift's properties in the edit lift dialog.
        editLiftDialogBuilder.setView(editLiftDialogView);

        TextView exerciseNameTextView = editLiftDialogView.findViewById(R.id.exercise_text_view);
        this.initExerciseNameTextView(exerciseNameTextView);

        EditText weightEditText = editLiftDialogView.findViewById(R.id.weight_edit_text);
        this.setWeightEditText(weightEditText);

        EditText repsEditText = editLiftDialogView.findViewById(R.id.reps_edit_text);
        this.setRepsEditText(repsEditText);

        EditText commentEditText = editLiftDialogView.findViewById(R.id.comment_edit_text);
        this.setCommentEditText(commentEditText);

        AlertDialog editDialog = editLiftDialogBuilder.create();

        Button editLiftButton = editLiftDialogView.findViewById(R.id.edit_lift_button);

        this.initEditLiftButton(editDialog, editLiftDialogView, editLiftButton, weightEditText, repsEditText, commentEditText);

        editDialog.show();
    }

    private void initTitle(AlertDialog.Builder editLiftDialogBuilder)
    {
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.edit_lift));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        editLiftDialogBuilder.setCustomTitle(title);
    }

    private void initExerciseNameTextView(TextView exerciseNameTextView)
    {
        if (exerciseNameTextView != null)
        {
            exerciseNameTextView.setText(lift_to_edit.getExercise().getName());
        }
    }

    private void setWeightEditText(EditText weightEditText)
    {
        if (weightEditText != null)
        {
            String weight = Integer.toString(lift_to_edit.getWeight());
            weightEditText.setText(weight);
        }
    }

    private void setRepsEditText(EditText repsEditText)
    {
        if (repsEditText != null)
        {
            String reps = Integer.toString(lift_to_edit.getReps());
            repsEditText.setText(reps);
        }
    }

    private void setCommentEditText(EditText commentEditText)
    {
        if (commentEditText != null)
        {
            String comment = lift_to_edit.getComment();
            commentEditText.setText(comment);
        }
    }

    private void initEditLiftButton(AlertDialog editDialog, View editLiftDialogView, Button editLiftButton, EditText weightEditText, EditText repsEditText, EditText commentEditText)
    {
        if (editLiftButton != null && editDialog != null && editLiftDialogView != null && weightEditText != null & repsEditText != null && commentEditText != null)
        {
            editLiftButton.setOnClickListener(this.editLift(editDialog, editLiftDialogView, weightEditText, repsEditText, commentEditText));
        }
    }

    private View.OnClickListener editLift(AlertDialog editDialog, View editLiftDialogView, EditText weightEditText, EditText repsEditText, EditText commentEditText)
    {
        return v -> {
            // Check that the weight is valid (i.e., an integer).
            int weight = -1;
            try
            {
                weight = Integer.parseInt(weightEditText.getText().toString());
            }
            catch (Throwable ignored) {}

            // Check that the reps are valid.
            int reps = -1;
            try
            {
                reps = Integer.parseInt(repsEditText.getText().toString());
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
                        commentEditText.getText().toString(),
                        recycler_view,
                        lift_position_in_adapter,
                        true,
                        snackbar_parent_view);
                // Start the edit lift operation in the background.
                new EditLiftOperation().execute(edit_lift_params);
                editDialog.cancel();
            }
            else
            {
                // The lift isn't valid.
                editDialog.cancel();
                Snackbar.make(snackbar_parent_view, R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
            }

            // Hide the keyboard.
            InputMethodManager input_method_manager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            input_method_manager.hideSoftInputFromWindow(editLiftDialogView.getWindowToken(), 0);
        };
    }

    /**
     * An asynchronous operation to edit a lift. The operation is done asynchronously so as not to
     * slow the GUI operation of the recycler view while the database is updating.
     * @author Tommy Smith
     */
    private class EditLiftOperation extends AsyncTask<EditLiftParams, Integer, Boolean>
    {
        // The recycler view.
        RecyclerView recycler_view;

        // The lift's position in the card adapter.
        int lift_position_in_adapter;

        // The previous weight, reps, and comment for the lift. This is stored so that the edit can be
        // retroactively undone.
        int old_lift_weight;
        int old_lift_reps;
        String old_lift_comment;

        // Whether or not the edit can be undone.
        boolean allow_undo;

        View snackbar_parent_view;

        @Override
        protected Boolean doInBackground(EditLiftParams... params)
        {
            recycler_view = params[0].recycler_view;
            lift_position_in_adapter = params[0].lift_position_in_adapter;
            old_lift_weight = params[0].lift.getWeight();
            old_lift_reps = params[0].lift.getReps();
            old_lift_comment = params[0].lift.getComment();
            allow_undo = params[0].allow_undo;
            snackbar_parent_view = params[0].snackbar_parent_view;
            params[0].lift.update(params[0].weight, params[0].reps, params[0].comment);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            if (result)
            {
                recycler_view.getAdapter().notifyItemChanged(lift_position_in_adapter);
                if (allow_undo)
                {
                    // Notify the user that the lift has been updated. Allow the action to be undone.
                    Snackbar edit_lift_snackbar = Snackbar.make(snackbar_parent_view, R.string.lift_updated, Snackbar.LENGTH_LONG);
                    edit_lift_snackbar.setAction(
                            R.string.undo,
                            new UndoEditLiftListener(
                                    recycler_view,
                                    lift_position_in_adapter,
                                    old_lift_weight,
                                    old_lift_reps,
                                    old_lift_comment,
                                    snackbar_parent_view));
                    edit_lift_snackbar.show();
                }
            }
        }
    }

    /**
     * Provides an interface to undo an edit to a lift.
     * @author Tommy Smith
     */
    private class UndoEditLiftListener implements View.OnClickListener
    {
        final RecyclerView recycler_view;

        // The lift's position in the card adapter.
        final int lift_position_in_adapter;

        // The weight, reps, and comment of the lift before editing.
        final int old_weight;
        final int old_reps;
        final String old_comment;

        final View snackbar_parent_view;

        /**
         * Constructor for the undo edit lift listener.
         * @param recycler_view             The recycler view.
         * @param lift_position_in_adapter  The lift's position in the card adapter.
         * @param old_weight                The weight of the lift before editing.
         * @param old_reps                  The reps of the lift before editing.
         * @param old_comment               The comment of the lift before editing.
         * @param snackbar_parent_view      The parent view for a snackbar.
         */
        UndoEditLiftListener(
                RecyclerView recycler_view,
                int lift_position_in_adapter,
                int old_weight,
                int old_reps,
                String old_comment,
                View snackbar_parent_view)
        {
            super();
            this.recycler_view = recycler_view;
            this.lift_position_in_adapter = lift_position_in_adapter;
            this.old_weight = old_weight;
            this.old_reps = old_reps;
            this.old_comment = old_comment;
            this.snackbar_parent_view = snackbar_parent_view;
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
                    recycler_view,
                    lift_position_in_adapter,
                    false,
                    snackbar_parent_view);

            // Asynchronously undo the edit, because it is database-bound. This is done to avoid slowing
            // the GUI.
            new EditLiftOperation().execute(undo_edit_lift_params);
        }
    }
}
