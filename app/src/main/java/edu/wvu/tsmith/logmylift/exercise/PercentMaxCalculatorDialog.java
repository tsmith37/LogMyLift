package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;
import edu.wvu.tsmith.logmylift.lift.Lift;

/**
 * Created by Tommy Smith on 5/4/2018.
 * A dialog used to show, interactively, percentages of an exercise's max effort.
 */

public class PercentMaxCalculatorDialog
{
    private Context context;
    private Exercise currentExercise;
    private Spinner maxTypeSpinner;
    private NumberPicker percentagePicker;
    private AutoCompleteTextView exerciseTextView;
    private EditText exerciseWeightEditText;
    private EditText customWeightEditText;
    private EditText customRepsEditText;
    private TextView percentMaxTextView;
    private LinearLayout exerciseSelectorLayout;
    private LinearLayout maxValueLayout;
    private LinearLayout weightRepsLayout;
    private ImageButton clearExerciseButton;
    private Button updateTrainingWeightButton;

    private int one_rep_max = 0;

    private enum PercentMaxType
    {
        TRAINING_MAX, MAX_WEIGHT, MAX_EFFORT, CUSTOM
    }

    public PercentMaxCalculatorDialog(Context context)
    {
        this.context = context;
    }

    public PercentMaxCalculatorDialog(Context context, Exercise currentExercise)
    {
        this.context = context;
        this.currentExercise = currentExercise;
    }

    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View percent_max_calculator_view = li.inflate(R.layout.percent_max_calculator_dialog, null);
        AlertDialog.Builder percent_max_calculator_dialog_builder = new AlertDialog.Builder(this.context);

        percent_max_calculator_dialog_builder.setView(percent_max_calculator_view);

        String max_percentage_title = this.context.getString(R.string.calculate_percentage_of_max);
        TextView title = new TextView(this.context);
        title.setText(max_percentage_title);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        percent_max_calculator_dialog_builder.setCustomTitle(title);

        this.maxTypeSpinner = percent_max_calculator_view.findViewById(R.id.percent_max_type_spinner);
        if (this.maxTypeSpinner != null)
        {
            this.initPercentMaxTypeSelector(maxTypeSpinner);
        }

        this.percentMaxTextView = percent_max_calculator_view.findViewById(R.id.percent_max_text_view);

        this.percentagePicker = percent_max_calculator_view.findViewById(R.id.percent_number_picker);
        if (this.percentagePicker != null)
        {
            this.initPercentPicker(this.percentagePicker);
        }

        this.customWeightEditText = percent_max_calculator_view.findViewById(R.id.weight_edit_text);
        if (this.customWeightEditText != null)
        {
            this.initWeightEditText(this.customWeightEditText);
        }

        this.customRepsEditText = percent_max_calculator_view.findViewById(R.id.reps_edit_text);
        if (this.customRepsEditText != null)
        {
            this.initRepsEditText(this.customRepsEditText);
        }

        this.exerciseTextView = percent_max_calculator_view.findViewById(R.id.exercise_input);
        if (this.exerciseTextView != null)
        {
            this.populateExerciseNameTextView();
            this.initExerciseNameTextView();
        }

        this.clearExerciseButton = percent_max_calculator_view.findViewById(R.id.clear_exercise_button);
        if (this.clearExerciseButton != null)
        {
            this.initClearExerciseButton();
        }

        this.updateTrainingWeightButton = percent_max_calculator_view.findViewById(R.id.update_training_weight_button);
        if (this.updateTrainingWeightButton != null)
        {
            this.initUpdateTrainingWeightButton();
        }

        this.exerciseSelectorLayout = percent_max_calculator_view.findViewById(R.id.exercise_selector_layout);
        this.maxValueLayout = percent_max_calculator_view.findViewById(R.id.max_value_layout);
        this.weightRepsLayout = percent_max_calculator_view.findViewById(R.id.weight_reps_horizontal_layout);

        this.exerciseWeightEditText = percent_max_calculator_view.findViewById(R.id.training_weight_edit_text);

        AlertDialog percent_max_calculator_dialog = percent_max_calculator_dialog_builder.create();
        percent_max_calculator_dialog.show();
    }

    private PercentMaxType findPercentMaxType()
    {
        switch (maxTypeSpinner.getSelectedItemPosition())
        {
            case 0:
                return PercentMaxType.TRAINING_MAX;
            case 1:
                return PercentMaxType.MAX_WEIGHT;
            case 2:
                return PercentMaxType.MAX_EFFORT;
            default:
                return PercentMaxType.CUSTOM;
        }
    }

    private void initPercentMaxTypeSelector(Spinner percent_max_type_selector)
    {
        List<String> maxTypes = new ArrayList<>();
        maxTypes.add("Exercise Training Max");
        maxTypes.add("Exercise Max Lift");
        maxTypes.add("Exercise Max Effort");
        maxTypes.add("Custom Weight");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, maxTypes);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        percent_max_type_selector.setAdapter(dataAdapter);

        percent_max_type_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                recalculatePercentMax();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initPercentPicker(NumberPicker percentagePicker)
    {
        percentagePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            try
            {
                recalculatePercentMax();
            }
            catch (Exception ignored)
            {};
        });

        percentagePicker.setMaxValue(20);
        percentagePicker.setMinValue(0);
        percentagePicker.setValue(20);
        percentagePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        NumberPicker.Formatter max_lift_percentage_formatter = i -> {
            int percentage = i * 5;
            return Integer.toString(percentage);
        };
        percentagePicker.setFormatter(max_lift_percentage_formatter);
    }

    private void initWeightEditText(EditText weightEditText)
    {
        weightEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
            {
                try
                {
                    recalculatePercentMax();
                }
                catch (Exception ignored)
                {};
            }
        });
    }

    private void initRepsEditText(EditText repsEditText)
    {
        repsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
            {
                try
                {
                    recalculatePercentMax();
                }
                catch (Exception ignored)
                {};
            }
        });
    }

    private void calculatePercentMax(int weight, int reps, int percent, TextView percent_max_text_view)
    {
        int max_effort = Lift.findMaxEffort(weight, reps);
        Double percentage_of_max = max_effort * percent / 100.00;
        percent_max_text_view.setText(String.format("%.2f", percentage_of_max));
    }

    private void recalculatePercentMax()
    {
        int one_rep_max = 0;
        int percent = this.percentagePicker.getValue() * 5;

        try
        {
            switch (findPercentMaxType())
            {
                case MAX_EFFORT:
                    this.showMaxWeightCalculationLayout();
                    one_rep_max = currentExercise.getMaxEffort(new LiftDbHelper(this.context));
                    break;
                case MAX_WEIGHT:
                    this.showMaxWeightCalculationLayout();
                    one_rep_max = currentExercise.getMaxWeight(new LiftDbHelper(this.context));
                    break;
                case TRAINING_MAX:
                    this.showTrainingMaxCalculationLayout();
                    one_rep_max = currentExercise.getTrainingWeight(new LiftDbHelper(this.context));
                    break;
                case CUSTOM:
                default:
                    this.showCustomCalculationLayout();
                    one_rep_max = Lift.findMaxEffort(
                            Integer.parseInt(customWeightEditText.getText().toString()),
                            Integer.parseInt(customRepsEditText.getText().toString()));
                    break;
            }

            this.populateExerciseWeight(one_rep_max);
            Double percentage_of_max = one_rep_max * ((double) (percent / 100.00));
            this.percentMaxTextView.setText(String.format("%.2f", percentage_of_max));
        }
        catch (Exception ignored)
        {}
    }

    private void showCustomCalculationLayout()
    {
        this.exerciseSelectorLayout.setVisibility(View.GONE);
        this.maxValueLayout.setVisibility(View.GONE);
        this.weightRepsLayout.setVisibility(View.VISIBLE);
    }

    private void showTrainingMaxCalculationLayout()
    {
        this.exerciseSelectorLayout.setVisibility(View.VISIBLE);
        this.maxValueLayout.setVisibility(View.VISIBLE);
        this.weightRepsLayout.setVisibility(View.GONE);

        this.exerciseWeightEditText.setEnabled(true);
        this.updateTrainingWeightButton.setVisibility(View.VISIBLE);
    }

    private void showMaxWeightCalculationLayout()
    {
        this.exerciseSelectorLayout.setVisibility(View.VISIBLE);
        this.maxValueLayout.setVisibility(View.VISIBLE);
        this.weightRepsLayout.setVisibility(View.GONE);

        this.exerciseWeightEditText.setEnabled(false);
        this.updateTrainingWeightButton.setVisibility(View.GONE);
    }

    private void populateExerciseNameTextView()
    {
        if (this.currentExercise != null)
        {
            this.exerciseTextView.setText(this.currentExercise.getName());
        }
    }

    private void initExerciseNameTextView()
    {
        SimpleCursorAdapter exercise_adapter = new SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LiftDbHelper.EXERCISE_COLUMN_NAME},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        exerciseTextView.setAdapter(exercise_adapter);

        // When the exercise input is changed, query the database for potential matches. Assume that
        // the no exercise is currently selected. If an exercise is defaulted, already selected, or
        // selected from the input, then it will be corrected.
        exercise_adapter.setFilterQueryProvider(constraint -> {
            // Get the cursor limited by the current filter text.
            return new LiftDbHelper(context).selectExercisesCursor(constraint.toString());
        });

        exercise_adapter.setCursorToStringConverter(cursor -> {
            int column_index = cursor.getColumnIndex(LiftDbHelper.EXERCISE_COLUMN_NAME);
            return cursor.getString(column_index);
        });

        // If an item is clicked for the exercise input, select that exercise.
        exerciseTextView.setOnItemClickListener((parent, view, position, id) -> {
            // An exercise was explicitly selected from the input.
            currentExercise = new LiftDbHelper(context).selectExerciseFromExerciseId(id);
            recalculatePercentMax();
        });
    }

    private void populateExerciseWeight(int one_rep_max)
    {
        this.exerciseWeightEditText.setText(Integer.toString(one_rep_max));
    }

    private void initClearExerciseButton()
    {
        clearExerciseButton.setOnClickListener(v -> {
            // Set the input text box to be blank.
            exerciseTextView.setText("");
        });
    }

    private void initUpdateTrainingWeightButton()
    {
        this.updateTrainingWeightButton.setOnClickListener(view -> {
            try
            {
                int training_weight = Integer.parseInt(exerciseWeightEditText.getText().toString());
                currentExercise.updateTrainingWeight(new LiftDbHelper(context), training_weight);
                recalculatePercentMax();
            }
            catch (Exception ignored)
            {}
        });
    }
}
