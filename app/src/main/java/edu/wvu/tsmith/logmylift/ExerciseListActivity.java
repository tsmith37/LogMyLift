package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import edu.wvu.tsmith.logmylift.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Exercises. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ExerciseDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ExerciseListActivity extends AppCompatActivity {
    LiftDbHelper lift_db;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lift_db = new LiftDbHelper(getApplicationContext());
        setContentView(R.layout.activity_exercise_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton add_exercise_button = (FloatingActionButton) findViewById(R.id.add_exercise_button);
        add_exercise_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddExerciseDialog();
            }
        });

        View recyclerView = findViewById(R.id.exercise_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView, "");

        if (findViewById(R.id.exercise_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        final EditText exercise_filter_input = (EditText) this.findViewById(R.id.exercise_filter_input);
        if (exercise_filter_input != null) {
            exercise_filter_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    final EditText exercise_filter_input = (EditText) findViewById(R.id.exercise_filter_input);
                    View recyclerView = findViewById(R.id.exercise_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView, exercise_filter_input.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        ImageButton clear_exercise_filter = (ImageButton) this.findViewById(R.id.clear_exercise_filter);
        if (clear_exercise_filter != null) {
            clear_exercise_filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exercise_filter_input.setText("");
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, String filter) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(lift_db.selectExerciseList(filter)));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Exercise> exercise_list_values;

        public SimpleItemRecyclerViewAdapter(List<Exercise> exercises)
        {
            exercise_list_values = exercises;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.exercise_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.exercise = exercise_list_values.get(position);
            holder.exercise_name_text_view.setText(exercise_list_values.get(position).getName());
            holder.exercise_description_text_view.setText(exercise_list_values.get(position).getDescription());

            holder.exercise_list_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putLong(ExerciseDetailFragment.exercise_id, holder.exercise.getExerciseId());
                        ExerciseDetailFragment fragment = new ExerciseDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.exercise_detail_container, fragment)
                                .commit();
                    }
                    else
                    {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ExerciseDetailActivity.class);
                        intent.putExtra(ExerciseDetailFragment.exercise_id, holder.exercise.getExerciseId());

                        context.startActivity(intent);
                    }}
            });

            holder.exercise_list_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showEditExerciseDialog(holder.exercise, position);
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return exercise_list_values.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View exercise_list_view;
            public final TextView exercise_name_text_view;
            public final TextView exercise_description_text_view;
            public Exercise exercise;

            public ViewHolder(View view) {
                super(view);
                exercise_list_view = view;
                exercise_name_text_view = (TextView) view.findViewById(R.id.exercise_name_text);
                exercise_description_text_view = (TextView) view.findViewById(R.id.exercise_description_text);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + exercise_name_text_view.getText() + "'";
            }
        }
    }

    private void showAddExerciseDialog()
    {
        LayoutInflater li = LayoutInflater.from(this);
        View add_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder add_exercise_dialog_builder = new AlertDialog.Builder(this);
        add_exercise_dialog_builder.setTitle(R.string.create_exercise_text);
        add_exercise_dialog_builder.setView(add_exercise_dialog_view);
        final EditText exercise_name_text = (EditText) add_exercise_dialog_view.findViewById(R.id.add_exercise_name_dialog_text);
        final EditText exercise_description_text = (EditText) add_exercise_dialog_view.findViewById(R.id.add_exercise_description_dialog_text);
        add_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (exercise_name_text.getText().toString().isEmpty())
                {
                    Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Exercise new_exercise = new Exercise(lift_db, exercise_name_text.getText().toString(), exercise_description_text.getText().toString());
                    Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise added.", Snackbar.LENGTH_LONG).show();
                    View recyclerView = findViewById(R.id.exercise_list);
                    assert recyclerView != null;
                    setupRecyclerView((RecyclerView) recyclerView, "");
                }
            }
        });

        add_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise not added.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog add_exercise_dialog = add_exercise_dialog_builder.create();
        add_exercise_dialog.show();
    }

    private void showEditExerciseDialog(final Exercise current_exercise, final int exercise_position_in_adapter)
    {
        LayoutInflater li = LayoutInflater.from(this);
        // Re-use the add exercise dialog here...
        View edit_exercise_dialog_view = li.inflate(R.layout.add_exercise_dialog, null);
        AlertDialog.Builder edit_exercise_dialog_builder = new AlertDialog.Builder(this);
        edit_exercise_dialog_builder.setTitle("Edit Exercise");
        edit_exercise_dialog_builder.setView(edit_exercise_dialog_view);
        final EditText exercise_name_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.add_exercise_name_dialog_text);
        exercise_name_text.setText(current_exercise.getName());
        final EditText exercise_description_text = (EditText) edit_exercise_dialog_view.findViewById(R.id.add_exercise_description_dialog_text);
        exercise_description_text.setText(current_exercise.getDescription());
        edit_exercise_dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String edited_exercise_name = exercise_name_text.getText().toString();
                if (!edited_exercise_name.isEmpty())
                {
                    current_exercise.setName(edited_exercise_name);
                    current_exercise.setDescription(exercise_description_text.getText().toString());
                    Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise updated.", Snackbar.LENGTH_LONG).show();
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.exercise_list);
                    SimpleItemRecyclerViewAdapter recycler_view_adapter = (SimpleItemRecyclerViewAdapter) recyclerView.getAdapter();
                    recycler_view_adapter.notifyItemChanged(exercise_position_in_adapter);
                }
                else
                {
                    Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise name not valid.", Snackbar.LENGTH_LONG).show();
                }
            }});

        edit_exercise_dialog_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.add_exercise_button), "Exercise not updated.", Snackbar.LENGTH_LONG).show();
            }
        });

        AlertDialog edit_exercise_dialog = edit_exercise_dialog_builder.create();
        edit_exercise_dialog.show();
    }
}
