package com.sergeyloginov.sharpperfection.model;

import java.util.HashMap;
import java.util.Map;

public class Set {

    private String id;
    private String start;
    private String finish;
    private String weight;
    private String reps;

    public Set() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
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

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("start", start);
        map.put("finish", finish);
        map.put("weight", weight);
        map.put("reps", reps);
        return map;
    }

    @Override
    public String toString() {
        return "Set {"
                + "start = " + start + ", "
                + "finish = " + finish + ", "
                + "weight = " + weight + ", "
                + "reps = " + reps + "}";
    }
}
