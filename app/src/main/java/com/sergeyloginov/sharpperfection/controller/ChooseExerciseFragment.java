package com.sergeyloginov.sharpperfection.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.model.Exercise;
import com.sergeyloginov.sharpperfection.model.Set;
import com.sergeyloginov.sharpperfection.model.Workout;
import com.sergeyloginov.sharpperfection.utils.TimerHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChooseExerciseFragment extends Fragment {

    private static final String KEY_WORKOUT_DURATION_DELTA = "key_workout_duration_delta";
    private static final String KEY_EXERCISE = "key_exercise";
    private static final String KEY_EXERCISES = "key_exercises";
    private static final String KEY_SETS = "key_sets";
    private static final int REQUEST_CODE = 10;
    private TimerHelper thWorkoutDuration;
    private Exercise exercise;
    private ArrayList<Set> sets;
    private ArrayList<Exercise> exercises;
    private TextView tvToolbarTimer;
    private int lastExpandedPosition = -1;
    private long workoutDurationDelta;
    private String[] muscleGroups;
    private RecyclerView rv;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            String jsonSets = data.getStringExtra(ChooseExerciseActivity.EXTRA_SETS);
            workoutDurationDelta = data
                    .getLongExtra(ChooseExerciseActivity.EXTRA_WORKOUT_DURATION_DELTA, 0);
            if (thWorkoutDuration != null) {
                thWorkoutDuration.finishTimer();
            }
            initializeWorkoutDurationTimer();
            ArrayList<Set> setsTemp = new Gson().fromJson(jsonSets,
                    new TypeToken<ArrayList<Set>>(){}.getType());
            if (setsTemp.size() != 0) {
                exercise.setFinish(Workout.timeFormat.format(new Date()));
                exercise.addSets(setsTemp);
                exercises.add(exercise);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_exercise, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    finishWorkout();
                }
                return false;
            }
        });

        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        exercises = new ArrayList<>();
        sets = new ArrayList<>();
        workoutDurationDelta = 0;

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        ImageButton ibFinishWorkout = (ImageButton) view.findViewById(R.id.ib_toolbar_icon);
        ibFinishWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWorkout();
            }
        });
        TextView tvToolbarTitle = (TextView) view.findViewById(R.id.tv_toolbar_title);
        tvToolbarTitle.setText(getResources().getString(R.string.workout));
        tvToolbarTimer = (TextView) view.findViewById(R.id.tv_toolbar_timer);

        if (savedInstanceState != null) {
            workoutDurationDelta = savedInstanceState.getLong(KEY_WORKOUT_DURATION_DELTA);
            String jsonExercises = savedInstanceState.getString(KEY_EXERCISES);
            String jsonSets = savedInstanceState.getString(KEY_SETS);
            exercises = new Gson().fromJson(jsonExercises,
                    new TypeToken<ArrayList<Exercise>>(){}.getType());
            sets = new Gson().fromJson(jsonSets, new TypeToken<ArrayList<Set>>(){}.getType());
            String jsonExercise = savedInstanceState.getString(KEY_EXERCISE);
            exercise = new Gson().fromJson(jsonExercise, new TypeToken<Exercise>(){}.getType());
        }
        initializeWorkoutDurationTimer();
        fillList();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_WORKOUT_DURATION_DELTA, thWorkoutDuration.getValue());
        thWorkoutDuration.finishTimer();
        if (exercise != null) {
            String jsonExercise = new Gson().toJson(exercise);
            outState.putString(KEY_EXERCISE, jsonExercise);
        }
        String jsonExercises = new Gson().toJson(exercises);
        String jsonSets = new Gson().toJson(sets);
        outState.putString(KEY_EXERCISES, jsonExercises);
        outState.putString(KEY_SETS, jsonSets);
    }

    private void initializeWorkoutDurationTimer() {
        thWorkoutDuration = new TimerHelper();
        thWorkoutDuration.setDelta(workoutDurationDelta);
        thWorkoutDuration.setOnSecondPassListener(new TimerHelper.OnSecondPassListener() {
            @Override
            public void onSecondPass(int hours, int minutes, int seconds) {
                tvToolbarTimer.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, seconds));
            }
        });
        thWorkoutDuration.startTimer();
    }

    public void finishWorkout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog alert = builder
                .setTitle(R.string.finish_workout_warning)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        thWorkoutDuration.finishTimer();
                        getActivity().setResult(Activity.RESULT_OK, MainActivity
                                .newIntent(getActivity(), new Gson().toJson(exercises)));
                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
                alert.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
            }
        });
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    }

    private void fillList() {
        muscleGroups = getResources().getStringArray(R.array.groups);
        List<String[]> exercises = new ArrayList<>();
        List<ParentListItem> parents = new ArrayList<>();

        exercises.add(getResources().getStringArray(R.array.biceps));
        exercises.add(getResources().getStringArray(R.array.triceps));
        exercises.add(getResources().getStringArray(R.array.shoulders));
        exercises.add(getResources().getStringArray(R.array.chest));
        exercises.add(getResources().getStringArray(R.array.back));
        exercises.add(getResources().getStringArray(R.array.legs));
        exercises.add(getResources().getStringArray(R.array.buttocks));
        exercises.add(getResources().getStringArray(R.array.abdomen));

        for (int i = 0; i < muscleGroups.length; i++) {
            List<MuscleGroupExercise> childList = new ArrayList<>();
            for (int j = 0; j < exercises.get(i).length; j++) {
                childList.add(new MuscleGroupExercise(exercises.get(i)[j], i));
            }
            MuscleGroup muscleGroup = new MuscleGroup(muscleGroups[i], childList);
            parents.add(muscleGroup);
        }

        final ChooseExerciseAdapter adapter = new ChooseExerciseAdapter(getActivity(), parents);
        adapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                if (lastExpandedPosition != -1 && position != lastExpandedPosition) {
                    adapter.collapseParent(lastExpandedPosition);
                }
                lastExpandedPosition = position;
            }

            @Override
            public void onListItemCollapsed(int position) {}
        });
        rv.setAdapter(adapter);
    }

    private class MuscleGroup implements ParentListItem {

        private String title;
        private List<MuscleGroupExercise> childListItem;

        MuscleGroup(String title, List<MuscleGroupExercise> childListItem) {
            this.title = title;
            this.childListItem = childListItem;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public List<?> getChildItemList() {
            return childListItem;
        }

        @Override
        public boolean isInitiallyExpanded() {
            return false;
        }
    }

    private class MuscleGroupExercise {

        private String title;
        private int parentId;

        MuscleGroupExercise(String title, int parentId) {
            this.title = title;
            this.parentId = parentId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        int getParentId() {
            return parentId;
        }
    }

    private class MuscleGroupHolder extends ParentViewHolder {

        private TextView tvGroupTitle;
        private ImageView ivExpand;
        private View topDivider;

        MuscleGroupHolder(View itemView) {
            super(itemView);
            tvGroupTitle = (TextView) itemView.findViewById(R.id.tv_group_title);
            topDivider = itemView.findViewById(R.id.top_divider);
            ivExpand = (ImageView) itemView.findViewById(R.id.iv_expand);
        }

        @Override
        public void setExpanded(boolean expanded) {
            super.setExpanded(expanded);
            if (expanded) {
                ivExpand.setRotation(180f);
                if (getAdapterPosition() != 0) {
                    topDivider.setVisibility(View.VISIBLE);
                }
            } else {
                ivExpand.setRotation(0f);
                topDivider.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onExpansionToggled(boolean expanded) {
            super.onExpansionToggled(expanded);
            RotateAnimation rotate;
            AlphaAnimation alpha;
            if (expanded) {
                rotate = new RotateAnimation(
                        180f, 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                alpha = new AlphaAnimation(1f, 0f);
            } else {
                rotate = new RotateAnimation(
                        -1 * 180f, 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                alpha = new AlphaAnimation(0f, 1f);
            }
            rotate.setInterpolator(new FastOutLinearInInterpolator());
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            ivExpand.startAnimation(rotate);

            alpha.setInterpolator(new FastOutLinearInInterpolator());
            alpha.setDuration(300);
            alpha.setFillAfter(true);

            if (getAdapterPosition() != 0) {
                topDivider.startAnimation(alpha);
            }
        }
    }

    private class MuscleGroupExerciseHolder extends ChildViewHolder
            implements View.OnClickListener {

        private TextView tvChildTitle;
        private View bottomDivider;

        MuscleGroupExerciseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvChildTitle = (TextView) itemView.findViewById(R.id.tv_child_title);
            bottomDivider = itemView.findViewById(R.id.bottom_divider);
        }

        @Override
        public void onClick(View v) {
            FrameLayout fl = (FrameLayout) v;
            TextView tv = (TextView) fl.findViewById(R.id.tv_child_title);

            exercise = new Exercise();
            exercise.setStart(Workout.timeFormat.format(new Date()));
            exercise.setTitle(tv.getText().toString());
            exercise.setLabel(muscleGroups[lastExpandedPosition]);

            startActivityForResult(StartExerciseActivity.newIntent(
                    getActivity(), exercise.getTitle(),
                    thWorkoutDuration.getValue()), REQUEST_CODE);

            thWorkoutDuration.finishTimer();
        }
    }

    private class ChooseExerciseAdapter
            extends ExpandableRecyclerAdapter<MuscleGroupHolder, MuscleGroupExerciseHolder> {

        private LayoutInflater inflater;

        ChooseExerciseAdapter(Context context, List<ParentListItem> itemList) {
            super(itemList);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public MuscleGroupHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
            View view = inflater.inflate(R.layout.rv_choose_exercise_group, parentViewGroup, false);
            return new MuscleGroupHolder(view);
        }

        @Override
        public MuscleGroupExerciseHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
            View view = inflater.inflate(R.layout.rv_choose_exercise_child, childViewGroup, false);
            return new MuscleGroupExerciseHolder(view);
        }

        @Override
        public void onBindParentViewHolder(MuscleGroupHolder parentViewHolder,
                                           int position, ParentListItem parentListItem) {
            MuscleGroup group = (MuscleGroup) parentListItem;
            parentViewHolder.tvGroupTitle.setText(group.getTitle());
        }

        @Override
        public void onBindChildViewHolder(MuscleGroupExerciseHolder childViewHolder,
                                          int position, Object childListItem) {
            MuscleGroupExercise exercise = (MuscleGroupExercise) childListItem;
            childViewHolder.tvChildTitle.setText(exercise.getTitle());
            if (position == getParentItemList().get(exercise.getParentId())
                    .getChildItemList().size() + exercise.getParentId()) {
                childViewHolder.bottomDivider.setVisibility(View.VISIBLE);
            } else {
                childViewHolder.bottomDivider.setVisibility(View.INVISIBLE);
            }
        }
    }
}
