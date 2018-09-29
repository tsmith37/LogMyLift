package edu.wvu.tsmith.logmylift.exercise.exercise_stats;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

public class ExerciseTrendDialog
{
    public enum TrendType
    {
        MAX_EFFORT, MAX_WEIGHT;
    }

    private Context context;
    private LiftDbHelper lift_db_helper;
    private long exercise_id;
    private ExerciseTrendGraphAdapter trend_adapter;
    private TrendType trend_type;

    public ExerciseTrendDialog(Context context, LiftDbHelper lift_db_helper, long exercise_id, TrendType trend_type)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.exercise_id = exercise_id;
        this.trend_adapter = new ExerciseTrendGraphAdapter(lift_db_helper, this.exercise_id);
        this.trend_type = trend_type;
    }

    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        final View trend_dialog_view = li.inflate(R.layout.exercise_trend_dialog, null);
        AlertDialog.Builder trend_dialog_builder = new AlertDialog.Builder(this.context);
        trend_dialog_builder.setView(trend_dialog_view);

        final GraphView graph = trend_dialog_view.findViewById(R.id.graph_view);

        LineGraphSeries<DataPoint> series = (this.trend_type == TrendType.MAX_EFFORT) ? trend_adapter.getMaxEffortSeries(trend_dialog_view) : trend_adapter.getMaxWeightSeries(trend_dialog_view);
        reloadTrend(series, graph);

        Spinner date_filter_spinner = trend_dialog_view.findViewById(R.id.date_filter);
        ArrayAdapter<CharSequence> date_filter_adapter = ArrayAdapter.createFromResource(this.context, R.array.date_filter_array, android.R.layout.simple_spinner_item);
        date_filter_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_filter_spinner.setAdapter(date_filter_adapter);
        date_filter_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String date_filter_selected = parent.getItemAtPosition(position).toString();

                Date from_date;
                Date to_date = new Date();

                switch (date_filter_selected)
                {
                    case "All Time":
                        from_date = new Date(0);
                        break;
                    case "This Year":
                        Calendar this_year_cal = Calendar.getInstance();
                        this_year_cal.add(Calendar.YEAR, -1);
                        from_date = this_year_cal.getTime();
                        break;
                    case "6 Months":
                        Calendar six_months_cal = Calendar.getInstance();
                        six_months_cal.add(Calendar.MONTH, -6);
                        from_date = six_months_cal.getTime();
                        break;
                    case "This Month":
                        Calendar this_month_cal = Calendar.getInstance();
                        this_month_cal.add(Calendar.MONTH, -1);
                        from_date = this_month_cal.getTime();
                        break;
                    default:
                        from_date = new Date(0);
                        break;
                }

                trend_adapter = new ExerciseTrendGraphAdapter(lift_db_helper, exercise_id, from_date, to_date);
                LineGraphSeries<DataPoint> series = (trend_type ==  TrendType.MAX_EFFORT) ? trend_adapter.getMaxEffortSeries(trend_dialog_view) : trend_adapter.getMaxWeightSeries(trend_dialog_view);
                reloadTrend(series, graph);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog trend_dialog = trend_dialog_builder.create();
        trend_dialog.show();
    }

    private void reloadTrend(LineGraphSeries<DataPoint> series, GraphView graph)
    {
        graph.removeAllSeries();
        series.setDrawDataPoints(true);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this.context));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(series.getLowestValueX());
        graph.getViewport().setMaxX(series.getHighestValueX());

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}
