package ua.napps.scorekeeper.utils;

import android.content.Context;

import java.util.ArrayList;

import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;

public class Singleton {
    private static final Singleton ourInstance = new Singleton();
    private ArrayList<LogEntry> logEntries = new ArrayList<>();
    private Context mainContext;

    private Singleton() {
    }

    public static Singleton getInstance() {
        return ourInstance;
    }

    public Context getMainContext() {
        return mainContext;
    }

    public void setMainContext(Context mainContext) {
        this.mainContext = mainContext;
    }

    public ArrayList<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void addLogEntry(LogEntry entry) {
        if (logEntries.size() > 0) {
            LogEntry lastEntry = logEntries.get(0);
            if (entry.type == LogType.INC || entry.type == LogType.DEC) {
                if (lastEntry.type == entry.type && lastEntry.counter.equals(entry.counter) && System.currentTimeMillis() - lastEntry.timestamp.getTime() < 2000) {
                    lastEntry.value = lastEntry.value + entry.value;
                    lastEntry.combined = true;
                    this.logEntries.set(0, lastEntry);
                    return;
                }
            }
        }

        this.logEntries.add(0, entry);
    }

    public void clearLogEntries() {
        this.logEntries.clear();
    }
}
