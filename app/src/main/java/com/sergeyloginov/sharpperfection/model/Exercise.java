package com.sergeyloginov.sharpperfection.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Exercise {

    private String id;
    private String label;
    private String title;
    private String start;
    private String finish;
    private ArrayList<Set> sets;

    public Exercise() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public ArrayList<Set> getSets() {
        return sets;
    }

    public void addSets(ArrayList<Set> sets) {
        this.sets = sets;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("start", start);
        map.put("finish", finish);
        map.put("label", label);
        map.put("title", title);
        return map;
    }

    @Override
    public String toString() {
        return "Exercise {"
                + "label = " + label + ", "
                + "title = " + title + ", "
                + "start = " + start + ", "
                + "finish = " + finish + "}";
    }
}
