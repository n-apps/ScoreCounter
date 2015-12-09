package ua.napps.scorekeeper.Events;

import ua.napps.scorekeeper.Models.Counter;

/**
 * Created by novo on 11/30/2015.
 */
public class CounterCaptionClick {
   Counter counter;

    public  CounterCaptionClick(Counter counter) {
        this.counter = counter;
    }

    public Counter getCounter(){
        return counter;
    }

}
