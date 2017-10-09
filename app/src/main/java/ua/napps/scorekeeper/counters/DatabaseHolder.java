package ua.napps.scorekeeper.counters;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

@Database(entities = { Counter.class }, version = 1) public abstract class DatabaseHolder
    extends RoomDatabase {

  public abstract CountersDao countersDao();

  private static DatabaseHolder database;

  @MainThread public static void init(@NonNull Context context) {
    database = Room.databaseBuilder(context.getApplicationContext(), DatabaseHolder.class,
        "counters-database").build();
  }

  @NonNull public static DatabaseHolder database() {
    return database;
  }
}