package com.sergeyloginov.sharpperfection.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class ViewWorkoutActivity extends SingleFragmentActivity {

    private static final String EXTRA_WORKOUT =
            "com.sergeyloginov.sharpperfection.controller.workouts";

    public static Intent newIntent(Context context, String jsonWorkout) {
        Intent intent = new Intent(context, ViewWorkoutActivity.class);
        intent.putExtra(EXTRA_WORKOUT, jsonWorkout);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        String jsonWorkout = (String) getIntent().getSerializableExtra(EXTRA_WORKOUT);
        return ViewWorkoutFragment.newInstance(jsonWorkout);
    }
}
