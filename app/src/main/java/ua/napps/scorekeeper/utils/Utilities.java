package ua.napps.scorekeeper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class Utilities {

    public static Pattern pattern = Pattern.compile("[\\-0-9]+");

    public static Integer parseInt(String value) {
        if (value == null) {
            return 0;
        }
        Integer val = 0;
        try {
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                String num = matcher.group(0);
                val = Integer.parseInt(num);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return val;
    }
}
