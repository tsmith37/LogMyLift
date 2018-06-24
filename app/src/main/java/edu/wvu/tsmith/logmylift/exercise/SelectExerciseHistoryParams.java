package edu.wvu.tsmith.logmylift.exercise;

import java.util.Date;

public class SelectExerciseHistoryParams
{
    public enum ExerciseListOrder
    {
        DATE_DESC, DATE_ASC, WEIGHT_DESC, WEIGHT_ASC, MAX_DESC, MAX_ASC
    }

    private Exercise exercise;
    private ExerciseListOrder order;
    private Date from_date;
    private Date to_date;

    public SelectExerciseHistoryParams(Exercise exercise)
    {
        this.exercise = exercise;
        this.order = ExerciseListOrder.DATE_DESC;
        this.from_date = new Date(0);
        this.to_date = new Date();
    }

    public SelectExerciseHistoryParams(Exercise exercise, ExerciseListOrder order)
    {
        this.exercise = exercise;
        this.order = order;
        this.from_date = new Date(0);
        this.to_date = new Date();
    }

    public SelectExerciseHistoryParams(Exercise exercise, Date from_date, Date to_date)
    {
        this.exercise = exercise;
        this.order = ExerciseListOrder.DATE_DESC;
        this.from_date = from_date;
        this.to_date = to_date;
    }

    public Exercise getExercise() {return this.exercise;}
    public ExerciseListOrder getOrder() {return this.order;}
    public Date getFromDate() {return this.from_date;}
    public Date getToDate() {return this.to_date;}
}
