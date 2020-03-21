package ua.napps.scorekeeper.log;

import java.util.Date;

import ua.napps.scorekeeper.counters.Counter;

/**
 * Simple Object that holds all attributes of a log entry
 */
public class LogEntry {

    public final Counter counter;
    public final LogType type;
    public int value;
    public final int prevValue;
    public final Date timestamp;
    public boolean combined;

    /**
     * Creates simple log entry with given type and value for current timestamp
     * @param counter - {@link Counter} Counter belonging to log entry
     * @param type - {@link LogType}to use for entry
     * @param value - {@link Integer} value for entry
     * @param prevValue - {@link Integer} previous value of counter
     */
    public LogEntry(Counter counter, LogType type, int value, int prevValue){
        this.counter = counter;
        this.type = type;
        this.value = value;
        this.prevValue = prevValue;
        this.timestamp = new Date();
        this.combined = false;
    }

    /**
     * Custom toString for easy debugging
     * @return string representation of {@link LogEntry} Object
     */
    @Override
    public String toString(){
        return type.toString() + " - " + prevValue + " -> " + value;
    }

}
