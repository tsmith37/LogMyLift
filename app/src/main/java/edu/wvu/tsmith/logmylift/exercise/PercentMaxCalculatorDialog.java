package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

/**
 * Created by Tommy Smith on 5/4/2018.
 * A dialog used to show, interactively, percentages of an exercise's max effort.
 */

public class PercentMaxCalculatorDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private Exercise exercise;

    public PercentMaxCalculatorDialog(Context context, LiftDbHelper lift_db_helper, Exercise exercise)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.exercise = exercise;
    }

    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        View percent_max_calculator_view = li.inflate(R.layout.percent_max_calculator_dialog, null);
        AlertDialog.Builder percent_max_calculator_dialog_builder = new AlertDialog.Builder(this.context);
        String dialog_title = String.format("%s - %s", this.context.getString(R.string.calculate_percentage_of_max), exercise.getName());
        percent_max_calculator_dialog_builder.setTitle(dialog_title);
        percent_max_calculator_dialog_builder.setView(percent_max_calculator_view);

        TextView exercise_description_text_view = percent_max_calculator_view.findViewById(R.id.exercise_description_text_view);
        exercise_description_text_view.setText(exercise.getDescription());

        final NumberPicker percent_max_number_picker = percent_max_calculator_view.findViewById(R.id.percent_number_picker);
        final TextView percent_max_text_view = percent_max_calculator_view.findViewById(R.id.percent_max_text_view);

        final int theoretical_max = this.lift_db_helper.selectLiftFromLiftId(this.exercise.getMaxLiftId()).calculateMaxEffort();

        percent_max_number_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int percent = newVal * 5;
                Double percentage_of_max = theoretical_max * ((double) (percent / 100.00));
                percent_max_text_view.setText(String.format("%.2f", percentage_of_max));
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
}
