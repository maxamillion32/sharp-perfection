package com.sergeyloginov.sharpperfection.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class StartExerciseActivity extends SingleFragmentActivity {

    private static final String EXTRA_EXERCISE_TITLE =
            "com.sergeyloginov.sharpperfection.controller.extra_exercise_title";
    private static final String EXTRA_WORKOUT_DURATION_DELTA =
            "com.sergeyloginov.sharpperfection.controller.extra_workout_duration_delta";

    public static Intent newIntent(Context context,
                                   String exerciseTitle, long workoutDurationDelta) {
        Intent intent = new Intent(context, StartExerciseActivity.class);
        intent.putExtra(EXTRA_EXERCISE_TITLE, exerciseTitle);
        intent.putExtra(EXTRA_WORKOUT_DURATION_DELTA, workoutDurationDelta);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        String exerciseTitle = (String) getIntent().getSerializableExtra(EXTRA_EXERCISE_TITLE);
        long workoutDurationDelta = (long) getIntent()
                .getSerializableExtra(EXTRA_WORKOUT_DURATION_DELTA);
        return StartExerciseFragment.newInstance(exerciseTitle, workoutDurationDelta);
    }

    @Override
    public void onBackPressed() {

    }
}
