package edu.wvu.tsmith.logmylift.workout;

import android.app.AlertDialog;
import android.content.Context;
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
        String[] choices = {"Newest", "Oldest"};
        dialog_builder.setItems(choices, (dialog, which) -> {
            switch (which)
            {
                case 0:
                    params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.DATE_DESC);
                    break;
                case 1:
                    params.setSortOrder(LoadWorkoutHistoryListParams.WorkoutListOrder.DATE_ASC);
                    break;
            }

            try
            {
                post_selection_function.call();
            }
            catch (Exception ignored) {};
        });
        AlertDialog dialog = dialog_builder.create();
        dialog.show();
    }
}
