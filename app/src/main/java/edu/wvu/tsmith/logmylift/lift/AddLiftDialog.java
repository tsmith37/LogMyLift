package edu.wvu.tsmith.logmylift.lift;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.exercise.AddExerciseDialog;
import edu.wvu.tsmith.logmylift.exercise.Exercise;
import edu.wvu.tsmith.logmylift.workout.SuggestedExercisesDialog;
import edu.wvu.tsmith.logmylift.workout.Workout;

/**
 * Created by Tommy Smith on 4/27/2018.
 * A dialog to allow the user to add a new lift. The lift may be defaulted to contain the currently
 * selected exercise or to be a copy of a previously existing lift. Suggested exercises may be shown
 * to the user via another dialog. The dialog validates that any lift inserted by the user is valid.
 */

public class AddLiftDialog
{
    private Context context;
    private RecyclerView recycler_view;
    private AddLiftParams add_lift_params;
    private LiftDbHelper lift_db_helper;
    private Workout current_workout;
    private ArrayList<Lift> current_workout_lifts;
    private int add_lift_dialog_resource;
    private boolean exercise_currently_selected;
    private Exercise current_exercise;

    private AutoCompleteTextView exercise_text_view;
    private ImageButton clear_exercise_button;
    private ImageButton suggested_exercises_button;

    private EditText weight_edit_text;
    private EditText reps_edit_text;
    private EditText comment_edit_text;

    private Button add_lift_button;

    /**
     * Constructor for the dialog.
     * @param context                   The context in which to show the dialog.
     * @param recycler_view             The recycler view that is used to display the lift after it is added.
     * @param lift_db_helper            The database helper.
     * @param current_workout           The current workout that the lift will be added to.
     * @param current_workout_lifts     The lifts in the current workout.
     * @param params                    The parameters used for the incoming lift.
     */
    public AddLiftDialog(
            Context context,
            RecyclerView recycler_view,
            LiftDbHelper lift_db_helper,
            Workout current_workout,
            ArrayList<Lift> current_workout_lifts,
            AddLiftParams params)
    {
        this.context = context;
        this.recycler_view = recycler_view;
        this.lift_db_helper = lift_db_helper;
        this.current_workout = current_workout;
        this.current_workout_lifts = current_workout_lifts;
        this.add_lift_params = params;
        this.add_lift_dialog_resource = R.layout.add_lift_dialog;
        exercise_currently_selected = false;
    }

    /**
     * Shows the dialog and accounts for any selections made in it.
     */
    public void show()
    {
        // Create the dialog to add a new lift.
        LayoutInflater li = LayoutInflater.from(context);
        View add_lift_dialog_view = li.inflate(this.add_lift_dialog_resource, null);
        AlertDialog.Builder add_lift_dialog_builder = new AlertDialog.Builder(context);

        add_lift_dialog_builder.setCustomTitle(this.initTitle());

        add_lift_dialog_builder.setView(add_lift_dialog_view);

        final AlertDialog add_lift_dialog = add_lift_dialog_builder.create();

        // Find the exercise text input.
        this.exercise_text_view = add_lift_dialog_view.findViewById(R.id.exercise_input);
        if (this.exercise_text_view != null)
        {
            this.initExerciseTextView();
        }

        // Set the clear exercise button to clear the exercise input.
        this.clear_exercise_button = add_lift_dialog_view.findViewById(R.id.clear_exercise_button);
        if (this.clear_exercise_button != null)
        {
            this.initClearExerciseButton();
        }

        this.weight_edit_text = add_lift_dialog_view.findViewById(R.id.weight_edit_text);
        this.reps_edit_text = add_lift_dialog_view.findViewById(R.id.reps_edit_text);
        this.comment_edit_text = add_lift_dialog_view.findViewById(R.id.comment_edit_text);

        this.initDefaultInputs();

        // Set up the suggested exercises dialog.
        this.suggested_exercises_button = add_lift_dialog_view.findViewById(R.id.suggested_exercises_button);
        if (suggested_exercises_button != null)
        {
            this.initSuggestedExercisesButton();
        }

        // Set up the add lift button press.
        add_lift_button = add_lift_dialog_view.findViewById(R.id.add_lift_button);
        if (add_lift_button != null)
        {
            this.initAddLiftButton(add_lift_dialog);
        }

        // Show the dialog.
        add_lift_dialog.getWindow().setBackgroundDrawableResource(R.color.lightGray);
        add_lift_dialog.show();
    }

    private TextView initTitle()
    {
        TextView title = new TextView(this.context);
        title.setText(this.context.getString(R.string.add_lift));
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        return title;
    }

    private void initExerciseTextView()
    {
        SimpleCursorAdapter exercise_text_view_adapter = new SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // When the exercise input is changed, query the database for potential matches. Assume that
        // the no exercise is currently selected. If an exercise is defaulted, already selected, or
        // selected from the input, then it will be corrected.
        exercise_text_view_adapter.setFilterQueryProvider(constraint -> {
            // The user typed something. Any previously selected exercise is now invalid.
            exercise_currently_selected = false;

            // Get the cursor limited by the current filter text.
            return lift_db_helper.selectExercisesCursor(constraint.toString());
        });

        exercise_text_view_adapter.setCursorToStringConverter(cursor -> {
            int column_index = cursor.getColumnIndex(LiftDbHelper.EXERCISE_COLUMN_NAME);
            return cursor.getString(column_index);
        });

        // If an item is clicked for the exercise input, select that exercise.
        exercise_text_view.setOnItemClickListener((parent, view, position, id) -> {
            // An exercise was explicitly selected from the input.
            changeCurrentExercise(id);
            exercise_currently_selected = true;
        });

        exercise_text_view.setAdapter(exercise_text_view_adapter);
    }

    private void initClearExerciseButton()
    {
        clear_exercise_button.setOnClickListener(v -> {
            // Set the input text box to be blank.
            exercise_text_view.setText("");
            exercise_currently_selected = false;
        });
    }

    private void initDefaultInputs()
    {
        // If default inputs are selected, place them into the dialog now.
        if (this.add_lift_params.set_all_fields)
        {
            exercise_text_view.setText(this.add_lift_params.default_exercise_name);
            weight_edit_text.setText(Integer.toString(this.add_lift_params.default_weight));
            reps_edit_text.setText(Integer.toString(this.add_lift_params.default_reps));
            comment_edit_text.setText(this.add_lift_params.default_comment);

            // Even though the exercise name is there, the exercise itself is not selected.
            exercise_currently_selected = false;

            // Don't focus on the exercise input.
            try
            {
                weight_edit_text.requestFocus();
            }
            catch (Throwable ignored) {}

        }
        else if (this.add_lift_params.set_selected_exercise)
        {
            // The caller of the dialog indicated that an exercise should already be selected. Select
            // that exercise.
            this.changeCurrentExercise(this.add_lift_params.selected_exercise_id);

            if (this.current_exercise != null)
            {
                exercise_text_view.setText(current_exercise.getName());

                exercise_currently_selected = true;
            }
            else
            {
                exercise_currently_selected = false;
            }

            // Don't focus on the exercise input.
            try
            {
                weight_edit_text.requestFocus();
            }
            catch (Throwable ignored) {}

        }
        else
        {
            // Don't set any fields by default and don't select an exercise.
            exercise_currently_selected = false;
        }
    }

    private void initSuggestedExercisesButton()
    {
        this.suggested_exercises_button.setOnClickListener(v -> {
            SuggestedExercisesDialog suggested_exercises_dialog = new SuggestedExercisesDialog(
                    context,
                    lift_db_helper,
                    current_workout,
                    exercise_text_view);
            suggested_exercises_dialog.show();
        });
    }

    private void initAddLiftButton(AlertDialog this_dialog)
    {
        add_lift_button.setOnClickListener(v -> {
            // Check if the exercise input currently contains an actual exercise.
            if (!exercise_currently_selected)
            {
                // Check if the inputted text actually does match an exercise.
                Exercise potential_exercise_from_input = lift_db_helper.selectExerciseFromName(exercise_text_view.getText().toString());
                if (potential_exercise_from_input != null)
                {
                    current_exercise = potential_exercise_from_input;
                    exercise_currently_selected = true;
                }
                else
                {
                    // Offer to the user via Snackbar to add an exercise named the same as the
                    // input to the add lift dialog.
                    Snackbar exercise_name_invalid = Snackbar.make(recycler_view, R.string.exercise_name_not_valid, Snackbar.LENGTH_LONG);
                    exercise_name_invalid.setAction(R.string.add_exercise, v1 -> {
                        AddExerciseDialog add_exercise_dialog = new AddExerciseDialog(
                                context,
                                lift_db_helper,
                                recycler_view,
                                exercise_text_view.getText().toString());
                        add_exercise_dialog.show(() -> {
                            AddLiftDialog add_lift_dialog = new AddLiftDialog(context, recycler_view, lift_db_helper, current_workout, current_workout_lifts, AddLiftParams.createFromExistingExercise(lift_db_helper.getSelectedExercise()));
                            add_lift_dialog.show();
                            return null;
                        });
                    });
                    exercise_name_invalid.show();
                    this_dialog.cancel();
                    return;
                }
            }

            // An exercise is currently selected, so update the currently selected exercise in
            // the database. That way, if the user entered an invalid weight or amount of reps,
            // their exercise selection will be retained.
            lift_db_helper.updateSelectedExercise(current_exercise.getExerciseId());

            // Check that the weight and reps are valid for the new lift.
            int weight = -1;
            try
            {
                weight = Integer.parseInt(weight_edit_text.getText().toString());
            }
            catch (Throwable ignored) {}
            int reps = -1;
            try
            {
                reps = Integer.parseInt(reps_edit_text.getText().toString());
            }
            catch (Throwable ignored) {}

            if ((weight > 0) && (reps > 0))
            {
                // Add the lift.
                String comment = comment_edit_text.getText().toString();
                current_workout_lifts.add(0, current_workout.addLift(current_exercise, reps, weight, comment));
                recycler_view.getAdapter().notifyItemInserted(0);

                this_dialog.cancel();

                // Go to the top of the recycler view.
                RecyclerView.LayoutManager layout_manager = recycler_view.getLayoutManager();
                layout_manager.smoothScrollToPosition(recycler_view, new RecyclerView.State(), 0);
            }
            else
            {
                this_dialog.cancel();
                Snackbar.make(recycler_view, R.string.lift_not_valid, Snackbar.LENGTH_LONG).show();
            }
        });

        this_dialog.cancel();
    }

    /**
     * Change the currently selected exercise based on the exercise ID.
     * @param exercise_id   The current exercise's ID.
     */
    private void changeCurrentExercise(long exercise_id)
    {
        // Set the current exercise.
        this.current_exercise = this.lift_db_helper.selectExerciseFromExerciseId(exercise_id);

        // An exercise is currently selected.
        this.exercise_currently_selected = true;
    }
}
