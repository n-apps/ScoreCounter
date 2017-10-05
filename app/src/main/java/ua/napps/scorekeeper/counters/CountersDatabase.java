package ua.napps.scorekeeper.counters;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = { Counter.class }, version = 1) public abstract class CountersDatabase
    extends RoomDatabase {

  private static final String DB_NAME = "counters.db";

  public abstract CounterDao counterDao();

  public static CountersDatabase sInstance;

  // Get a database instance
  public static synchronized CountersDatabase getDatabaseInstance(Context context) {
    if (sInstance == null) {
      sInstance = create(context);
    }
    return sInstance;
  }

  // Create the database
  static CountersDatabase create(Context context) {
    RoomDatabase.Builder<CountersDatabase> builder =
        Room.databaseBuilder(context.getApplicationContext(), CountersDatabase.class, DB_NAME);
    return builder.build();
  }
}