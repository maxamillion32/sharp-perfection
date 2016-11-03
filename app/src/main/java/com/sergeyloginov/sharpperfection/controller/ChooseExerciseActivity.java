package com.sergeyloginov.sharpperfection.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class ChooseExerciseActivity extends SingleFragmentActivity {

    public static final String EXTRA_SETS =
            "com.sergeyloginov.sharpperfection.controller.extra_sets";
    public static final String EXTRA_WORKOUT_DURATION_DELTA =
            "com.sergeyloginov.sharpperfection.controller.extra_workout_duration_delta";

    public static Intent newIntent(Context context) {
        return new Intent(context, ChooseExerciseActivity.class);
    }

    public static Intent newIntent(Context context, String jsonSets, long workoutDurationDelta) {
        Intent intent = new Intent(context, ChooseExerciseActivity.class);
        intent.putExtra(EXTRA_SETS, jsonSets);
        intent.putExtra(EXTRA_WORKOUT_DURATION_DELTA, workoutDurationDelta);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new ChooseExerciseFragment();
    }

    @Override
    public void onBackPressed() {

    }
}
