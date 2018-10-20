package ua.napps.scorekeeper.storage;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

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