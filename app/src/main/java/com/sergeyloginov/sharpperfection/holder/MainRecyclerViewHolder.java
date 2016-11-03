package com.sergeyloginov.sharpperfection.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.model.Workout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainRecyclerViewHolder extends RecyclerView.ViewHolder {

    private ImageButton ibStar;
    private TextView tvWeekday;
    private TextView tvDate;
    private TextView tvMuscleGroups;
    private TextView tvDuration;
    private ImageView ivWeekday;

    public MainRecyclerViewHolder(View itemView) {
        super(itemView);
        ibStar = (ImageButton) itemView.findViewById(R.id.ib_star);
        tvWeekday = (TextView) itemView.findViewById(R.id.tv_weekday);
        tvDate = (TextView) itemView.findViewById(R.id.tv_date);
        tvMuscleGroups = (TextView) itemView.findViewById(R.id.tv_muscle_groups);
        tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
        ivWeekday = (ImageView) itemView.findViewById(R.id.iv_weekday);
    }

    public void bind(Context context, final Workout workout,
                     View.OnClickListener starClickListener) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(Workout.dateFormat.parse(workout.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            ivWeekday.setImageResource(R.drawable.ic_monday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            ivWeekday.setImageResource(R.drawable.ic_tuesday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            ivWeekday.setImageResource(R.drawable.ic_wednesday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            ivWeekday.setImageResource(R.drawable.ic_thursday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            ivWeekday.setImageResource(R.drawable.ic_friday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            ivWeekday.setImageResource(R.drawable.ic_saturday);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            ivWeekday.setImageResource(R.drawable.ic_sunday);
        }
        tvDate.setText(new SimpleDateFormat("dd MMM", Locale.getDefault())
                .format(calendar.getTime()));
        String weekday = Workout.weekdayFormat.format(calendar.getTime());
        String outputWeekday = weekday.substring(0, 1).toUpperCase() + weekday.substring(1);
        tvWeekday.setText(outputWeekday);
        String mg = context.getResources().getString(R.string.muscle_groups)
                + " " + workout.getLabels();
        tvMuscleGroups.setText(mg);
        String duration = context.getResources().getString(R.string.start_time_list_item)
                + " " + workout.getStart()
                + " " + context.getResources().getString(R.string.finish_time_list_item)
                + " " + workout.getFinish();
        tvDuration.setText(duration);
        if (workout.isFavourite()) {
            ibStar.setImageResource(R.drawable.ic_star_fav);
        } else {
            ibStar.setImageResource(R.drawable.ic_star);
        }
        ibStar.setOnClickListener(starClickListener);
    }

    public void enableFavourite() {
        ibStar.setImageResource(R.drawable.ic_star_fav);
    }

    public void disableFavourite() {
        ibStar.setImageResource(R.drawable.ic_star);
    }
}
