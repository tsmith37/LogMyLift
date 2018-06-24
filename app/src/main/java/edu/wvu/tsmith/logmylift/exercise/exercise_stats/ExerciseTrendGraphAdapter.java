package edu.wvu.tsmith.logmylift.exercise.exercise_stats;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.exercise.SelectExerciseHistoryParams;
import edu.wvu.tsmith.logmylift.lift.Lift;

public class ExerciseTrendGraphAdapter
{
    ArrayList<Lift> lifts;

    public ExerciseTrendGraphAdapter(LiftDbHelper lift_db_helper, long exercise_id)
    {
        this.lifts = lift_db_helper.selectExerciseHistoryLifts(new SelectExerciseHistoryParams(lift_db_helper.selectExerciseFromExerciseId(exercise_id)));
    }

    public ExerciseTrendGraphAdapter(LiftDbHelper lift_db_helper, long exercise_id, Date from_date, Date to_date)
    {
        this.lifts = lift_db_helper.selectExerciseHistoryLifts(new SelectExerciseHistoryParams(lift_db_helper.selectExerciseFromExerciseId(exercise_id), from_date, to_date));
    }

    public LineGraphSeries<DataPoint> getMaxWeightSeries(final View snackbar_parent_view)
    {
        ArrayList<DataPoint> data_series = new ArrayList<>();
        final Map<String, Lift> max_weight_lifts_by_date = new TreeMap<>();

        final SimpleDateFormat map_date_format = new SimpleDateFormat("yyyyMMdd");
        for (Lift l : this.lifts)
        {
            String lift_date_string = map_date_format.format(l.getStartDate());
            if (max_weight_lifts_by_date.containsKey(lift_date_string))
            {
                Lift current_max_weight_lift_on_date = max_weight_lifts_by_date.get(lift_date_string);
                int current_max_weight = current_max_weight_lift_on_date.getWeight();
                int this_lift_weight = l.getWeight();
                if (this_lift_weight > current_max_weight)
                {
                    max_weight_lifts_by_date.put(lift_date_string, l);
                }
            }
            else
            {
                max_weight_lifts_by_date.put(lift_date_string, l);
            }
        }

        Iterator max_lifts_by_date_iterator = max_weight_lifts_by_date.entrySet().iterator();
        while (max_lifts_by_date_iterator.hasNext())
        {
            Map.Entry max_lift_and_date = (Map.Entry) max_lifts_by_date_iterator.next();
            Lift max_lift = (Lift) max_lift_and_date.getValue();
            DataPoint data_point = new DataPoint(max_lift.getStartDate(), max_lift.getWeight());
            data_series.add(data_point);
        }

        DataPoint[] data_series_array = new DataPoint[data_series.size()];
        data_series_array = data_series.toArray(data_series_array);
        LineGraphSeries<DataPoint> to_ret = new LineGraphSeries<>(data_series_array);

        to_ret.setOnDataPointTapListener(new OnDataPointTapListener()
        {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint)
            {
                Date d = new Date((long) dataPoint.getX());
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                String formatted_date = date_format.format(d.getTime());

                Lift max_effort_lift = max_weight_lifts_by_date.get(map_date_format.format(d.getTime()));
                String lift_string = String.format("%s: %d for %d", formatted_date, max_effort_lift.getWeight(), max_effort_lift.getReps());
                Snackbar.make(snackbar_parent_view, lift_string, Snackbar.LENGTH_LONG).show();
            }
        });

        return to_ret;
    }

    public LineGraphSeries<DataPoint> getMaxEffortSeries(final View snackbar_parent_view)
    {
        ArrayList<DataPoint> data_series = new ArrayList<>();
        final Map<String, Lift> max_effort_lifts_by_date = new TreeMap<>();

        final SimpleDateFormat map_date_format = new SimpleDateFormat("yyyyMMdd");
        for (Lift l : this.lifts)
        {
            String lift_date_string = map_date_format.format(l.getStartDate());
            if (max_effort_lifts_by_date.containsKey(lift_date_string))
            {
                Lift current_max_effort_lift_on_date = max_effort_lifts_by_date.get(lift_date_string);
                int current_max_effort = current_max_effort_lift_on_date.calculateMaxEffort();
                int this_lift_max_effort = l.calculateMaxEffort();
                if (this_lift_max_effort > current_max_effort)
                {
                    max_effort_lifts_by_date.put(lift_date_string, l);
                }
            }
            else
            {
                max_effort_lifts_by_date.put(lift_date_string, l);
            }
        }

        Iterator max_lifts_by_date_iterator = max_effort_lifts_by_date.entrySet().iterator();
        while (max_lifts_by_date_iterator.hasNext())
        {
            Map.Entry max_lift_and_date = (Map.Entry) max_lifts_by_date_iterator.next();
            Lift max_lift = (Lift) max_lift_and_date.getValue();
            DataPoint data_point = new DataPoint(max_lift.getStartDate(), max_lift.calculateMaxEffort());
            data_series.add(data_point);
        }

        DataPoint[] data_series_array = new DataPoint[data_series.size()];
        data_series_array = data_series.toArray(data_series_array);
        LineGraphSeries<DataPoint> to_ret = new LineGraphSeries<>(data_series_array);

        to_ret.setOnDataPointTapListener(new OnDataPointTapListener()
        {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint)
            {
                Date d = new Date((long) dataPoint.getX());
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                String formatted_date = date_format.format(d.getTime());

                Lift max_effort_lift = max_effort_lifts_by_date.get(map_date_format.format(d.getTime()));
                String lift_string = String.format("%s: %d for %d", formatted_date, max_effort_lift.getWeight(), max_effort_lift.getReps());
                Snackbar.make(snackbar_parent_view, lift_string, Snackbar.LENGTH_LONG).show();
            }
        });

        return to_ret;
    }
}
