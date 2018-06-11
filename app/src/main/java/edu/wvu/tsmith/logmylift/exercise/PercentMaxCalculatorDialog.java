package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

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

        final TextView percent_max_text_view = percent_max_calculator_view.findViewById(R.id.percent_max_text_view);

        final NumberPicker percent_max_number_picker = percent_max_calculator_view.findViewById(R.id.percent_number_picker);
        final EditText weight_edit_text = percent_max_calculator_view.findViewById(R.id.weight_edit_text);
        final EditText reps_edit_text = percent_max_calculator_view.findViewById(R.id.reps_edit_text);

        percent_max_number_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                try
                {
                    int percent = newVal * 5;
                    calculatePercentMax(Integer.parseInt(weight_edit_text.getText().toString()), Integer.parseInt(reps_edit_text.getText().toString()),  percent, percent_max_text_view);
                }
                catch (Exception ignored)
                {};
            }
        });

        weight_edit_text.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    try
                    {
                        int percent = percent_max_number_picker.getValue() * 5;
                        calculatePercentMax(Integer.parseInt(weight_edit_text.getText().toString()), Integer.parseInt(reps_edit_text.getText().toString()), percent, percent_max_text_view);
                    }
                    catch (Exception ignored)
                    {};
                }
            }
        });

        reps_edit_text.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    try
                    {
                        int percent = percent_max_number_picker.getValue() * 5;
                        calculatePercentMax(Integer.parseInt(weight_edit_text.getText().toString()), Integer.parseInt(reps_edit_text.getText().toString()), percent, percent_max_text_view);
                    }
                    catch (Exception ignored)
                    {};
                }
            }
        });

        percent_max_number_picker.setMaxValue(20);
        percent_max_number_picker.setMinValue(0);
        percent_max_number_picker.setValue(20);
        percent_max_number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        NumberPicker.Formatter max_lift_percentage_formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                int percentage = i * 5;
                return Integer.toString(percentage);
            }
        };
        percent_max_number_picker.setFormatter(max_lift_percentage_formatter);

        AlertDialog percent_max_calculator_dialog = percent_max_calculator_dialog_builder.create();
        percent_max_calculator_dialog.show();
    }

    private void calculatePercentMax(int weight, int reps, int percent, TextView percent_max_text_view)
    {
        int max_effort = Lift.findMaxEffort(weight, reps);
        Double percentage_of_max = max_effort * ((double) (percent / 100.00));
        percent_max_text_view.setText(String.format("%.2f", percentage_of_max));
    }
}
