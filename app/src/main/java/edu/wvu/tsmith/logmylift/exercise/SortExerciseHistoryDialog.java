package edu.wvu.tsmith.logmylift.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Tommy Smith on 5/7/2018.
 * Shows a dialog used to sort exercise history any number of ways: from heaviest to light and vice
 * versa, from hardest to easiest and vice versa, and from most recent to oldest and vice versa.
 */

public class SortExerciseHistoryDialog
{
    private Context context;
    private ExerciseDetailFragment fragment;

    public SortExerciseHistoryDialog(Context context, ExerciseDetailFragment fragment)
    {
        this.context = context;
        this.fragment = fragment;
    }

    public void show()
    {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this.context);
        String[] choices = {"Latest", "Earliest", "Heaviest", "Lightest", "Hardest", "Easiest"};
        dialog_builder.setItems(choices, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which) {
                    case 0:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.DATE_DESC);
                        break;
                    case 1:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.DATE_ASC);
                        break;
                    case 2:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.WEIGHT_DESC);
                        break;
                    case 3:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.WEIGHT_ASC);
                        break;
                    case 4:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.MAX_DESC);
                        break;
                    case 5:
                        fragment.reloadExerciseHistory(SelectExerciseHistoryParams.ExerciseListOrder.MAX_ASC);
                        break;
                }
            }
        });
        AlertDialog dialog = dialog_builder.create();
        dialog.show();
    }
}
