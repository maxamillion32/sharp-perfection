package com.sergeyloginov.sharpperfection.controller;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.model.Set;
import com.sergeyloginov.sharpperfection.model.Workout;
import com.sergeyloginov.sharpperfection.utils.TimerHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StartExerciseFragment extends Fragment {

    private static final String ARG_EXERCISE_TITLE = "arg_exercise_title";
    private static final String ARG_WORKOUT_DURATION_DELTA = "arg_workout_duration_delta";
    private static final String KEY_WORKOUT_DURATION_DELTA = "key_workout_duration_delta";
    private static final String KEY_EXERCISE_DURATION_DELTA = "key_exercise_duration_delta";
    private static final String KEY_SET_DURATION_DELTA = "key_set_duration_delta";
    private static final String KEY_SET_REST_DURATION_DELTA = "key_set_rest_duration_delta";
    private static final String KEY_SET = "key_set";
    private static final String KEY_SETS = "key_sets";
    private static final String KEY_ROWS = "key_rows";
    private static final String KEY_SET_IN_PROGRESS = "key_set_in_progress";
    private static final String KEY_SET_REST_IN_PROGRESS = "key_set_rest_in_progress";
    private static final String KEY_SET_RESULTS_IN_PROGRESS = "key_set_results_in_progress";
    private static final String DIALOG_RESULTS = "dialog_results";
    private static final int REQUEST_RESULTS = 0;
    private ArrayList<String> rows;
    private ArrayList<Set> sets;
    private RecyclerView rv;
    private TimerHelper thWorkoutDuration;
    private TimerHelper thExerciseDuration;
    private TimerHelper thSetDuration;
    private TimerHelper thSetRest;
    private ImageButton ibToolbarExerciseIcon;
    private TextView tvToolbarSetTitle;
    private TextView tvToolbarSetTimer;
    private FloatingActionButton fab;
    private Set set;
    private String exerciseTitle;
    private boolean setInProgress;
    private boolean setRestInProgress;
    private boolean setResultsInProgress;
    private long workoutDurationDelta;
    private long setDurationDelta;
    private long setRestDurationDelta;

    private TimerHelper.OnSecondPassListener setRestDurationListener
            = new TimerHelper.OnSecondPassListener() {
        @Override
        public void onSecondPass(int hours, int minutes, int seconds) {
            tvToolbarSetTimer.setText(String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds));
        }
    };

    public static StartExerciseFragment newInstance(String exerciseTitle,
                                                    long workoutDurationDelta) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE_TITLE, exerciseTitle);
        args.putSerializable(ARG_WORKOUT_DURATION_DELTA, workoutDurationDelta);
        StartExerciseFragment fragment = new StartExerciseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_RESULTS) {
            acceptSet(data);
        } else {
            cancelSet();
        }
        setResultsInProgress = false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseTitle = (String) getArguments().getSerializable(ARG_EXERCISE_TITLE);
        workoutDurationDelta = (long) getArguments().getSerializable(ARG_WORKOUT_DURATION_DELTA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_exercise, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (!setInProgress) {
                        finishExercise();
                    }
                }
                return false;
            }
        });

        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        long stateExerciseDurationDelta = 0;
        setDurationDelta = 0;
        setRestDurationDelta = 0;
        setInProgress = false;
        setRestInProgress = false;
        setResultsInProgress = false;
        sets = new ArrayList<>();
        rows = new ArrayList<>();
        rows.add(exerciseTitle);

        Toolbar toolbarExercise = (Toolbar) view.findViewById(R.id.exercise_toolbar);
        toolbarExercise.setContentInsetsAbsolute(0, 0);
        TextView tvToolbarExerciseTitle = (TextView) view
                .findViewById(R.id.tv_exercise_toolbar_title);
        tvToolbarExerciseTitle.setText(getResources().getString(R.string.exercise));
        final TextView tvToolbarExerciseTimer = (TextView) view
                .findViewById(R.id.tv_exercise_toolbar_timer);
        ibToolbarExerciseIcon = (ImageButton) view
                .findViewById(R.id.ib_exercise_toolbar_icon);
        ibToolbarExerciseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishExercise();
            }
        });

        Toolbar toolbarSet = (Toolbar) view.findViewById(R.id.set_toolbar);
        toolbarSet.setContentInsetsAbsolute(0, 0);
        tvToolbarSetTitle = (TextView) view
                .findViewById(R.id.tv_set_toolbar_title);
        tvToolbarSetTitle.setText(getResources().getString(R.string.set));
        tvToolbarSetTimer = (TextView) view
                .findViewById(R.id.tv_set_toolbar_timer);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });

        if (savedInstanceState != null) {
            workoutDurationDelta = savedInstanceState.getLong(KEY_WORKOUT_DURATION_DELTA);
            stateExerciseDurationDelta = savedInstanceState.getLong(KEY_EXERCISE_DURATION_DELTA);
            String jsonSets = savedInstanceState.getString(KEY_SETS);
            String jsonSet = savedInstanceState.getString(KEY_SET);
            set = new Gson().fromJson(jsonSet, new TypeToken<Set>() {}.getType());
            sets = new Gson().fromJson(jsonSets, new TypeToken<ArrayList<Set>>() {}.getType());
            rows = savedInstanceState.getStringArrayList(KEY_ROWS);
            setInProgress = savedInstanceState.getBoolean(KEY_SET_IN_PROGRESS);
            setRestInProgress = savedInstanceState.getBoolean(KEY_SET_REST_IN_PROGRESS);
            setResultsInProgress = savedInstanceState.getBoolean(KEY_SET_RESULTS_IN_PROGRESS);
            if (setInProgress) {
                setDurationDelta = savedInstanceState.getLong(KEY_SET_DURATION_DELTA);
                animateFabChangeState();
                animateFinishExerciseSqueeze();
                initializeSetDurationTimer();
            }
            if (setRestInProgress) {
                setRestDurationDelta = savedInstanceState.getLong(KEY_SET_REST_DURATION_DELTA);
                initializeSetRestDurationTimer();
                if (setResultsInProgress) {
                    animateFabSqueeze();
                }
            }
        }

        thWorkoutDuration = new TimerHelper();
        thWorkoutDuration.setDelta(workoutDurationDelta);
        thWorkoutDuration.startTimer();

        thExerciseDuration = new TimerHelper();
        thExerciseDuration.setDelta(stateExerciseDurationDelta);
        thExerciseDuration.setOnSecondPassListener(new TimerHelper.OnSecondPassListener() {
            @Override
            public void onSecondPass(int hours, int minutes, int seconds) {
                tvToolbarExerciseTimer.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, seconds));
            }
        });
        thExerciseDuration.startTimer();

        refresh();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_WORKOUT_DURATION_DELTA, thWorkoutDuration.getValue());
        outState.putLong(KEY_EXERCISE_DURATION_DELTA, thExerciseDuration.getValue());
        if (thSetDuration != null) {
            outState.putLong(KEY_SET_DURATION_DELTA, thSetDuration.getValue());
        }
        if (thSetRest != null) {
            outState.putLong(KEY_SET_REST_DURATION_DELTA, thSetRest.getValue());
        }
        String jsonSets = new Gson().toJson(sets);
        outState.putString(KEY_SETS, jsonSets);
        outState.putStringArrayList(KEY_ROWS, rows);
        outState.putBoolean(KEY_SET_IN_PROGRESS, setInProgress);
        outState.putBoolean(KEY_SET_REST_IN_PROGRESS, setRestInProgress);
        outState.putBoolean(KEY_SET_RESULTS_IN_PROGRESS, setResultsInProgress);
        if (set != null) {
            outState.putString(KEY_SET, new Gson().toJson(set));
        }
    }

    private void refresh() {
        StartExerciseRecyclerViewAdapter adapter = new StartExerciseRecyclerViewAdapter(rows);
        rv.setAdapter(adapter);
    }

    private void onFabClick() {
        if (setInProgress) {
            animateFabSqueeze();
            finishSet();
        } else {
            animateFabChangeState();
            startSet();
        }
    }

    private void finishExercise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog alert = builder
                .setTitle(R.string.finish_exercise_warning)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String jsonSets = new Gson().toJson(sets);
                        Intent intent = ChooseExerciseActivity
                                .newIntent(getActivity(), jsonSets, thWorkoutDuration.getValue());
                        thExerciseDuration.finishTimer();
                        thWorkoutDuration.finishTimer();
                        getActivity().setResult(Activity.RESULT_OK, intent);
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

    private void initializeSetDurationTimer() {
        if (thSetRest != null) {
            thSetRest.finishTimer();
        }
        if (thSetDuration != null) {
            thSetDuration.finishTimer();
        }
        tvToolbarSetTimer.setVisibility(View.INVISIBLE);
        thSetDuration = new TimerHelper();
        thSetDuration.setFormat(TimerHelper.FORMAT_MINUTES);
        thSetDuration.setDelta(setDurationDelta);
        thSetDuration.startTimer();
        animateSetDuration();
    }

    private void initializeSetRestDurationTimer() {
        if (thSetRest != null) {
            thSetRest.finishTimer();
        }
        tvToolbarSetTimer.setVisibility(View.INVISIBLE);
        thSetRest = new TimerHelper();
        thSetRest.setFormat(TimerHelper.FORMAT_MINUTES);
        thSetRest.setDelta(setRestDurationDelta);
        thSetRest.startTimer();
        animateSetRest();
    }

    public void startSet() {
        setInProgress = true;
        setRestInProgress = false;
        setRestDurationDelta = 0;
        initializeSetDurationTimer();
        set = new Set();
        set.setStart(Workout.timeFormat.format(new Date()));
        animateFinishExerciseSqueeze();
    }

    public void finishSet() {
        setInProgress = false;
        setRestInProgress = true;
        setResultsInProgress = true;
        setDurationDelta = 0;
        thSetDuration.finishTimer();
        initializeSetRestDurationTimer();
        final FragmentManager manager = getActivity().getSupportFragmentManager();
        final ResultsPickerDialogFragment dialog = new ResultsPickerDialogFragment();
        dialog.setTargetFragment(StartExerciseFragment.this, REQUEST_RESULTS);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show(manager, DIALOG_RESULTS);
            }
        }, 100);
        set.setFinish(Workout.timeFormat.format(new Date()));
    }

    private void acceptSet(Intent data) {
        String weight = data.getStringExtra(ResultsPickerDialogFragment.EXTRA_WEIGHT);
        String reps = data.getStringExtra(ResultsPickerDialogFragment.EXTRA_REPS);
        set.setReps(reps);
        set.setWeight(weight);
        sets.add(set);
        long duration = 0;
        try {
            duration = (Workout.timeFormat.parse(set.getFinish()).getTime()
                    - Workout.timeFormat.parse(set.getStart()).getTime()) / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        int min = (int) duration / 60;
        int sec = (int) duration % 60;
        String res = getResources().getString(R.string.set)
                + ": " + String.format(Locale.getDefault(), "%02d:%02d", min, sec)
                + " / " + set.getWeight() + " x " + set.getReps();
        rows.add(res);
        refresh();
        animateFabExpand();
        animateFinishExerciseExpand();
    }

    private void cancelSet() {
        if (sets.size() != 0) {
            tvToolbarSetTitle.setText(getResources().getString(R.string.set_rest));
            thSetRest.finishTimer();
            thSetRest = new TimerHelper();
            try {
                thSetRest.setDelta(new Date().getTime()
                        - (Workout.timeFormat.parse(
                                sets.get(sets.size() - 1).getFinish()).getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            thSetRest.startTimer();
            animateCancelSetNotZero();
        } else {
            animateCancelSetZero();
            thSetRest.finishTimer();
            setRestInProgress = false;
        }
        animateFabExpand();
        animateFinishExerciseExpand();
    }

    private Animation getFadeInAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new FastOutLinearInInterpolator());
        fadeIn.setDuration(250);
        return fadeIn;
    }
    
    private Animation getFadeOutAnimation() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new FastOutLinearInInterpolator());
        fadeOut.setDuration(250);
        return fadeOut;
    }

    private void animateCancelSetZero() {
        tvToolbarSetTimer.setVisibility(View.INVISIBLE);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new FastOutLinearInInterpolator());
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvToolbarSetTitle.setText(getResources().getString(R.string.set));
                tvToolbarSetTitle.setAnimation(getFadeInAnimation());
                tvToolbarSetTimer.setText("");
                tvToolbarSetTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvToolbarSetTimer.setAnimation(getFadeOutAnimation());
        tvToolbarSetTitle.setAnimation(fadeOut);
    }

    private void animateCancelSetNotZero() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new FastOutLinearInInterpolator());
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvToolbarSetTimer.setText(thSetRest.getTime(TimerHelper.FORMAT_MINUTES));
                thSetRest.setOnSecondPassListener(setRestDurationListener);
                tvToolbarSetTimer.setAnimation(getFadeInAnimation());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvToolbarSetTimer.setAnimation(fadeOut);
    }

    private void animateSetDuration() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new FastOutLinearInInterpolator());
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvToolbarSetTimer.setText(R.string.min_duration);
                tvToolbarSetTitle.setAnimation(getFadeInAnimation());
                tvToolbarSetTimer.setAnimation(getFadeInAnimation());
                tvToolbarSetTitle.setText(getResources().getString(R.string.set_duration));
                tvToolbarSetTimer.setVisibility(View.VISIBLE);
                thSetDuration.setOnSecondPassListener(setRestDurationListener);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvToolbarSetTitle.setAnimation(fadeOut);
        tvToolbarSetTimer.setAnimation(getFadeOutAnimation());
    }

    private void animateSetRest() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new FastOutLinearInInterpolator());
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvToolbarSetTimer.setText(R.string.min_duration);
                tvToolbarSetTitle.setAnimation(getFadeInAnimation());
                tvToolbarSetTimer.setAnimation(getFadeInAnimation());
                tvToolbarSetTitle.setText(getResources().getString(R.string.set_rest));
                tvToolbarSetTimer.setVisibility(View.VISIBLE);
                thSetRest.setOnSecondPassListener(setRestDurationListener);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvToolbarSetTitle.setAnimation(fadeOut);
        tvToolbarSetTimer.setAnimation(getFadeOutAnimation());
    }

    private void animateFabChangeState() {
        fab.setClickable(false);
        final ScaleAnimation expand = new ScaleAnimation(
                0.2f, 1f, 0.2f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setInterpolator(new FastOutLinearInInterpolator());
        expand.setDuration(100);
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        ScaleAnimation squeeze = new ScaleAnimation(
                1f, 0.2f, 1f, 0.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        squeeze.setInterpolator(new FastOutLinearInInterpolator());
        squeeze.setDuration(100);
        squeeze.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setImageResource(R.drawable.ic_check);
                fab.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        fab.startAnimation(squeeze);
    }

    private void animateFabSqueeze() {
        fab.setClickable(false);
        ObjectAnimator animX = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0f);
        animX.setInterpolator(new FastOutLinearInInterpolator());
        animY.setInterpolator(new FastOutLinearInInterpolator());
        animX.setDuration(50);
        animY.setDuration(50);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.start();
    }

    private void animateFinishExerciseSqueeze() {
        ibToolbarExerciseIcon.setClickable(false);
        ObjectAnimator animX = ObjectAnimator.ofFloat(ibToolbarExerciseIcon, "scaleX", 1f, 0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(ibToolbarExerciseIcon, "scaleY", 1f, 0f);
        animX.setInterpolator(new FastOutLinearInInterpolator());
        animY.setInterpolator(new FastOutLinearInInterpolator());
        animX.setDuration(100);
        animY.setDuration(100);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.start();
    }

    private void animateFabExpand() {
        fab.setImageResource(R.drawable.ic_add);
        ObjectAnimator animX = ObjectAnimator.ofFloat(fab, "scaleX", 0f, 1f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(fab, "scaleY", 0f, 1f);
        animX.setInterpolator(new FastOutLinearInInterpolator());
        animY.setInterpolator(new FastOutLinearInInterpolator());
        animX.setDuration(100);
        animY.setDuration(100);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.start();
        fab.setClickable(true);
    }

    private void animateFinishExerciseExpand() {
        ObjectAnimator animX = ObjectAnimator.ofFloat(ibToolbarExerciseIcon, "scaleX", 0f, 1f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(ibToolbarExerciseIcon, "scaleY", 0f, 1f);
        animX.setInterpolator(new FastOutLinearInInterpolator());
        animY.setInterpolator(new FastOutLinearInInterpolator());
        animX.setDuration(100);
        animY.setDuration(100);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.start();
        ibToolbarExerciseIcon.setClickable(true);
    }

    private class StartExerciseRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView tv;

        StartExerciseRecyclerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.rv_item);
        }

        void bind(String row) {
            tv.setText(row);
        }
    }

    private class StartExerciseRecyclerViewAdapter
            extends RecyclerView.Adapter<StartExerciseRecyclerViewHolder> {

        private ArrayList<String> rows;

        StartExerciseRecyclerViewAdapter(ArrayList<String> rows) {
            this.rows = rows;
        }

        @Override
        public StartExerciseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.rv_start_exercise_item, parent, false);
            return new StartExerciseRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StartExerciseRecyclerViewHolder holder, int position) {
            String row = rows.get(position);
            holder.bind(row);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}
