package ua.napps.scorekeeper.counters;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = { Counter.class }, version = 1) public abstract class CountersDatabase
    extends RoomDatabase {

  public abstract CounterDao counterDao();
}