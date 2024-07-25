package ua.napps.scorekeeper.log;

/**
 * Custom types for log entry types
 * <p>
 * INC: single increment (+ click)
 * INC_C: custom increment (long + click)
 * DEC: single decrement (- click)
 * DEC_C: custom decrement (long - click)
 * SET: counter set trough edit interface
 * RST: counter reset trough menu
 * RMV: counter removed (trough menu or edit interface)
 *
 */
public enum LogType {
    INC,INC_C,DEC_C,DEC,SET,RST,RMV
}
