package edu.wvu.tsmith.logmylift.lift;

import edu.wvu.tsmith.logmylift.exercise.Exercise;

public class SelectExerciseHistoryParams
{
    public enum ExerciseListOrder
    {
        DATE_DESC, DATE_ASC, WEIGHT_DESC, WEIGHT_ASC, MAX_DESC, MAX_ASC
    }

    private Exercise exercise;
    private ExerciseListOrder order;

    public SelectExerciseHistoryParams(Exercise exercise)
    {
        this.exercise = exercise;
        this.order = ExerciseListOrder.DATE_DESC;
    }

    public SelectExerciseHistoryParams(Exercise exercise, ExerciseListOrder order)
    {
        this.exercise = exercise;
        this.order = order;
    }

    public Exercise getExercise() {return this.exercise;}
    public ExerciseListOrder getOrder() {return this.order;}
}
