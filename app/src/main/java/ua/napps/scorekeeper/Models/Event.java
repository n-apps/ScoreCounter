package ua.napps.scorekeeper.Models;

/**
 * Created by novo on 12/2/2015.
 */
public class Event {
    private String name;
    private String source;
    public Event(String name, String source) {
        this.name = name;
        this.source = source;
    }
    @Override
    public String toString() {
        return String.format("(name=%s, source=%s)", name, source);
    }
}