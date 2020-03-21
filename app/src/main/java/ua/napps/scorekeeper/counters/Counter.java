package ua.napps.scorekeeper.counters;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "counters")
public class Counter {

    private String color;

    private int defaultValue;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private int step;

    private int value;

    private int position;

    public Counter(@NonNull String name, String color, int position) {
        this.name = name;
        this.color = color;
        this.position = position;
        value = 0;
        defaultValue = 0;
        step = 1;
    }

    public Counter(Counter counter) {
        id = counter.id;
        this.name = counter.name;
        this.color = counter.color;
        this.value = counter.value;
        this.position = counter.position;
        defaultValue = counter.defaultValue;
        step = counter.step;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    @NonNull
    @Override
    public String toString() {
        return "Counter{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", value=" + value + '\'' + ", position=" + position + '}';
    }


    /**
     * Checks if two counters are equal, uses id, color, name and position (id would suffice but this way it safer)
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
        return this.id == counter.id && this.name.equals(counter.name) && this.position == counter.position;
    }
}
