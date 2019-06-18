package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
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
    private Spinner maxTypeSpinner;
    private NumberPicker percentagePicker;
    private EditText exerciseEditText;
    private EditText customWeightEditText;
    private EditText customRepsEditText;
    private TextView percentMaxTextView;

    private enum PercentMaxType
    {
        TRAINING_MAX, MAX_WEIGHT, MAX_EFFORT, CUSTOM
    }

    public PercentMaxCalculatorDialog(Context context)
    {
        this.context = context;
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
        maxTypes.add("Exercise Max Effort");
        maxTypes.add("Exercise Max Lift");
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

        switch (findPercentMaxType())
        {
            case CUSTOM:
                one_rep_max = Lift.findMaxEffort(
                        Integer.parseInt(customWeightEditText.getText().toString()),
                        Integer.parseInt(customRepsEditText.getText().toString()));
                break;
            case MAX_EFFORT:
                break;
            case MAX_WEIGHT:
                break;
            case TRAINING_MAX:
                break;
                default:
                    break;

        }

        Double percentage_of_max = one_rep_max * ((double) (percent / 100.00));
        this.percentMaxTextView.setText(String.format("%.2f", percentage_of_max));
    }
}
