package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.Date;

import edu.wvu.tsmith.logmylift.LiftDbHelper;
import edu.wvu.tsmith.logmylift.R;

public class ExerciseHistoryTrendDialog
{
    private Context context;
    private LiftDbHelper lift_db_helper;
    private long exercise_id;

    public ExerciseHistoryTrendDialog(Context context, LiftDbHelper lift_db_helper, long exercise_id)
    {
        this.context = context;
        this.lift_db_helper = lift_db_helper;
        this.exercise_id = exercise_id;
    }

    public void show()
    {
        LayoutInflater li = LayoutInflater.from(this.context);
        final View trend_dialog_view = li.inflate(R.layout.exercise_history_trend_dialog, null);
        AlertDialog.Builder trend_dialog_builder = new AlertDialog.Builder(this.context);
        trend_dialog_builder.setView(trend_dialog_view);

        GraphView graph = trend_dialog_view.findViewById(R.id.graph_view);
        //Date from_date = new Date(1498953600000)
        //DataPoint[] data = lift_db_helper.getDataPointsFromExercise(new Date(1498953600000));
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        //series.setDrawDataPoints(true);
        //series.setOnDataPointTapListener(new OnDataPointTapListener() {
        //    @Override
        //    public void onTap(Series series, DataPointInterface dataPoint) {
        //        Snackbar.make(trend_dialog_view, "Max effort: " + Double.toString(dataPoint.getY()), Snackbar.LENGTH_LONG).show();
        //    }
        //});
        //graph.addSeries(series);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this.context));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);

        AlertDialog trend_dialog = trend_dialog_builder.create();
        trend_dialog.show();
    }
}
