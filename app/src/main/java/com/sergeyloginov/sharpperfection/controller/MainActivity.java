package com.sergeyloginov.sharpperfection.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    public static final String EXTRA_JSON =
            "com.sergeyloginov.sharpperfection.controller.extra_json";
    public static final String EXTRA_IS_FAVOURITE_ENABLED =
            "com.sergeyloginov.sharpperfection.controller.extra_is_favourite_enabled";
    public static final String EXTRA_IS_DATE_ENABLED =
            "com.sergeyloginov.sharpperfection.controller.extra_is_date_enabled";
    public static final String EXTRA_DATE_FROM =
            "com.sergeyloginov.sharpperfection.controller.extra_date_from";
    public static final String EXTRA_DATE_BY =
            "com.sergeyloginov.sharpperfection.controller.extra_date_by";

    public static Intent newIntent(Context context, String json) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_JSON, json);
        return intent;
    }

    public static Intent newIntent(Context context,
                                   boolean isFavouriteEnabled, boolean isDateEnabled,
                                   String dateFrom, String dateBy) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_IS_FAVOURITE_ENABLED, isFavouriteEnabled);
        intent.putExtra(EXTRA_IS_DATE_ENABLED, isDateEnabled);
        intent.putExtra(EXTRA_DATE_FROM, dateFrom);
        intent.putExtra(EXTRA_DATE_BY, dateBy);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
