package ua.napps.scorekeeper.counters;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "counters")
public class Counter {

    private String color;

    private int defaultValue;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private int step;

    private int value;

    public Counter(@NonNull String name, String color) {
        this.name = name;
        this.color = color;
        value = 0;
        defaultValue = 0;
        step = 1;
    }

    public Counter(Counter counter) {
        id = counter.id;
        this.name = counter.name;
        color = counter.color;
        value = counter.value;
        defaultValue = counter.defaultValue;
        step = counter.step;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Counter{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", value=" + value + '}';
    }

    /**
     * Checks if two counters are equal, uses id, color and name (id would suffice but this way it safer)
     * @param other - {@link Object } to compare to
     * @return {@link Boolean} if equal or not
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Counter)) {
            return false;
        }
        Counter counter = (Counter) other;
        return this.id == counter.id && this.name.equals(counter.name);
    }
}
