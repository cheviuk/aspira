package com.aspira;

public class Runner {
    private final String id;
    private final String name;
    private final Double ratio;
    public Runner(String id, String name, Double ratio) {
        this.id = id;
        this.name = name;
        this.ratio = ratio;
    }
    @Override
    public String toString() {
        return "\t\t\t" + name + ", " + ratio + ", " + id;
    }
}
