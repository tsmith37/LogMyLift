package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import edu.wvu.tsmith.logmylift.exercise.ExerciseListActivity;
import edu.wvu.tsmith.logmylift.lift.AddLift;
import edu.wvu.tsmith.logmylift.workout.NewWorkoutDialog;
import edu.wvu.tsmith.logmylift.workout.Workout;
import edu.wvu.tsmith.logmylift.workout.WorkoutDetailFragment;
import edu.wvu.tsmith.logmylift.workout.WorkoutListActivity;

public class Start extends AppCompatActivity
{
    private LiftDbHelper lift_db_helper;
    private Context current_context;
    private final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        current_context = getApplicationContext();
        lift_db_helper = new LiftDbHelper(current_context);

        Button continue_workout_button = this.findViewById(R.id.continue_workout_button);
        continue_workout_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                continueLastWorkout();
            }
        });

        Button start_new_workout_button = this.findViewById(R.id.start_new_workout_button);
        start_new_workout_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                NewWorkoutDialog new_workout_dialog = new NewWorkoutDialog(v.getContext(), lift_db_helper);
                new_workout_dialog.show();
            }
        });

        Button workout_history_button = this.findViewById(R.id.workout_history_button);
        workout_history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showWorkoutHistory();
            }
        });

        Button exercise_history_button = this.findViewById(R.id.exercise_button);
        exercise_history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showExerciseHistory();
            }
        });

        ImageButton settings_button = this.findViewById(R.id.settings_button);
        settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(Start.this);
                String[] choices = {"Export Database", "Import Database"};
                dialog_builder.setItems(choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which) {
                            case 0:
                                exportDatabase();
                                break;
                            case 1:
                                showImportDatabaseDialog();
                        }
                    }
                });
                AlertDialog dialog = dialog_builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Snackbar.make(findViewById(R.id.start_new_workout_button), "App not allowed to write to storage", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Continue the most recent workout.
     */
    private void continueLastWorkout()
    {
        Workout last_workout = lift_db_helper.selectLastWorkout();
        if (last_workout != null)
        {
            Intent workout_intent = new Intent(current_context, AddLift.class);
            workout_intent.putExtra(WorkoutDetailFragment.workout_parcel, last_workout);
            startActivity(workout_intent);
        }
        else
        {
            Snackbar no_previous_workouts = Snackbar.make(findViewById(R.id.continue_workout_button), R.string.no_workouts_to_continue, Snackbar.LENGTH_LONG);
            no_previous_workouts.setAction(R.string.start_new_workout, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    NewWorkoutDialog new_workout_dialog = new NewWorkoutDialog(v.getContext(), lift_db_helper);
                    new_workout_dialog.show();
                }
            });
            no_previous_workouts.show();
        }
    }

    /**
     * Show the ExerciseListActivity.
     */
    private void showExerciseHistory()
    {
        Intent exercise_list_intent = new Intent(current_context, ExerciseListActivity.class);
        startActivity(exercise_list_intent);
    }

    /**
     * Show the WorkoutListActivity.
     */
    private void showWorkoutHistory()
    {
        Intent workout_list_intent = new Intent(current_context, WorkoutListActivity.class);
        startActivity(workout_list_intent);
    }

    private void exportDatabase()
    {
        try
        {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            boolean hasPermission = (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }

            if (sd.canWrite())
            {
                String currentDBPath = current_context.getDatabasePath(LiftDbHelper.DATABASE_NAME).getPath();
                String backupDBPath = getString(R.string.database_backup_name);
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Snackbar.make(findViewById(R.id.start_new_workout_button), "Database backed up at: " + backupDB.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e)
        {
            Snackbar.make(findViewById(R.id.start_new_workout_button), "Something went wrong.", Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void showImportDatabaseDialog()
    {
        AlertDialog.Builder import_database_dialog_builder = new AlertDialog.Builder(this);
        import_database_dialog_builder.setTitle("Import Database");
        import_database_dialog_builder.setMessage("Are you sure you want to import a database? The database must be located locally in your downloads folder. This will delete all your current data.");
        import_database_dialog_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try
                {
                    File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File data = Environment.getDataDirectory();

                    boolean hasPermission = (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    if (!hasPermission)
                    {
                        ActivityCompat.requestPermissions(Start.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                    }

                    String currentDBPath = current_context.getDatabasePath(LiftDbHelper.DATABASE_NAME).getPath();
                    String backupDBPath = getString(R.string.database_backup_name);
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (backupDB.exists())
                    {
                        FileChannel src = new FileInputStream(backupDB).getChannel();
                        FileChannel dst = new FileOutputStream(currentDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        Snackbar.make(findViewById(R.id.start_new_workout_button), "Database imported.", Snackbar.LENGTH_LONG).show();
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.start_new_workout_button), "Couldn't find a backup at: " + backupDB.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
                    }
                }
                catch (Exception e)
                {
                    Snackbar.make(findViewById(R.id.start_new_workout_button), "Something went wrong.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        import_database_dialog_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.start_new_workout_button), "Database not imported.", Snackbar.LENGTH_LONG).show();
            }
        });
        import_database_dialog_builder.show();
    }
}
