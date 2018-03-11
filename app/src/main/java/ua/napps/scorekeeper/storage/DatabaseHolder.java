package ua.napps.scorekeeper.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CountersDao;

@Database(entities = {Counter.class}, version = 1, exportSchema = false)
public abstract class DatabaseHolder extends RoomDatabase {

    private static DatabaseHolder database;

    @NonNull
    public static DatabaseHolder database() {
        return database;
    }

    @MainThread
    public static void init(@NonNull Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), DatabaseHolder.class, "counters-database").build();
    }

    public abstract CountersDao countersDao();
}