package com.aspira;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Event {
    private final String id;
    private final String name;
    private final Long kickoff;
    private final List<Market> marketList;

    public Event(String id, String name, Long kickoff, List<Market> marketList) {
        this.id = id;
        this.name = name;
        this.kickoff = kickoff;
        this.marketList = marketList;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\t").append(this.name).append(", ").append(getUtcKickoff()).append(", ").append(id);
        for (Market market : marketList) {
            result.append("\n").append(market);
        }
        return result.toString();
    }

    private String getUtcKickoff() {
        Instant instant = Instant.ofEpochMilli(kickoff);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return formatter.format(instant);
    }
}
