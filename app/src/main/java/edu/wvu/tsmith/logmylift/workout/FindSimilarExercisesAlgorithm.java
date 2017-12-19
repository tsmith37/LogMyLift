package edu.wvu.tsmith.logmylift.workout;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.wvu.tsmith.logmylift.LiftDbHelper;

/**
 * Created by Tommy Smith on 12/10/2017.
 * An algorithm used to predict likely coming exercises in a workout. The algorithm uses similar exercises
 * to ones that have been inserted into the algorithm. An exercise is considered similar if it has been
 * done in the same workout as another exercise. While this may initially result in many "similar" exercises
 * being added for a given exercise, the number of times the "similar" exercise has been performed is taken into
 * account. Once more data is loaded into the local database, the algorithm should become more accurate.
 * @author Tommy Smith
 */

public class FindSimilarExercisesAlgorithm implements Parcelable {
    // The exercise IDs used to add data into the algorithm.
    private ArrayList<Long> current_exercise_ids;

    // A map of similar exercise IDs to the count of sets they have accounted for.
    private Map<Long, Integer> similar_exercise_ids_to_previous_set_counts;

    FindSimilarExercisesAlgorithm()
    {
        this.current_exercise_ids = new ArrayList<>();

        // todo Change this to a different implementation?
        this.similar_exercise_ids_to_previous_set_counts = new TreeMap<>();
    }

    // Implement the functions required to parcel the algorithm, allowing it to be passed between activities.
    // This is necessary because a workout must be parcelable, and the algorithm is contained in a workout.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.current_exercise_ids);

        // Write the size of the map of similar exercise IDs to the count of previous sets.
        dest.writeInt(this.similar_exercise_ids_to_previous_set_counts.size());

        // Write the similar exercise ID and the count of previous sets for each entry in the map.
        for (Map.Entry<Long, Integer> entry : this.similar_exercise_ids_to_previous_set_counts.entrySet())
        {
            dest.writeLong(entry.getKey());
            dest.writeInt(entry.getValue());
        }
    }

    public static final Parcelable.Creator<FindSimilarExercisesAlgorithm> CREATOR = new Parcelable.Creator<FindSimilarExercisesAlgorithm>()
    {
        @Override
        public FindSimilarExercisesAlgorithm createFromParcel(Parcel source) {
            return new FindSimilarExercisesAlgorithm(source);
        }

        @Override
        public FindSimilarExercisesAlgorithm[] newArray(int size) {
            return new FindSimilarExercisesAlgorithm[size];
        }
    };

    private FindSimilarExercisesAlgorithm(Parcel source)
    {
        this.current_exercise_ids = (ArrayList<Long>) source.readSerializable();
        this.similar_exercise_ids_to_previous_set_counts = new TreeMap<>();

        // Read each entry to the map of similar exercise IDs to previous set count, and add it to the map.
        int map_size = source.readInt();
        for (int map_index = 0; map_index < map_size; ++map_index)
        {
            Long key = source.readLong();
            Integer val = source.readInt();
            this.similar_exercise_ids_to_previous_set_counts.put(key, val);
        }
    }

    /**
     * Add information to the algorithm. The input is just an exercise ID; the function uses that ID
     * to determine if similar exercises can be determined and adding the number of sets that they
     * have been done previously into the map stored by the algorithm.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise_id       The ID of the exercise to add to the algorithm.
     */
    void addInfo(LiftDbHelper lift_db_helper, Long exercise_id)
    {
        // If the current exercise is already in the similar exercises, remove it.
        boolean current_exercise_in_similar_exercises = similar_exercise_ids_to_previous_set_counts.containsKey(exercise_id);
        if (current_exercise_in_similar_exercises)
        {
            similar_exercise_ids_to_previous_set_counts.remove(exercise_id);
            return;
        }

        // Check if this exercise has already been added to the workout.
        boolean current_exercise_already_exists = current_exercise_ids.contains(exercise_id);
        if (current_exercise_already_exists)
        {
            return;
        }

        // Get exercises similar to the current one.
        Map<Long, Integer> exercises_similar_to_current = lift_db_helper.getSimilarExercises(exercise_id);
        for (Map.Entry<Long, Integer> similar_exercise_and_set_count : exercises_similar_to_current.entrySet())
        {
            Long similar_exercise_id = similar_exercise_and_set_count.getKey();

            // Don't add a similar exercise if it's already in the workout.
            boolean similar_exercise_in_current_workout = current_exercise_ids.contains(similar_exercise_id);
            if (similar_exercise_in_current_workout)
            {
                continue;
            }

            Integer set_count = similar_exercise_and_set_count.getValue();

            // Check if the similar exercise has already been found.
            boolean similar_exercise_already_exists = similar_exercise_ids_to_previous_set_counts.containsKey(similar_exercise_id);
            if (similar_exercise_already_exists)
            {
                // Increment the previous set count.
                Integer previous_set_count = similar_exercise_ids_to_previous_set_counts.get(similar_exercise_id);
                Integer new_previous_sets_count = previous_set_count + set_count;
                similar_exercise_ids_to_previous_set_counts.put(similar_exercise_id, new_previous_sets_count);
            }
            else
            {
                // Add the similar exercise.
                similar_exercise_ids_to_previous_set_counts.put(similar_exercise_id, set_count);
            }
        }

        current_exercise_ids.add(exercise_id);
    }

    /**
     * Sets the information in the algorithm based on a number of lifts.
     * @param lift_db_helper    SQLite database helper.
     * @param lift_ids          The lift IDs to add into the algorithm.
     */
    void setInfo(LiftDbHelper lift_db_helper, ArrayList<Long> lift_ids)
    {
        for (Long lift_id : lift_ids)
        {
            long exercise_id = lift_db_helper.selectExerciseIdFromLiftId(lift_id);
            boolean exercise_found = exercise_id != -1;
            if (exercise_found)
            {
                addInfo(lift_db_helper, exercise_id);
            }
        }
    }

    /**
     * Removes information from the algorithm.
     * @param lift_db_helper    SQLite database helper.
     * @param exercise_id       The ID of the exercise to remove from the algorithm.
     */
    public void removeInfo(LiftDbHelper lift_db_helper, Long exercise_id)
    {
        // For now, just don't worry about what happens if information is removed from the algorithm.
        // This should be okay because unwanted information should be limited if only a single unwanted exercise is added.
        // Also, deleting a lift is fairly uncommon anyway. This may need to be implemented at a later point.
    }

    /**
     * Returns the similar exercise IDs. Similar exercises are ones that have been done in the same
     * workout as the exercises added to this algorithm. They are sorted by the number of sets that
     * have been done in a workout along with an exercise in this algorithm.
     * @return  An array of similar exercise IDs.
     */
    ArrayList<Long> getSimilarExercises()
    {
        ArrayList<Long> similar_exercise_ids = new ArrayList<>();

        // Sort the similar exercises by the number of previous sets.
        similar_exercise_ids_to_previous_set_counts = SimilarExerciseComparator.sortByValue(similar_exercise_ids_to_previous_set_counts);

        // Iterate through the map of similar exercises.
        for (Map.Entry<Long, Integer> similar_exercise_and_set_count : similar_exercise_ids_to_previous_set_counts.entrySet())
        {
            // Add the exercise ID of the similar exercise.
            similar_exercise_ids.add(similar_exercise_and_set_count.getKey());

            // Limit the number of similar exercises to 5.
            if (similar_exercise_ids.size() > 5)
            {
                return similar_exercise_ids;
            }
        }
        return similar_exercise_ids;
    }
}

/**
 * Comparison function for the exercises, sorting them by the number of sets.
 */
class SimilarExerciseComparator
{
    static<K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K,V> o1, Map.Entry<K,V> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}