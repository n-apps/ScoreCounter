package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class TinyDB {

  private SharedPreferences preferences;

  public TinyDB(Context appContext) {
    preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
  }

  /**
   * Get int value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
   *
   * @param key SharedPreferences key
   * @return int value at 'key' or 'defaultValue' if key not found
   */
  public int getInt(String key) {
    return preferences.getInt(key, 0);
  }

  /**
   * Get parsed ArrayList of Integers from SharedPreferences at 'key'
   *
   * @param key SharedPreferences key
   * @return ArrayList of Integers
   */
  public ArrayList<Integer> getListInt(String key) {
    String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
    ArrayList<String> arrayToList = new ArrayList<String>(Arrays.asList(myList));
    ArrayList<Integer> newList = new ArrayList<Integer>();

    for (String item : arrayToList)
      newList.add(Integer.parseInt(item));

    return newList;
  }

  /**
   * Get long value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
   *
   * @param key SharedPreferences key
   * @param defaultValue long value returned if key was not found
   * @return long value at 'key' or 'defaultValue' if key not found
   */
  public long getLong(String key, long defaultValue) {
    return preferences.getLong(key, defaultValue);
  }

  /**
   * Get float value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
   *
   * @param key SharedPreferences key
   * @return float value at 'key' or 'defaultValue' if key not found
   */
  public float getFloat(String key) {
    return preferences.getFloat(key, 0);
  }

  /**
   * Get double value from SharedPreferences at 'key'. If exception thrown, return 'defaultValue'
   *
   * @param key SharedPreferences key
   * @param defaultValue double value returned if exception is thrown
   * @return double value at 'key' or 'defaultValue' if exception is thrown
   */
  public double getDouble(String key, double defaultValue) {
    String number = getString(key);

    try {
      return Double.parseDouble(number);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Get parsed ArrayList of Double from SharedPreferences at 'key'
   *
   * @param key SharedPreferences key
   * @return ArrayList of Double
   */
  public ArrayList<Double> getListDouble(String key) {
    String[] myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚");
    ArrayList<String> arrayToList = new ArrayList<String>(Arrays.asList(myList));
    ArrayList<Double> newList = new ArrayList<Double>();

    for (String item : arrayToList)
      newList.add(Double.parseDouble(item));

    return newList;
  }

  /**
   * Get String value from SharedPreferences at 'key'. If key not found, return ""
   *
   * @param key SharedPreferences key
   * @return String value at 'key' or "" (empty String) if key not found
   */
  public String getString(String key) {
    return preferences.getString(key, "");
  }

  /**
   * Get parsed ArrayList of String from SharedPreferences at 'key'
   *
   * @param key SharedPreferences key
   * @return ArrayList of String
   */
  public ArrayList<String> getListString(String key) {
    return new ArrayList<String>(
        Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
  }

  /**
   * Get boolean value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
   *
   * @param key SharedPreferences key
   * @return boolean value at 'key' or 'defaultValue' if key not found
   */
  public boolean getBoolean(String key) {
    return preferences.getBoolean(key, false);
  }

  /**
   * Get parsed ArrayList of Boolean from SharedPreferences at 'key'
   *
   * @param key SharedPreferences key
   * @return ArrayList of Boolean
   */
  public ArrayList<Boolean> getListBoolean(String key) {
    ArrayList<String> myList = getListString(key);
    ArrayList<Boolean> newList = new ArrayList<Boolean>();

    for (String item : myList) {
      if (item.equals("true")) {
        newList.add(true);
      } else {
        newList.add(false);
      }
    }

    return newList;
  }

  // Put methods

  /**
   * Put int value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value int value to be added
   */
  public void putInt(String key, int value) {
    checkForNullKey(key);
    preferences.edit().putInt(key, value).apply();
  }

  /**
   * Put ArrayList of Integer into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param intList ArrayList of Integer to be added
   */
  public void putListInt(String key, ArrayList<Integer> intList) {
    checkForNullKey(key);
    Integer[] myIntList = intList.toArray(new Integer[intList.size()]);
    preferences.edit().putString(key, TextUtils.join("‚‗‚", myIntList)).apply();
  }

  /**
   * Put long value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value long value to be added
   */
  public void putLong(String key, long value) {
    checkForNullKey(key);
    preferences.edit().putLong(key, value).apply();
  }

  /**
   * Put float value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value float value to be added
   */
  public void putFloat(String key, float value) {
    checkForNullKey(key);
    preferences.edit().putFloat(key, value).apply();
  }

  /**
   * Put double value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value double value to be added
   */
  public void putDouble(String key, double value) {
    checkForNullKey(key);
    putString(key, String.valueOf(value));
  }

  /**
   * Put ArrayList of Double into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param doubleList ArrayList of Double to be added
   */
  public void putListDouble(String key, ArrayList<Double> doubleList) {
    checkForNullKey(key);
    Double[] myDoubleList = doubleList.toArray(new Double[doubleList.size()]);
    preferences.edit().putString(key, TextUtils.join("‚‗‚", myDoubleList)).apply();
  }

  /**
   * Put String value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value String value to be added
   */
  public void putString(String key, String value) {
    checkForNullKey(key);
    checkForNullValue(value);
    preferences.edit().putString(key, value).apply();
  }

  /**
   * Put ArrayList of String into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param stringList ArrayList of String to be added
   */
  public void putListString(String key, ArrayList<String> stringList) {
    checkForNullKey(key);
    String[] myStringList = stringList.toArray(new String[stringList.size()]);
    preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
  }

  /**
   * Put boolean value into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param value boolean value to be added
   */
  public void putBoolean(String key, boolean value) {
    checkForNullKey(key);
    preferences.edit().putBoolean(key, value).apply();
  }

  /**
   * Put ArrayList of Boolean into SharedPreferences with 'key' and save
   *
   * @param key SharedPreferences key
   * @param boolList ArrayList of Boolean to be added
   */
  public void putListBoolean(String key, ArrayList<Boolean> boolList) {
    checkForNullKey(key);
    ArrayList<String> newList = new ArrayList<String>();

    for (Boolean item : boolList) {
      if (item) {
        newList.add("true");
      } else {
        newList.add("false");
      }
    }

    putListString(key, newList);
  }

  /**
   * Remove SharedPreferences item with 'key'
   *
   * @param key SharedPreferences key
   */
  public void remove(String key) {
    preferences.edit().remove(key).apply();
  }

  /**
   * Clear SharedPreferences (remove everything)
   */
  public void clear() {
    preferences.edit().clear().apply();
  }

  /**
   * Retrieve all values from SharedPreferences. Do not modify collection return by method
   *
   * @return a Map representing a list of key/value pairs from SharedPreferences
   */
  public Map<String, ?> getAll() {
    return preferences.getAll();
  }

  /**
   * Register SharedPreferences change listener
   *
   * @param listener listener object of OnSharedPreferenceChangeListener
   */
  public void registerOnSharedPreferenceChangeListener(
      SharedPreferences.OnSharedPreferenceChangeListener listener) {

    preferences.registerOnSharedPreferenceChangeListener(listener);
  }

  /**
   * Unregister SharedPreferences change listener
   *
   * @param listener listener object of OnSharedPreferenceChangeListener to be unregistered
   */
  public void unregisterOnSharedPreferenceChangeListener(
      SharedPreferences.OnSharedPreferenceChangeListener listener) {

    preferences.unregisterOnSharedPreferenceChangeListener(listener);
  }

  /**
   * null keys would corrupt the shared pref file and make them unreadable this is a preventive
   * measure
   *
   * @param key pref key
   */
  public void checkForNullKey(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
  }

  /**
   * null keys would corrupt the shared pref file and make them unreadable this is a preventive
   * measure
   *
   * @param value pref key
   */
  public void checkForNullValue(String value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}