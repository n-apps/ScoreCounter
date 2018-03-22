package ua.napps.scorekeeper.storage;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CountersDao;

@Database(entities = {Counter.class}, version = 2, exportSchema = false)
public abstract class DatabaseHolder extends RoomDatabase {

    private static DatabaseHolder database;

    @NonNull
    public static DatabaseHolder database() {
        return database;
    }

    @MainThread
    public static void init(@NonNull Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), DatabaseHolder.class, "counters-database")
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract CountersDao countersDao();


    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Since we didn't alter the table, there's nothing else to do here.
    }
};
}