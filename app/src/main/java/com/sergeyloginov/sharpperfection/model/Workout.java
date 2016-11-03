package com.sergeyloginov.sharpperfection.model;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class Workout {

    private String id;
    private String date;
    private String start;
    private String finish;
    private String labels;
    private String exercises;
    private TreeSet<String> labelSet = new TreeSet<>();
    private boolean favourite = false;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault());
    public static SimpleDateFormat weekdayFormat = new SimpleDateFormat(
            "EEEE", Locale.getDefault());
    public static SimpleDateFormat timeFormat = new SimpleDateFormat(
            "HH:mm:ss", Locale.getDefault());

    public Workout() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    public String getLabels() {
        return labels;
    }

    public void createLabels() {
        labels = "";
        for (String label : labelSet) {
            labels = labels + label + " ";
        }
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        labelSet.add(label);
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public String getExercises() {
        return exercises;
    }

    public void addExercises(String exercises) {
        this.exercises = exercises;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("date", date);
        map.put("start", start);
        map.put("finish", finish);
        map.put("labels", labels);
        map.put("favourite", favourite);
        map.put("exercises", exercises);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Workout) {
            Workout workout = (Workout) o;
            return this.date.equals(workout.getDate())
                    && this.start.equals(workout.getStart())
                    && this.finish.equals(workout.getFinish())
                    && this.labels.equals(workout.getLabels());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Workout {"
                + "start = " + start + ", "
                + "finish = " + finish + ", "
                + "labels = " + labels + "}";
    }
}
