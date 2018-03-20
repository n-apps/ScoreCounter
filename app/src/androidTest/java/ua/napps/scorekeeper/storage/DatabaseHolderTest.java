package ua.napps.scorekeeper.storage;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import ua.napps.scorekeeper.counters.Counter;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DatabaseHolderTest {

        private static DatabaseHolder database;

    @Before
    public void setUp() {
         database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), DatabaseHolder.class).build();
    }

    @After
    public void tearDown() throws java.io.IOException {
        database.close();
    }

    @Test
public void insertDeleteAndCount() {
    final Counter counter = new Counter("Counter", "#2196F3");

    assertThat(database.countersDao().count(), Is.is(0));
            database.countersDao().insert(counter);
      assertThat(database.countersDao().count(), Is.is(1));

    List<Counter> counters = database.countersDao().loadAllCountersSync();
    Counter dbCounter = counters.get(0);
    assertEquals(dbCounter.getName(), "Counter");

    database.countersDao().deleteCounter(dbCounter);
    assertThat(database.countersDao().count(), Is.is(0));

}

}