package com.sergeyloginov.sharpperfection.controller;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.model.Workout;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class FiltersActivity extends AppCompatActivity {

    private static final String KEY_SW_FAVOURITE_CHECKED = "swFavouriteChecked";
    private static final String KEY_SW_DATE_CHECKED = "swDateChecked";
    private static final String KEY_TV_DATE_FROM_TEXT = "tvDateFromText";
    private static final String KEY_TV_DATE_BY_TEXT = "tvDateByText";
    private static final String CONTEXT_PREFERENCES = "filters";
    private static final String PREF_FAVOURITE = "prefFavourite";
    private static final String PREF_DATE = "prefDate";
    private static final String PREF_DATE_FROM = "prefDateFrom";
    private static final String PREF_DATE_BY = "prefDateBy";
    private SharedPreferences filtersPref;
    private SwitchCompat swFavourite;
    private SwitchCompat swDate;
    private TextView tvFrom;
    private TextView tvBy;
    private TextView tvDateFrom;
    private TextView tvDateBy;
    private boolean isFavouriteEnabled = false;
    private boolean isDateEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        filtersPref = getSharedPreferences(CONTEXT_PREFERENCES, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.filters);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvBy = (TextView) findViewById(R.id.tv_by);
        tvDateFrom = (TextView) findViewById(R.id.tv_date_from);
        tvDateFrom.setText(Workout.dateFormat.format(new Date()));
        tvDateBy = (TextView) findViewById(R.id.tv_date_by);
        tvDateBy.setText(Workout.dateFormat.format(new Date()));
        swFavourite = (SwitchCompat) findViewById(R.id.sw_favourite);
        swFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isFavouriteEnabled = b;
                if (isFavouriteEnabled) {
                    enableFavourite();
                } else {
                    disableFavourite();
                }
            }
        });
        swDate = (SwitchCompat) findViewById(R.id.sw_date);
        swDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isDateEnabled = b;
                if (isDateEnabled) {
                    enableFilterByDate();
                } else {
                    disableFilterByDate();
                }
            }
        });

        if (savedInstanceState != null) {
            swFavourite.setChecked(savedInstanceState.getBoolean(KEY_SW_FAVOURITE_CHECKED));
            swDate.setChecked(savedInstanceState.getBoolean(KEY_SW_DATE_CHECKED));
            tvDateFrom.setText(savedInstanceState.getString(KEY_TV_DATE_FROM_TEXT));
            tvDateBy.setText(savedInstanceState.getString(KEY_TV_DATE_BY_TEXT));
            if (swDate.isChecked()) {
                enableFilterByDate();
            } else {
                disableFilterByDate();
            }
        } else {
            swFavourite.setChecked(filtersPref.getBoolean(PREF_FAVOURITE, false));
            swDate.setChecked(filtersPref.getBoolean(PREF_DATE, false));
            if (swDate.isChecked()) {
                tvDateFrom.setText(filtersPref.getString(PREF_DATE_FROM,
                        Workout.dateFormat.format(new Date())));
                tvDateBy.setText(filtersPref.getString(PREF_DATE_BY,
                        Workout.dateFormat.format(new Date())));
            }
        }
    }

    private void enableFavourite() {
        isFavouriteEnabled = true;
        filtersPref.edit().putBoolean(PREF_FAVOURITE, isFavouriteEnabled).apply();
        if (swDate.isChecked()) {
            disableFilterByDate();
        }
    }

    private void disableFavourite() {
        isFavouriteEnabled = false;
        filtersPref.edit().putBoolean(PREF_FAVOURITE, isFavouriteEnabled).apply();
        swFavourite.setChecked(false);
    }

    private void enableFilterByDate() {
        isDateEnabled = true;
        filtersPref.edit().putBoolean(PREF_DATE, isDateEnabled).apply();
        tvFrom.setTextColor(ContextCompat.getColor(
                getApplicationContext(), android.R.color.black));
        tvBy.setTextColor(ContextCompat.getColor(
                getApplicationContext(), android.R.color.black));
        tvDateFrom.setTextColor(ContextCompat.getColor(
                getApplicationContext(), android.R.color.black));
        tvDateBy.setTextColor(ContextCompat.getColor(
                getApplicationContext(), android.R.color.black));
        tvDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar(tvDateFrom);
            }
        });
        tvDateBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar(tvDateBy);
            }
        });
        if (swFavourite.isChecked()) {
            disableFavourite();
        }
    }

    private void disableFilterByDate() {
        isDateEnabled = false;
        filtersPref.edit().putBoolean(PREF_DATE, isDateEnabled).apply();
        swDate.setChecked(false);
        tvFrom.setTextColor(ContextCompat.getColor(
                getApplicationContext(), R.color.disabled));
        tvBy.setTextColor(ContextCompat.getColor(
                getApplicationContext(), R.color.disabled));
        tvDateFrom.setTextColor(ContextCompat.getColor(
                getApplicationContext(), R.color.disabled));
        tvDateBy.setTextColor(ContextCompat.getColor(
                getApplicationContext(), R.color.disabled));
        tvDateFrom.setOnClickListener(null);
        tvDateBy.setOnClickListener(null);
    }

    private void showCalendar(final TextView tv) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        tv.setText(Workout.dateFormat.format(calendar.getTime()));
                        try {
                            if (Workout.dateFormat.parse(tvDateFrom.getText().toString())
                                    .getTime() >
                                    Workout.dateFormat.parse(tvDateBy.getText().toString())
                                            .getTime()) {
                                tvDateFrom.setText(tvDateBy.getText().toString());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, year, month, day);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Resources res = dialog.getContext().getResources();
                    int idDivider = res.getIdentifier("titleDivider", "id", "android");
                    int idTitle = res.getIdentifier("alertTitle", "id", "android");
                    View titleDivider = dialog.findViewById(idDivider);
                    TextView title = (TextView) dialog.findViewById(idTitle);
                    title.setTextColor(ContextCompat
                            .getColor(getApplicationContext(), android.R.color.black));
                    titleDivider.setBackgroundColor(ContextCompat
                            .getColor(getApplicationContext(), android.R.color.black));
                    DatePicker dpView = dialog.getDatePicker();
                    LinearLayout llFirst = (LinearLayout) dpView.getChildAt(0);
                    LinearLayout llSecond = (LinearLayout) llFirst.getChildAt(0);
                    for (int i = 0; i < llSecond.getChildCount(); i++) {
                        NumberPicker picker = (NumberPicker) llSecond.getChildAt(i);
                        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
                        for (Field pf : pickerFields) {
                            if (pf.getName().equals("mSelectionDivider")) {
                                pf.setAccessible(true);
                                try {
                                    pf.set(picker,
                                            new ColorDrawable(ContextCompat.getColor(
                                                    getApplicationContext(),
                                                    android.R.color.black)));
                                } catch (IllegalArgumentException
                                        | Resources.NotFoundException
                                        | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
            });
        }
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SW_FAVOURITE_CHECKED, swFavourite.isChecked());
        outState.putSerializable(KEY_SW_DATE_CHECKED, swDate.isChecked());
        outState.putSerializable(KEY_TV_DATE_FROM_TEXT, tvDateFrom.getText().toString());
        outState.putSerializable(KEY_TV_DATE_BY_TEXT, tvDateBy.getText().toString());
    }

    @Override
    public void onBackPressed() {
        filtersPref.edit().putString(PREF_DATE_FROM, tvDateFrom.getText().toString()).apply();
        filtersPref.edit().putString(PREF_DATE_BY, tvDateBy.getText().toString()).apply();
        setResult(Activity.RESULT_OK, MainActivity
                .newIntent(this, isFavouriteEnabled, isDateEnabled,
                        tvDateFrom.getText().toString(), tvDateBy.getText().toString()));
        super.onBackPressed();
    }
}
