package ua.napps.scorekeeper.utils;

import java.util.ArrayList;

import ua.napps.scorekeeper.log.LogEntry;

public class Singleton {
    private static final Singleton ourInstance = new Singleton();
    public static Singleton getInstance() {
        return ourInstance;
    }

    private ArrayList<LogEntry> logEntries = new ArrayList<>();

    private Singleton() {
    }

    public ArrayList<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void addLogEntry(LogEntry entry) {
        //TODO add logic to combine multiple single inc/dec logs
        this.logEntries.add(entry);
    }
}
