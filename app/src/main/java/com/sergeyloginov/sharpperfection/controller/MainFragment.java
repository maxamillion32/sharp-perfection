package com.sergeyloginov.sharpperfection.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.auth.LoginActivity;
import com.sergeyloginov.sharpperfection.holder.MainRecyclerViewHolder;
import com.sergeyloginov.sharpperfection.model.Exercise;
import com.sergeyloginov.sharpperfection.model.Set;
import com.sergeyloginov.sharpperfection.model.Workout;
import com.sergeyloginov.sharpperfection.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    private static final int REQUEST_CODE_CHOOSE_EXERCISE = 10;
    private static final int REQUEST_CODE_VIEW_WORKOUT = 20;
    private static final int REQUEST_CODE_FILTERS = 30;
    private static final String CONTEXT_PREFERENCES = "filters";
    private static final String PREF_FAVOURITE = "prefFavourite";
    private static final String PREF_DATE = "prefDate";
    private static final String PREF_DATE_FROM = "prefDateFrom";
    private static final String PREF_DATE_BY = "prefDateBy";
    private static final String KEY_IS_WORKOUT_STARTED = "key_is_workout_started";
    private static final String KEY_IS_FAVOURITE_LIST = "key_is_favourite_list";
    private static final String KEY_IS_FILTER_LIST = "key_is_filter_list";
    private static final String KEY_FILTER_DATE_FROM = "key_filter_date_from";
    private static final String KEY_FILTER_DATE_BY = "key_filter_date_by";
    private static final String KEY_WORKOUT_START_DATE = "key_workout_start_date";
    private static final String FB_WORKOUTS = "workouts";
    private static final String FB_EXERCISES = "exercises";
    private static final String FB_SETS = "sets";
    private static final String FB_DATE = "date";
    private static final String FB_FAVOURITE = "favourite";
    private SharedPreferences filtersPref;
    private RecyclerView rv;
    private Workout workout;
    private String userId;
    private String dateFrom;
    private String dateBy;
    private boolean isFavouriteList = false;
    private boolean isFilterList = false;
    private boolean isWorkoutStarted = false;
    private DatabaseReference reference;
    private Query allWorkouts;
    private Query favouriteWorkouts;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHOOSE_EXERCISE) {
            isWorkoutStarted = false;
            String jsonExercises = data.getStringExtra(MainActivity.EXTRA_JSON);
            ArrayList<Exercise> exercises = new Gson().fromJson(
                    jsonExercises,
                    new TypeToken<ArrayList<Exercise>>(){}.getType());
            if (exercises.size() != 0) {
                writeData(exercises);
            }
        } else if (requestCode == REQUEST_CODE_VIEW_WORKOUT) {
            Workout w = new Gson().fromJson(
                    data.getStringExtra(MainActivity.EXTRA_JSON),
                    new TypeToken<Workout>(){}.getType());
            deleteData(w);
        } else if (requestCode == REQUEST_CODE_FILTERS) {
            isFavouriteList = data.getBooleanExtra(MainActivity.EXTRA_IS_FAVOURITE_ENABLED, false);
            isFilterList = data.getBooleanExtra(MainActivity.EXTRA_IS_DATE_ENABLED, false);
            dateFrom = data.getStringExtra(MainActivity.EXTRA_DATE_FROM);
            dateBy = data.getStringExtra(MainActivity.EXTRA_DATE_BY);
            filtersPref.edit().putBoolean(PREF_FAVOURITE, isFavouriteList).apply();
            filtersPref.edit().putBoolean(PREF_DATE, isFilterList).apply();
            filtersPref.edit().putString(PREF_DATE_FROM, dateFrom).apply();
            filtersPref.edit().putString(PREF_DATE_BY, dateBy).apply();
        }
        refresh();
    }

    private void writeData(ArrayList<Exercise> exercises) {
        workout.setDate(Workout.dateFormat.format(new Date()));
        workout.setFinish(Workout.timeFormat.format(new Date()));
        for (Exercise exercise : exercises) {
            workout.addLabel(exercise.getLabel());
        }
        workout.createLabels();

        String workoutId = reference.child(FB_WORKOUTS + "/" + userId).push().getKey();
        workout.setId(workoutId);
        Map<String, Object> map = new HashMap<>();

        for (Exercise exercise : exercises) {
            String exerciseId = reference.child(FB_EXERCISES + "/" + userId).push().getKey();
            exercise.setId(exerciseId);
            map = new HashMap<>();
            map.put("/" + FB_EXERCISES + "/" + userId + "/" + workoutId + "/" + exerciseId,
                    exercise.toMap());
            reference.updateChildren(map);

            for (Set set : exercise.getSets()) {
                String setId = reference.child(FB_SETS).push().getKey();
                set.setId(setId);
                map = new HashMap<>();
                map.put("/" + FB_SETS + "/" + userId + "/" + exerciseId + "/" + setId, set.toMap());
                reference.updateChildren(map);
            }
        }

        workout.addExercises(new Gson().toJson(exercises));
        map.put("/" + FB_WORKOUTS + "/" + userId + "/" + workoutId, workout.toMap());
        reference.updateChildren(map);
    }

    private void deleteData(Workout w) {
        ArrayList<Exercise> exercises = new Gson().fromJson(
                w.getExercises(),
                new TypeToken<ArrayList<Exercise>>(){}.getType());

        Map<String, Object> map = new HashMap<>();
        map.put("/" + FB_WORKOUTS + "/" + userId + "/" + w.getId(), null);
        reference.updateChildren(map);

        for (Exercise exercise : exercises) {
            map = new HashMap<>();
            map.put("/" + FB_EXERCISES + "/" + userId + "/" + w.getId() + "/" + exercise.getId(),
                    null);
            reference.updateChildren(map);
            for (Set set : exercise.getSets()) {
                map = new HashMap<>();
                map.put("/" + FB_SETS + "/" + userId + "/" + exercise.getId() + "/" + set.getId(),
                        null);
                reference.updateChildren(map);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        } else {
            final ProgressBar pb = (ProgressBar) view.findViewById(R.id.pb);
            pb.setVisibility(View.VISIBLE);
            userId = auth.getCurrentUser().getUid();
            reference = FirebaseDatabase.getInstance().getReference();
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    pb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            filtersPref = getActivity()
                    .getSharedPreferences(CONTEXT_PREFERENCES, Context.MODE_PRIVATE);

            rv = (RecyclerView) view.findViewById(R.id.rv);
            rv.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), 72));
            rv.setHasFixedSize(true);

            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.all_workouts);
            toolbar.inflateMenu(R.menu.fragment_main);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.item_filters:
                            startActivityForResult(new Intent(getActivity(),
                                    FiltersActivity.class), REQUEST_CODE_FILTERS);
                            return true;
                    }
                    return true;
                }
            });

            view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isWorkoutStarted = true;
                    workout = new Workout();
                    workout.setStart(Workout.timeFormat.format(new Date()));
                    startActivityForResult(ChooseExerciseActivity.newIntent(getActivity()),
                            REQUEST_CODE_CHOOSE_EXERCISE);
                }
            });

            allWorkouts = reference.child(FB_WORKOUTS).child(userId);
            favouriteWorkouts = reference.child(FB_WORKOUTS)
                    .child(userId).orderByChild(FB_FAVOURITE).equalTo(true);

            if (savedInstanceState != null) {
                isFavouriteList = savedInstanceState.getBoolean(KEY_IS_FAVOURITE_LIST);
                isFilterList = savedInstanceState.getBoolean(KEY_IS_FILTER_LIST);
                if (isFilterList) {
                    dateFrom = savedInstanceState.getString(KEY_FILTER_DATE_FROM);
                    dateBy = savedInstanceState.getString(KEY_FILTER_DATE_BY);
                }
                isWorkoutStarted = savedInstanceState.getBoolean(KEY_IS_WORKOUT_STARTED);
                if (isWorkoutStarted) {
                    workout = new Workout();
                    workout.setStart(savedInstanceState.getString(KEY_WORKOUT_START_DATE));
                }
            } else {
                isFavouriteList = filtersPref.getBoolean(PREF_FAVOURITE, false);
                isFilterList = filtersPref.getBoolean(PREF_DATE, false);
                if (isFilterList) {
                    dateFrom = filtersPref.getString(PREF_DATE_FROM, "");
                    dateBy = filtersPref.getString(PREF_DATE_BY, "");
                }
            }
            refresh();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_FAVOURITE_LIST, isFavouriteList);
        if (isFilterList) {
            outState.putBoolean(KEY_IS_FILTER_LIST, true);
            outState.putString(KEY_FILTER_DATE_FROM, dateFrom);
            outState.putString(KEY_FILTER_DATE_BY, dateBy);
        }
        if (isWorkoutStarted) {
            outState.putBoolean(KEY_IS_WORKOUT_STARTED, true);
            outState.putString(KEY_WORKOUT_START_DATE, workout.getStart());
        }
    }

    private void refresh() {
        if (isFavouriteList) {
            initAdapter(favouriteWorkouts);
        } else if (isFilterList) {
            initAdapter(sortedByDateWorkouts(dateFrom, dateBy));
        } else {
            initAdapter(allWorkouts);
        }
    }

    private Query sortedByDateWorkouts(String dateFrom, String dateBy) {
        return reference.child(FB_WORKOUTS).child(userId)
                .orderByChild(FB_DATE).startAt(dateFrom).endAt(dateBy);
    }

    private void initAdapter(final Query query) {
        final FirebaseRecyclerAdapter<Workout, MainRecyclerViewHolder> adapter
                = new FirebaseRecyclerAdapter<Workout, MainRecyclerViewHolder>(Workout.class,
                R.layout.rv_main_item, MainRecyclerViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final MainRecyclerViewHolder viewHolder,
                                              final Workout model, int position) {
                viewHolder.bind(getActivity(), model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.isFavourite()) {
                            viewHolder.disableFavourite();
                        } else {
                            viewHolder.enableFavourite();
                        }
                        model.setFavourite(!model.isFavourite());
                        Map<String, Object> map = new HashMap<>();
                        map.put("/" + FB_WORKOUTS + "/" + userId + "/" + model.getId(),
                                model.toMap());
                        reference.updateChildren(map);
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String jsonWorkout = new Gson().toJson(model);
                        startActivityForResult(ViewWorkoutActivity
                                        .newIntent(getActivity(), jsonWorkout),
                                REQUEST_CODE_VIEW_WORKOUT);
                    }
                });
            }
        };
        rv.setAdapter(adapter);
    }
}
