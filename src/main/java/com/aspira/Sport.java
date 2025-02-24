package com.aspira;

import java.util.List;

public class Sport {
    private final String id;
    private final String name;
    private final List<League> leagueList;
    public Sport(String id, String name, List<League> leagueList) {
        this.id = id;
        this.name = name;
        this.leagueList = leagueList;
    }
    public String getId() {
        return id;
    }
    public List<League> getLeagueList() {
        return leagueList;
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (League league : leagueList) {
            result.append(this.name).append(", ").append(league).append("\n");
        }
        return result.toString();
    }
}
