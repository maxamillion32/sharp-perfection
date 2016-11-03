package com.sergeyloginov.sharpperfection.utils;

import android.os.Handler;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class TimerHelper {

    public static final int FORMAT_HOURS = 10;
    public static final int FORMAT_MINUTES = 20;
    private Handler timerHandler = new Handler();
    private TextView output;
    private boolean isRunning = false;
    private long millis;
    private long startTime;
    private long delta = 0;
    private int hours;
    private int minutes;
    private int seconds;
    private int format = 10;

    public interface OnSecondPassListener {
        void onSecondPass(int hours, int minutes, int seconds);
    }

    private OnSecondPassListener onSecondPassListener;

    public void setOnSecondPassListener(OnSecondPassListener listener) {
        onSecondPassListener = listener;
    }

    public TimerHelper() {}

    public TimerHelper(TextView output, int format) {
        this.output = output;
        this.format = format;
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime + delta;
            seconds = (int) (millis / 1000);
            minutes = seconds / 60;
            hours = minutes / 60;
            minutes = minutes % 60;
            seconds = seconds % 60;

            if (onSecondPassListener != null) {
                onSecondPassListener.onSecondPass(hours, minutes, seconds);
            }

            if (format == FORMAT_HOURS) {
                if (output != null) {
                    output.setText(String.format(
                            Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                }
            } else if (format == FORMAT_MINUTES) {
                if (output != null) {
                    output.setText(String.format(
                            Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    public void startTimer() {
        isRunning = true;
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void finishTimer() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public void setOutput(TextView output) {
        this.output = output;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public long getValue() {
        return this.millis;
    }

    public Date getDateValue() {
        return new Date();
    }

    public String getTime(int format) {
        String result = "";
        if (format == FORMAT_HOURS) {
            result = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else if (format == FORMAT_MINUTES) {
            result = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        return result;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
