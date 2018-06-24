package edu.wvu.tsmith.logmylift.workout;

import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.Date;

public class LoadWorkoutHistoryListParams
{
    public enum WorkoutListOrder
    {
        DATE_DESC, DATE_ASC, LENGTH_DESC, LENGTH_ASC, LIFT_COUNT_DESC, LIFT_COUNT_ASC;
    }

    private String name_filter;
    private Date from_date;
    private Date to_date;
    private WorkoutListOrder workout_list_order;

    public LoadWorkoutHistoryListParams()
    {
        this.clearNameFilter();
        this.clearDateFilter();
        this.workout_list_order = WorkoutListOrder.DATE_DESC;
    }

    public String getNameFilter()
    {
        return this.name_filter;
    }

    public Date getFromDate()
    {
        return this.from_date;
    }

    public Date getToDate()
    {
        return this.to_date;
    }

    public WorkoutListOrder getOrder()
    {
        return this.workout_list_order;
    }

    public void filterName(String name_filter)
    {
        this.name_filter = name_filter;
    }

    public void clearNameFilter()
    {
        this.name_filter = "";
    }

    public void filterDate(Date from_date, Date to_date, View snackbar_parent_view)
    {
        if (to_date.before(from_date))
        {
            this.clearDateFilter();
            Snackbar.make(snackbar_parent_view, "From date must be before the to date.", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            this.from_date = from_date;
            this.to_date = to_date;
        }
    }

    public void clearDateFilter()
    {
        this.from_date = new Date(0);
        this.to_date = new Date();
    }

    public void setSortOrder(WorkoutListOrder order)
    {
        this.workout_list_order = order;
    }
}
