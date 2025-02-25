package com.aspira;

import java.util.List;

public class Market {
    String id;
    String name;
    List<Runner> runnerList;

    public Market(String id, String name, List<Runner> runnerList) {
        this.id = id;
        this.name = name;
        this.runnerList = runnerList;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\t\t").append(this.name);
        for (Runner runner : runnerList) {
            result.append("\n").append(runner);
        }
        return result.toString();
    }
}
