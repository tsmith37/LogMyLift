package edu.wvu.tsmith.logmylift.lift;

/**
 * Created by Tommy Smith on 4/27/2018.
 * Parameters used to add a new lift. These are used to provide default values to the dialog.
 */

public class AddLiftParams
{
    // Set all of the fields of the new lift.
    boolean set_all_fields;

    // The exercise name to use as a default when the dialog is created. This is used when a lift
    // is copied so that the exercise name is identical to the copied lift.
    String default_exercise_name;

    // The weight to use as a default when the dialog is created. This is used when a lift is copied
    // so that the weight is identical to the copied lift.
    int default_weight;

    // The reps to use as a default when the dialog is created. This is used when a lift is copied
    // so that the reps are identical to the copied lift.
    int default_reps;

    // The comment to use as a default when the dialog is created. This is used when a lift is
    // copied so that the comments are identical to the copied lift.
    String default_comment;

    // Set the dialog's selected exercise. This won't cause all of the fields to be selected by default, just the name.
    boolean set_selected_exercise;

    // The ID of the selected exercise.
    long selected_exercise_id;

    /**
     * Construtor. This class uses factory methods to populate the constructor values correctly.
     * @param set_all_fields            Whether to populate all fields by default.
     * @param default_exercise_name     The default exercise name.
     * @param default_weight            The default weight.
     * @param default_reps              The default number of reps.
     * @param default_comment           The default comment.
     * @param set_selected_exercise     Whether to populate the currently selected exercise.
     * @param selected_exercise_id      The current selected exercise ID.
     */
    public AddLiftParams(
            boolean set_all_fields,
            String default_exercise_name,
            int default_weight,
            int default_reps,
            String default_comment,
            boolean set_selected_exercise,
            long selected_exercise_id)
    {
        this.set_all_fields = set_all_fields;
        this.default_exercise_name = default_exercise_name;
        this.default_weight = default_weight;
        this.default_reps = default_reps;
        this.default_comment = default_comment;
        this.set_selected_exercise = set_selected_exercise;
        this.selected_exercise_id = selected_exercise_id;
    }

    /**
     * Create the parameters from a copied lift.
     * @param copied_lift   The lift to copy.
     * @return              The parameters.
     */
    public static AddLiftParams createFromCopiedLift(Lift copied_lift)
    {
        return new AddLiftParams(
                true,
                copied_lift.getExercise().getName(),
                copied_lift.getWeight(),
                copied_lift.getReps(),
                copied_lift.getComment(),
                false,
                -1);
    }

    /**
     * Creates the parameters from an existing exercise.
     * @param exercise_id   The exercise ID used to populate the new lift.
     * @return              The parameters.
     */
    public static AddLiftParams createFromExistingExercise(
            long exercise_id)
    {
        return new AddLiftParams(
                false,
                "",
                -1,
                -1,
                "",
                true,
                exercise_id);
    }

    /**
     * Creates a blank set of parameters.
     * @return  The parameters.
     */
    public static AddLiftParams createBlank()
    {
        return new AddLiftParams(
                false,
                "",
                -1,
                -1,
                "",
                false,
                -1);
    }
}
