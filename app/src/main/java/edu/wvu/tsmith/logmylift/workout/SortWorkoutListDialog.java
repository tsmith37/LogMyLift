package edu.wvu.tsmith.logmylift.workout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.concurrent.Callable;

public class SortWorkoutListDialog
{
    private Context context;
    private LoadWorkoutHistoryListParams params;

    public SortWorkoutListDialog(Context context, LoadWorkoutHistoryListParams params)
    {
        this.context = context;
        this.params = params;
    }

    public LoadWorkoutHistoryListParams getParams()
    {
        return this.params;
    }

    public void show(final Callable<Integer> post_selection_function)
    {
        AlertDialog.Builder dialog_builder;
        dialog_builder = new AlertDialog.Builder(this.context);
        String[] choices = {"Newest", "Oldest", "Longest", "Shortest", "Most Sets", "Least Sets"};
        dialog_builder.setItems(choices, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case 0:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.DATE_DESC);
                        break;
                    case 1:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.DATE_ASC);
                        break;
                    case 2:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.LENGTH_DESC);
                        break;
                    case 3:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.LENGTH_ASC);
                        break;
                    case 4:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.LIFT_COUNT_DESC);
                        break;
                    case 5:
                        params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.LIFT_COUNT_ASC);
                        break;
                }

                try
                {
                    post_selection_function.call();
                }
                catch (Exception ignored) {};
            }
        });
        AlertDialog dialog = dialog_builder.create();
        dialog.show();
    }
}
