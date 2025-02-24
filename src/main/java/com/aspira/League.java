package com.aspira;

import java.util.List;

public class League {
    private final String id;
    private final String name;
    private List<Event> eventList;
    public League(String id, String name, List<Event> eventList) {
        this.id = id;
        this.name = name;
        this.eventList = eventList;
    }
    public String getId() {
        return id;
    }
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.name);
        for (Event event : eventList) {
            result.append("\n").append(event);
        }
        return result.toString();
    }
}
